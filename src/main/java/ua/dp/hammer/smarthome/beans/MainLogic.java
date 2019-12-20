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
import reactor.util.StringUtils;
import ua.dp.hammer.smarthome.clients.StreetProjectors;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.states.AlarmsState;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
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

   private ProjectorResponsesCollector projectorResponsesCollector;

   private Environment environment;
   private ImmobilizerBean immobilizerBean;
   private CameraBean cameraBean;
   private EnvSensorsBean envSensorsBean;
   private StatesBean statesBean;

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
      if (turnProjectorsOnManually || envSensorsBean.getStreetLightValue() < 5) {
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

   public String setDeviceNameToUpdateFirmware(String deviceNameToUpdateFirmware) {
      this.deviceNameToUpdateFirmware = deviceNameToUpdateFirmware;
      return deviceNameToUpdateFirmware + "\r\nLength: " +
            (StringUtils.isEmpty(deviceNameToUpdateFirmware) ? 0 : deviceNameToUpdateFirmware.length());
   }

   public AlarmsState ignoreAlarms(int minutesTimeout) {
      alarmsAreBeingIgnored = true;
      String returnValue = null;

      if (cancelIgnoringAlarmsTimer != null) {
         cancelIgnoringAlarmsTimer.cancel();
      }

      if (minutesTimeout > 0) {
         cancelIgnoringAlarmsTimer = new Timer();
         returnValue = LocalDateTime.now().plusMinutes(minutesTimeout).toString();

         cancelIgnoringAlarmsTimer.scheduleAtFixedRate(new TimerTask() {
            private int executionsAmount = 0;

            @Override
            public void run() {
               executionsAmount++;

               if (executionsAmount >= minutesTimeout) {
                  alarmsAreBeingIgnored = false;
                  statesBean.changeAlarmsIgnoringState(false, 0);

                  LOGGER.info("Alarms are not ignored anymore");

                  cancel();
               } else {
                  int minutesRemaining = minutesTimeout - executionsAmount;
                  statesBean.changeAlarmsIgnoringState(true, minutesRemaining);
               }
            }
         },
   60 * 1000L,
   60 * 1000L);
      } else if (minutesTimeout < 0) {
         alarmsAreBeingIgnored = false;
      }

      statesBean.changeAlarmsIgnoringState(alarmsAreBeingIgnored, minutesTimeout);

      if (alarmsAreBeingIgnored) {
         LOGGER.info("Alarms will be ignored " + (minutesTimeout == 0 ? "indefinitely" : (minutesTimeout + " minutes and finishes ignoring at " + returnValue)));
      } else {
         returnValue = "Alarms are not ignored anymore";

         LOGGER.info(returnValue);
      }
      return new AlarmsState(alarmsAreBeingIgnored, minutesTimeout);
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
      projectorResponsesCollector = new ProjectorResponsesCollector.Builder()
            .setExpectedResponses(StreetProjectors.values().length)
            .setTurnOn(newProjectorsState == ProjectorState.TURN_ON)
            .setRunnableOnAllResponsesReceived((atLeastOneRequestReceived, turnOn) -> {
               if (atLeastOneRequestReceived) {
                  statesBean.changeProjectorState(turnOn);
               } else {
                  // To notify that no any changes occurred
                  statesBean.changeProjectorState(!turnOn);
               }
            })
            .build();

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
            .subscribe(null,
                  e -> {
                     failedStreetProjectors.put(projector, newProjectorState);
                     projectorResponsesCollector.errorResponseReceived();
                  },
                  () -> {
                     projectorResponsesCollector.okResponseReceived();
                  });
   }

   @Scheduled(fixedDelay=60000)
   public void resendToFailedStreetProjectors() {
      Map<StreetProjectors, ProjectorState> failedCopy = new HashMap<>();

      for (Map.Entry<StreetProjectors, ProjectorState> entry : failedStreetProjectors.entrySet()) {
         failedCopy.put(entry.getKey(), entry.getValue());
      }
      failedStreetProjectors.clear();

      for (Map.Entry<StreetProjectors, ProjectorState> entry : failedCopy.entrySet()) {
         sendProjectorRequest(entry.getKey(), entry.getValue());

         LOGGER.info("Previously failed request with " + entry.getValue() + " state has been resent to "
               + entry.getKey().getIpAddress());
      }
   }

   private enum ProjectorState {
      TURN_ON, TURN_OFF
   }

   public void setUpdateFirmwareStatus(ServerStatus response, String deviceName) {
      if (!StringUtils.isEmpty(deviceName) && deviceName.equals(deviceNameToUpdateFirmware)) {
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

   @Autowired
   public void setStatesBean(StatesBean statesBean) {
      this.statesBean = statesBean;
   }

   private static final class ProjectorResponsesCollector {
      private int expectedResponses;
      private int receivedResponses;
      private OnFinishRunnable runOnAllResponsesReceived;
      private Boolean turnOn;
      private boolean atLeastOneRequestReceived;

      private ProjectorResponsesCollector() {
      }

      public void okResponseReceived() {
         receivedResponses++;
         atLeastOneRequestReceived = true;
         runRunnable();
      }

      public void errorResponseReceived() {
         receivedResponses++;
         runRunnable();
      }

      public void runRunnable() {
         if (expectedResponses == receivedResponses) {
            runOnAllResponsesReceived.run(atLeastOneRequestReceived, turnOn);
         }
      }

      public static final class Builder {
         ProjectorResponsesCollector projectorResponsesCollector = new ProjectorResponsesCollector();

         public Builder setExpectedResponses(int expectedResponses) {
            projectorResponsesCollector.expectedResponses = expectedResponses;
            return this;
         }

         public Builder setRunnableOnAllResponsesReceived(OnFinishRunnable runOnAllResponsesReceived) {
            projectorResponsesCollector.runOnAllResponsesReceived = runOnAllResponsesReceived;
            return this;
         }

         public Builder setTurnOn(boolean turnOn) {
            projectorResponsesCollector.turnOn = turnOn;
            return this;
         }

         public ProjectorResponsesCollector build() {
            if (projectorResponsesCollector.expectedResponses == 0) {
               throw new IllegalStateException("Expected responses amount isn't set");
            }
            if (projectorResponsesCollector.runOnAllResponsesReceived == null) {
               throw new IllegalStateException("Runnable on all responses isn't set");
            }
            if (projectorResponsesCollector.turnOn == null) {
               throw new IllegalStateException("Turn on/off state isn't set");
            }
            return projectorResponsesCollector;
         }
      }

      public interface OnFinishRunnable {
         void run(boolean atLeastOneRequestReceived, boolean turnOn);
      }
   }
}
