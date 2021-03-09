package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.StringUtils;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.setup.DeviceType;
import ua.dp.hammer.smarthome.models.states.AlarmsState;
import ua.dp.hammer.smarthome.models.states.ProjectorState;
import ua.dp.hammer.smarthome.models.states.ShutterState;
import ua.dp.hammer.smarthome.models.states.ShutterStates;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

   private Environment environment;
   private ImmobilizerBean immobilizerBean;
   private CameraBean cameraBean;
   private EnvSensorsBean envSensorsBean;
   private ManagerStatesBean managerStatesBean;
   private DevicesRepository devicesRepository;

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
                  switchProjectors(ProjectorSwitchedState.TURN_OFF), new Date(System.currentTimeMillis() + projectorTurnOffTimeoutSec * 1000));
         }

         switchProjectors(ProjectorSwitchedState.TURN_ON);
      }
   }

   public void turnProjectorsOnManually() {
      turnProjectorsOnManually = true;
      turnProjectorsOn();
   }

   public void turnProjectorsOffManually() {
      turnProjectorsOnManually = false;
      switchProjectors(ProjectorSwitchedState.TURN_OFF);
   }

   public String setDeviceNameToUpdateFirmware(String deviceNameToUpdateFirmware) {
      this.deviceNameToUpdateFirmware = deviceNameToUpdateFirmware;
      return deviceNameToUpdateFirmware + "\r\nLength: " +
            (StringUtils.isEmpty(deviceNameToUpdateFirmware) ? 0 : deviceNameToUpdateFirmware.length());
   }

   public AlarmsState ignoreAlarms(int minutesTimeout) {
      alarmsAreBeingIgnored = true;
      String finishesIgnoringTime = null;

      if (cancelIgnoringAlarmsTimer != null) {
         cancelIgnoringAlarmsTimer.cancel();
      }

      if (minutesTimeout > 0) {
         cancelIgnoringAlarmsTimer = new Timer();
         finishesIgnoringTime = LocalDateTime.now().plusMinutes(minutesTimeout).toString();

         cancelIgnoringAlarmsTimer.scheduleAtFixedRate(new TimerTask() {
            private int executionsAmount = 0;

            @Override
            public void run() {
               executionsAmount++;

               if (executionsAmount >= minutesTimeout) {
                  alarmsAreBeingIgnored = false;
                  managerStatesBean.changeAlarmsIgnoringState(false, 0);

                  LOGGER.info("Alarms are not ignored anymore");

                  cancel();
               } else {
                  int minutesRemaining = minutesTimeout - executionsAmount;
                  managerStatesBean.changeAlarmsIgnoringState(true, minutesRemaining);
               }
            }
         },
   60 * 1000L,
   60 * 1000L);
      } else if (minutesTimeout < 0) {
         alarmsAreBeingIgnored = false;
      }

      managerStatesBean.changeAlarmsIgnoringState(alarmsAreBeingIgnored, minutesTimeout);

      if (alarmsAreBeingIgnored) {
         LOGGER.info("Alarms will be ignored " + (minutesTimeout == 0 ? "indefinitely" : (minutesTimeout + " minutes and finishes ignoring at " + finishesIgnoringTime)));
      } else {
         LOGGER.info("Alarms are not ignored anymore");
      }
      return new AlarmsState(alarmsAreBeingIgnored, minutesTimeout);
   }

   private void switchProjectors(ProjectorSwitchedState newProjectorState) {
      if (newProjectorState != null) {
         turnStreetProjectorsOn = newProjectorState == ProjectorSwitchedState.TURN_ON;
      }

      boolean turnOn = turnStreetProjectorsOn || turnProjectorsOnManually;

      if (turnOn) {
         LOGGER.info("Projectors are turning on...");
      } else {
         LOGGER.info("Projectors are turning off...");
      }

      sendProjectorsRequests(turnOn);
   }

   private void sendProjectorsRequests(boolean turnOn) {
      List<DeviceSetupEntity> projectors = devicesRepository.getDevicesByType(DeviceType.PROJECTOR);

      managerStatesBean.resetExpectedSequentialProjectorCounter();
      for (DeviceSetupEntity projector : projectors) {
         sendProjectorRequest(projector, turnOn, projectors.size());
      }
   }

   private void sendProjectorRequest(DeviceSetupEntity projector, boolean turnOn, int projectorsAmount) {
      WebClient client = WebClient.builder()
            .baseUrl("http://" + projector.getIp4Address())
            .build();

      client.method(HttpMethod.GET);

      String actionParamValue = turnOn ? "turnOn" : "turnOff";
      Mono<Void> deferredResponse = client
            .get()
            .uri(uriBuilder -> uriBuilder
                  .queryParam("action", actionParamValue)
                  .build())
            .retrieve()
            .bodyToMono(Void.class);

      ProjectorState projectorState = new ProjectorState(projector.getName());
      projectorState.setTurnedOn(turnOn);

      Flux.merge(deferredResponse)
            .subscribe(null,
                  e -> {
                  LOGGER.error("Projector isn't available: " + projector.getName(), e);
                  projectorState.setNotAvailable(true);
                  managerStatesBean.changeProjectorState(projectorState, projectorsAmount);
               },
                  () -> {
                  projectorState.setNotAvailable(false);
                  managerStatesBean.changeProjectorState(projectorState, projectorsAmount);
               });
   }

   public void doShutter(String name, int no, boolean open) {
      DeviceSetupEntity shutterEntity = devicesRepository.getDeviceTypeNameEntity(name);

      sendShutterStateRequest(shutterEntity, no, open);
   }

   private void sendShutterStateRequest(DeviceSetupEntity shutter, int shutterNo, boolean open) {
      WebClient client = WebClient.builder()
            .baseUrl("http://" + shutter.getIp4Address())
            .build();

      client.method(HttpMethod.GET);

      String action = open ? "open" : "close";
      Mono<Void> deferredResponse = client
            .get()
            .uri(uriBuilder -> uriBuilder
                  .queryParam(action, shutter.getShutterActionTimeSetup().getActionTime())
                  .queryParam("shutter_no", shutterNo)
                  .build())
            .retrieve()
            .bodyToMono(Void.class);

      ShutterState onErrorShutterState = new ShutterState();
      onErrorShutterState.setDeviceName(shutter.getName());
      onErrorShutterState.setShutterNo(shutterNo);
      onErrorShutterState.setNotAvailable(true);

      Flux.merge(deferredResponse)
            .subscribe(null,
                  e -> {
                     if (open) {
                        onErrorShutterState.setState(ShutterStates.SHUTTER_CLOSED);
                     } else {
                        onErrorShutterState.setState(ShutterStates.SHUTTER_OPENED);
                     }

                     managerStatesBean.setShutterState(onErrorShutterState);
                  });
   }

   public void setUpdateFirmwareStatus(ServerStatus response, String deviceName) {
      if (!StringUtils.isEmpty(deviceName) && deviceName.equals(deviceNameToUpdateFirmware)) {
         response.setUpdateFirmware(true);
         deviceNameToUpdateFirmware = null;

         LOGGER.info("Firmware of '" + deviceName + "' will be updated");
      }

      response.setIgnoreAlarms(alarmsAreBeingIgnored);
   }

   private enum ProjectorSwitchedState {
      TURN_ON, TURN_OFF
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
   public void setManagerStatesBean(ManagerStatesBean managerStatesBean) {
      this.managerStatesBean = managerStatesBean;
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
