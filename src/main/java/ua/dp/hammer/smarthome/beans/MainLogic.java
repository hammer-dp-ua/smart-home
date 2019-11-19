package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.dp.hammer.smarthome.clients.StreetProjectors;
import ua.dp.hammer.smarthome.models.ServerStatus;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class MainLogic {
   private final static Logger LOGGER = LogManager.getLogger(MainLogic.class);

   private boolean turnStreetProjectorsOn;
   private boolean turnProjectorsOnManually;
   private ScheduledFuture<?> scheduledFutureProjectorTurningOff;

   private String deviceNameToUpdateFirmware;
   private Timer cancelIgnoringAlarmsTimer;
   private boolean alarmsAreBeingIgnored;
   private int projectorTurnOffTimeoutSec;

   private int ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec;

   private Map<StreetProjectors, ProjectorState> failedStreetProjectors = new ConcurrentHashMap<>();

   private Environment environment;
   private ImmobilizerBean immobilizerBean;
   private CameraBean cameraBean;
   private EnvSensorsBean envSensorsBean;

   @PostConstruct
   public void init() {
      projectorTurnOffTimeoutSec = Integer.parseInt(environment.getRequiredProperty("projectorTurnOffTimeoutSec"));
      ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec =
            Integer.parseInt(environment.getRequiredProperty("ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec"));

      turnProjectorsOffManually();
   }

   public void receiveAlarm(String alarmSource) {
      turnProjectorsOn();

      if (alarmsAreBeingIgnored || (alarmSource != null && alarmSource.equals("MOTION_SENSOR_2"))) {
         return;
      }

      LocalDateTime immobilizerActivatedDateTime = immobilizerBean.getActivationDateTime();

      if (immobilizerActivatedDateTime != null) {
         LocalDateTime currentDateTime = LocalDateTime.now();
         long durationBetweenImmobilizerAndAlarm =
               Duration.between(currentDateTime, immobilizerActivatedDateTime).abs().getSeconds();

         if (durationBetweenImmobilizerAndAlarm >= ignoreVideoRecordingTimeoutAfterImmobilizerActivationSec) {
            cameraBean.startVideoRecording();
         } else {
            LOGGER.info("Video recording wasn't started because immobilizer was activated " +
                  durationBetweenImmobilizerAndAlarm + " seconds ago");
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

   private void turnProjectorsOn() {
      if (turnProjectorsOnManually || envSensorsBean.getStreetLightValue() < 50) {
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

   public void setDeviceNameToUpdateFirmware(String deviceNameToUpdateFirmware) {
      this.deviceNameToUpdateFirmware = deviceNameToUpdateFirmware;
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

   private void switchProjectors(ProjectorState newProjectorState) {
      if (newProjectorState != null) {
         turnStreetProjectorsOn = newProjectorState == ProjectorState.TURN_ON;
      }

      boolean turnOn = turnStreetProjectorsOn || turnProjectorsOnManually;

      if (turnOn) {
         LOGGER.info("Projectors are turning on...");
      } else {
         LOGGER.info("Projectors are turning off...");
      }

      sendProjectorsRequests(turnOn ? ProjectorState.TURN_ON : ProjectorState.TURN_OFF);
   }

   private void sendProjectorsRequests(ProjectorState newProjectorsState) {
      for (StreetProjectors projector : StreetProjectors.values()) {
         failedStreetProjectors.remove(projector);

         sendProjectorRequest(projector, newProjectorsState);
      }
   }

   private void sendProjectorRequest(StreetProjectors projector, ProjectorState newProjectorState) {
      WebClient client = WebClient.builder()
            .baseUrl("http://" + projector.getIpAddress())
            .build();

      client.method(HttpMethod.GET);

      String actionParamValue = newProjectorState == ProjectorState.TURN_ON ? "turnOn" : "turnOff";
      Mono<Void> deferredResponse = client
            .get()
            .uri(uriBuilder -> uriBuilder
                  .queryParam("action", actionParamValue)
                  .build())
            .retrieve()
            .bodyToMono(Void.class);

      Flux.merge(deferredResponse)
            .subscribe(null, e -> failedStreetProjectors.put(projector, newProjectorState));
   }

   @Scheduled(fixedDelay=60000)
   public void resendToFailedStreetProjectors() {
      for (Map.Entry<StreetProjectors, ProjectorState> entry : failedStreetProjectors.entrySet()) {
         sendProjectorRequest(entry.getKey(), entry.getValue());
         LOGGER.info("Request with " + entry.getValue() + " state has been resent to " + entry.getKey().getIpAddress());
      }

      failedStreetProjectors.clear();
   }

   private enum ProjectorState {
      TURN_ON, TURN_OFF
   }

   public void setUpdateFirmwareStatus(ServerStatus response, String deviceName) {
      if (deviceName != null && deviceName.equals(deviceNameToUpdateFirmware)) {
         response.setUpdateFirmware(true);
         deviceNameToUpdateFirmware = null;

         LOGGER.info("Firmware of '" + deviceName + "' will be updated");
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

   @Autowired
   public void setEnvSensorsBean(EnvSensorsBean envSensorsBean) {
      this.envSensorsBean = envSensorsBean;
   }
}
