package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.models.ExtendedDeferredResult;
import ua.dp.hammer.smarthome.models.ProjectorResponse;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;

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
   private static final Logger LOGGER = LogManager.getLogger(MainLogic.class);

   private boolean turnProjectorOn;
   private boolean turnProjectorsOnManually;
   private Queue<ExtendedDeferredResult<ProjectorResponse>> projectorsDeferredResults = new ConcurrentLinkedQueue<>();
   private ScheduledFuture<?> scheduledFutureProjectorTurningOff;
   private LocalDateTime lastSentResponsesTime;
   private LocalDateTime thresholdHumidityStartTime;
   private String ipAddressToUpdateFirmware;
   private boolean alarmsAreBeingIgnored;

   private int projectorTurnOffTimeoutSec;
   private int deferredResponseTimeoutSec;
   private int ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec;

   @Autowired
   private Environment environment;

   @Autowired
   private ImmobilizerBean immobilizerBean;

   @Autowired
   private CameraBean cameraBean;

   @PostConstruct
   public void init() {
      projectorTurnOffTimeoutSec = Integer.parseInt(environment.getRequiredProperty("projectorTurnOffTimeoutSec"));
      deferredResponseTimeoutSec = Integer.parseInt(environment.getRequiredProperty("deferredResponseTimeoutSec"));
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
         ProjectorResponse projectorResponse = createProjectorResponse(clientIp);
         setUpdateStatus(projectorResponse, clientIp);
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
      LocalDateTime localDateTime = LocalDateTime.now();

      if (turnProjectorsOnManually || localDateTime.getHour() >= 20 || localDateTime.getHour() <= 6) {
         if (scheduledFutureProjectorTurningOff != null && !scheduledFutureProjectorTurningOff.isDone()) {
            scheduledFutureProjectorTurningOff.cancel(false);

            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Projector turning off scheduled task has been canceled");
            }
         }

         scheduledFutureProjectorTurningOff = new ConcurrentTaskScheduler().schedule(new Runnable() {
            @Override
            public void run() {
               switchProjectors(ProjectorState.TURN_OFF);
            }
         }, new Date(System.currentTimeMillis() + projectorTurnOffTimeoutSec * 1000));

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
      if (humidity >= 80.0f) {
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

   public void ignoreAlarms(int timeout) {
      alarmsAreBeingIgnored = true;

      if (timeout > 0) {
         Timer timer = new Timer();

         timer.schedule(new TimerTask() {
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
         LOGGER.info("Alarms are being ignored");
      } else {
         LOGGER.info("Alarms are not ignored anymore");
      }
   }

   private void sendKeepHeartResponse() {
      switchProjectors(null);
   }

   private void switchProjectors(ProjectorState newProjectorState) {
      if (newProjectorState != null) {
         turnProjectorOn = newProjectorState == ProjectorState.TURN_ON;
      }

      if (turnProjectorOn) {
         LOGGER.info("Projectors are turning on...");
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred results are ready to be returned. Size: " + projectorsDeferredResults.size() +
               ". New projectors state: " + newProjectorState + ". State to set: " + turnProjectorOn);
      }

      while (!projectorsDeferredResults.isEmpty()) {
         ExtendedDeferredResult<ProjectorResponse> projectorDeferredResult = projectorsDeferredResults.poll();

         if (projectorDeferredResult == null) {
            return;
         }

         ProjectorResponse projectorResponse = createProjectorResponse(projectorDeferredResult.getClientIp());

         projectorDeferredResult.setResult(projectorResponse);
      }
      lastSentResponsesTime = LocalDateTime.now();
   }

   private ProjectorResponse createProjectorResponse(String clientIp) {
      ProjectorResponse projectorResponse = new ProjectorResponse(StatusCodes.OK);

      projectorResponse.setTurnOn(turnProjectorOn || turnProjectorsOnManually);
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
}
