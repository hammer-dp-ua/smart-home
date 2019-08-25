package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.dp.hammer.smarthome.clients.TcpServerClients;
import ua.dp.hammer.smarthome.models.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class MainLogic {
   private final static Logger LOGGER = LogManager.getLogger(MainLogic.class);

   private int streetLightValue;
   private boolean turnProjectorOn;
   private boolean turnProjectorsOnManually;
   private Queue<ExtendedDeferredResult<ProjectorResponse>> projectorsDeferredResults = new ConcurrentLinkedQueue<>();
   private ScheduledFuture<?> scheduledFutureProjectorTurningOff;
   private LocalDateTime lastSentResponsesTime;
   private LocalDateTime thresholdHumidityStartTime;
   private String ipAddressToUpdateFirmware;
   private Timer cancelIgnoringAlarmsTimer;
   private boolean alarmsAreBeingIgnored;

   private int projectorTurnOffTimeoutSec;
   private int deferredResponseTimeoutSec;
   private float thresholdBathroomHumidity;
   private int ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec;

   private Environment environment;
   private ImmobilizerBean immobilizerBean;
   private CameraBean cameraBean;

   @PostConstruct
   public void init() {
      projectorTurnOffTimeoutSec = Integer.parseInt(environment.getRequiredProperty("projectorTurnOffTimeoutSec"));
      deferredResponseTimeoutSec = Integer.parseInt(environment.getRequiredProperty("deferredResponseTimeoutSec"));
      thresholdBathroomHumidity = Float.parseFloat(environment.getRequiredProperty("thresholdBathroomHumidity"));
      ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec =
            Integer.parseInt(environment.getRequiredProperty("ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec"));
   }

   public void receiveAlarm(String alarmSource) {
      turnProjectorsOn();

      if (alarmsAreBeingIgnored ||
            (alarmSource != null && alarmSource.equals("MOTION_SENSOR_2"))) {
         return;
      }

      LocalDateTime immobilizerActivatedDateTime = immobilizerBean.getActivationDateTime();

      if (immobilizerActivatedDateTime != null) {
         LocalDateTime currentDateTime = LocalDateTime.now();
         long durationBetweenImmobilizerAndAlarm = Duration.between(currentDateTime, immobilizerActivatedDateTime).abs().getSeconds();

         if (durationBetweenImmobilizerAndAlarm >= ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec) {
            cameraBean.startVideoRecording();
         } else {
            LOGGER.info("Video recording wasn't started because immobilizer was activated " + durationBetweenImmobilizerAndAlarm +
                  " seconds ago");
         }
      } else {
         cameraBean.startVideoRecording();
      }
   }

   public void receiveImmobilizerActivation() {
      turnProjectorsOn();
      immobilizerBean.setActivationDateTime(LocalDateTime.now());

      if (cameraBean.isVideoRecordingInProcess()) {
         LOGGER.info("Video recording is stopping because immobilizer has been activated");

         cameraBean.scheduleStopVideoRecording(20, TimeUnit.SECONDS);
      }
   }

   public void addProjectorsDeferredResult(ExtendedDeferredResult<ProjectorResponse> projectorDeferredResult,
                                           String clientIp,
                                           boolean serverIsAvailable) {
      if (!serverIsAvailable) {
         // Return immediately on first request
         boolean turnOn = turnProjectorOn || turnProjectorsOnManually;
         ProjectorResponse projectorResponse = createProjectorResponse(clientIp, turnOn);
         projectorDeferredResult.setResult(projectorResponse);

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Server availability hasn't been detected yet");
         }
         return;
      }

      projectorsDeferredResults.add(projectorDeferredResult);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred result from " + clientIp + " has been added. Deferred results size: " + projectorsDeferredResults.size());
      }
   }

   @Scheduled(fixedRate = 5000)
   public void setProjectorsDeferredResult() {
      if (lastSentResponsesTime == null || LocalDateTime.now().minusSeconds(deferredResponseTimeoutSec).isAfter(lastSentResponsesTime)) {
         sendKeepHeartResponse();
      }
   }

   private void turnProjectorsOn() {
      if (turnProjectorsOnManually || streetLightValue < 50) {
         if (scheduledFutureProjectorTurningOff != null && !scheduledFutureProjectorTurningOff.isDone()) {
            scheduledFutureProjectorTurningOff.cancel(false);

            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Projector turning off scheduled task has been canceled");
            }
         }

         if (!turnProjectorsOnManually) {
            scheduledFutureProjectorTurningOff = new ConcurrentTaskScheduler().schedule(() ->
                  switchProjectors(ProjectorState.TURN_OFF), new Date(System.currentTimeMillis() + projectorTurnOffTimeoutSec * 1000));
         }

         switchProjectors(ProjectorState.TURN_ON);
      }
   }

   public void turnProjectorsOnManually() {
      turnProjectorsOnManually = true;
      turnProjectorsOn();
   }

   public void turnProjectorsOffManually() {
      turnProjectorsOnManually = false;
      switchProjectors(ProjectorState.TURN_OFF);
   }

   public boolean getBathroomFanState(float humidity, float temperature) {
      if (humidity >= thresholdBathroomHumidity) {
         thresholdHumidityStartTime = LocalDateTime.now();
      }

      LocalDateTime currentTime = LocalDateTime.now();
      return thresholdHumidityStartTime != null && currentTime.isBefore(thresholdHumidityStartTime.plusMinutes(10));
   }

   public void setIpAddressToUpdateFirmware(String ipAddressToUpdateFirmware) {
      this.ipAddressToUpdateFirmware = ipAddressToUpdateFirmware;
   }

   public void turnOnBathroomFan() {
      thresholdHumidityStartTime = LocalDateTime.now();
   }

   public String ignoreAlarms(int timeout) {
      alarmsAreBeingIgnored = true;
      String returnValue = "Ignoring indefinitely";

      if (cancelIgnoringAlarmsTimer != null) {
         cancelIgnoringAlarmsTimer.cancel();
      }

      if (timeout > 0) {
         cancelIgnoringAlarmsTimer = new Timer();
         returnValue = "Finishes ignoring at " + LocalDateTime.now().plusMinutes(timeout).toString();

         cancelIgnoringAlarmsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
               alarmsAreBeingIgnored = false;
               LOGGER.info("Alarms are not ignored anymore");
            }
         }, timeout * 60 * 1000);
      } else if (timeout == -1) {
         alarmsAreBeingIgnored = false;
      }

      if (alarmsAreBeingIgnored) {
         LOGGER.info("Alarms will be ignored " + (timeout == 0 ? "indefinitely" : (timeout + " minutes and finishes ignoring at " + returnValue)));
      } else {
         returnValue = "Alarms are not ignored anymore";

         LOGGER.info(returnValue);
      }
      return returnValue;
   }

   public void setStreetLightValue(int streetLightValue) {
      this.streetLightValue = streetLightValue;
   }

   private void sendKeepHeartResponse() {
      switchProjectors(null);
   }

   private void switchProjectors(ProjectorState newProjectorState) {
      if (newProjectorState != null) {
         turnProjectorOn = newProjectorState == ProjectorState.TURN_ON;
      }

      boolean turnOn = turnProjectorOn || turnProjectorsOnManually;

      if (turnOn) {
         LOGGER.info("Projectors are turning on...");
      } else {
         LOGGER.info("Projectors are turning off...");
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred results are ready to be returned. Size: " + projectorsDeferredResults.size());
      }

      while (!projectorsDeferredResults.isEmpty()) {
         ExtendedDeferredResult<ProjectorResponse> projectorDeferredResult = projectorsDeferredResults.poll();

         if (projectorDeferredResult == null) {
            continue;
         }

         ProjectorResponse projectorResponse = createProjectorResponse(projectorDeferredResult.getClientIp(), turnOn);

         projectorDeferredResult.setResult(projectorResponse);
      }

      RestTemplate requestTemplate = new RestTemplate();
      StringBuilder request = new StringBuilder("http://");

      request.append(TcpServerClients.ENTRANCE_PROJECTORS.getIpAddress());
      request.append("?action=");

      if (turnOn) {
         request.append("turnOn");
      } else {
         request.append("turnOff");
      }
      ResponseEntity<String> response = requestTemplate.getForEntity(request.toString(), String.class);
      if (HttpStatus.OK != response.getStatusCode()) {
         LOGGER.error(TcpServerClients.ENTRANCE_PROJECTORS.getIpAddress() + " client didn't return OK response");
      }

      lastSentResponsesTime = LocalDateTime.now();
   }

   private ProjectorResponse createProjectorResponse(String clientIp, boolean turnOn) {
      ProjectorResponse projectorResponse = new ProjectorResponse(StatusCodes.OK);

      projectorResponse.setTurnOn(turnOn);
      setUpdateStatus(projectorResponse, clientIp);
      if (LOGGER.isDebugEnabled()) {
         projectorResponse.setIncludeDebugInfo(true);
      }
      return projectorResponse;
   }

   private enum ProjectorState {
      TURN_ON, TURN_OFF
   }

   public void setUpdateStatus(ServerStatus response, String clientIp) {
      if (clientIp != null && clientIp.equals(ipAddressToUpdateFirmware)) {
         response.setUpdateFirmware(true);
         ipAddressToUpdateFirmware = null;

         LOGGER.info("Firmware of " + clientIp + " will be updated");
      }

      response.setIgnoreAlarms(alarmsAreBeingIgnored);
   }

   @Autowired
   public void setEnvironment(Environment environment) {
      this.environment = environment;
   }

   @Autowired
   public void setImmobilizerBean(ImmobilizerBean immobilizerBean) {
      this.immobilizerBean = immobilizerBean;
   }

   @Autowired
   public void setCameraBean(CameraBean cameraBean) {
      this.cameraBean = cameraBean;
   }
}
