package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.beans.EnvSensorsBean;
import ua.dp.hammer.smarthome.beans.KeepAliveStatusesBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.beans.ManagerStatesBean;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.Esp8266ResetReasons;
import ua.dp.hammer.smarthome.models.FanResponse;
import ua.dp.hammer.smarthome.models.ProjectorResponse;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.states.ProjectorState;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorRestController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorRestController.class);

   private final static Pattern SINGLE_DIGIT_PATTERN = Pattern.compile("(.*?)(\\d{1})(.*)");

   private MainLogic mainLogic;
   private EnvSensorsBean envSensorsBean;
   private DevicesRepository devicesRepository;
   private ManagerStatesBean managerStatesBean;
   private KeepAliveStatusesBean keepAliveStatusesBean;

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody DeviceInfo deviceInfo) {
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(deviceInfo);
         serverStatus.setIncludeDebugInfo(true);
      }

      keepAliveStatusesBean.update(deviceInfo.getDeviceName());

      if (isEnvSensor(deviceInfo)) {
         envSensorsBean.addEnvSensorState(deviceInfo);
      } else {
         TechnicalDeviceInfoEntity deviceInfoEntity =
               DevicesRepository.createTechnicalDeviceInfoEntity(deviceInfo);
         devicesRepository.saveTechnicalDeviceInfo(deviceInfoEntity, deviceInfo.getDeviceName());
      }

      if (deviceInfo.getShutterStates() != null) {
         managerStatesBean.receiveNewShutterState(deviceInfo);
      }

      mainLogic.setUpdateFirmwareStatus(serverStatus, deviceInfo.getDeviceName());
      return serverStatus;
   }

   @PostMapping(path = "/projectorStatusInfo", consumes="application/json")
   public ProjectorResponse receiveProjectorStatusInfo(@RequestBody DeviceInfo deviceInfo) {
      ProjectorResponse projectorResponse = new ProjectorResponse(receiveStatusInfo(deviceInfo));
      ProjectorState projectorState = new ProjectorState(deviceInfo.getDeviceName());

      projectorState.setTurnedOn(managerStatesBean.isProjectorTurnedOn(deviceInfo.getDeviceName()));
      projectorState.setNotAvailable(false);
      managerStatesBean.changeProjectorState(projectorState);

      projectorResponse.setTurnOn(projectorState.isTurnedOn());
      return projectorResponse;
   }

   @GetMapping(path = "/alarm")
   public ServerStatus receiveAlarm(@RequestParam(value = "alarmSource", required = false) String alarmSource) {
      LOGGER.info("Alarm. Source: " + alarmSource);

      mainLogic.receiveAlarm(alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/falseAlarm")
   public ServerStatus receiveFalseAlarm(@RequestParam("alarmSource") String alarmSource) {
      LOGGER.debug("False alarm. Source: " + alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerActivated")
   public ServerStatus receiveImmobilizerActivation() {
      LOGGER.info("Immobilizer activated");

      mainLogic.receiveImmobilizerActivation();
      return new ServerStatus(StatusCodes.OK);
   }

   @PostMapping(path = "/bathroomFan", consumes="application/json")
   public FanResponse receiveBathroomParameters(@RequestBody DeviceInfo deviceInfo) {
      FanResponse fanResponse = new FanResponse(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(deviceInfo);
         fanResponse.setIncludeDebugInfo(true);
      }

      envSensorsBean.addEnvSensorState(deviceInfo);

      fanResponse.setTurnOn(envSensorsBean.setBathroomFanState(deviceInfo.getHumidity()));
      fanResponse.setManuallyTurnedOnTimeout(envSensorsBean.getManuallyTurnedOnFanTimeoutMinutes());
      return fanResponse;
   }

   private void writeGeneralDebugInfo(DeviceInfo deviceInfo) {
      StringBuilder infoMessage = new StringBuilder();
      String wholeResetReasonMessage = describeResetReason(deviceInfo.getResetReason());
      String gain = deviceInfo.getGain() != null ? deviceInfo.getGain().trim() : null;

      long uptimeDays = 0;
      long uptimeHours = 0;
      long uptimeMinutes = 0;
      long uptimeSeconds = 0;

      if (deviceInfo.getUptime() != null) {
         long secondsRemaining = deviceInfo.getUptime();

         uptimeDays = deviceInfo.getUptime() / 60L / 60L / 24L;
         if (uptimeDays > 0) {
            secondsRemaining -= 60L * 60L * 24L * uptimeDays;
         }
         uptimeHours = secondsRemaining / 60L / 60L;
         if (uptimeHours > 0) {
            secondsRemaining -= 60L * 60L * uptimeHours;
         }
         uptimeMinutes = secondsRemaining / 60L;
         if (uptimeMinutes > 0) {
            secondsRemaining -= 60L * uptimeMinutes;
         }
         uptimeSeconds = secondsRemaining;
      }

      infoMessage.append("Gain of '").append(deviceInfo.getDeviceName()).append("': ")
            .append(gain).append("dB");

      if (deviceInfo.getErrors() != null) {
         infoMessage.append("\nErrors: ");
         infoMessage.append(deviceInfo.getErrors());
      }
      if (deviceInfo.getPendingConnectionErrors() > 0) {
         infoMessage.append("\nPending connection errors: ");
         infoMessage.append(deviceInfo.getPendingConnectionErrors());
      }
      if (deviceInfo.getUptime() != null) {
         infoMessage.append("\nUptime: ");
         infoMessage.append(uptimeDays).append("days ");
         infoMessage.append(uptimeHours).append("hours ");
         infoMessage.append(uptimeMinutes).append("minutes ");
         infoMessage.append(uptimeSeconds).append("seconds");
      }
      if (deviceInfo.getBuildTimestamp() != null && deviceInfo.getBuildTimestamp().length() > 1) {
         infoMessage.append("\nBuild timestamp: ");
         infoMessage.append(deviceInfo.getBuildTimestamp());
      }

      if (isEnvSensor(deviceInfo)) {
         infoMessage.append("\n");
         if (deviceInfo.getTemperature() != null) {
            infoMessage.append("Temperature: ");
            infoMessage.append(deviceInfo.getTemperature());
         }
         if (deviceInfo.getTemperatureRaw() != null) {
            infoMessage.append(", Temperature raw: ");
            infoMessage.append(deviceInfo.getTemperatureRaw());
         }
         if (deviceInfo.getHumidity() != null)
         {
            infoMessage.append(", Humidity: ");
            infoMessage.append(deviceInfo.getHumidity());
         }
         if (deviceInfo.getLight() != null)
         {
            infoMessage.append(", Light sensor value: ");
            infoMessage.append(deviceInfo.getLight());
         }
      }

      if (deviceInfo.getFreeHeapSpace() != null) {
         infoMessage.append("\nFree heap: ");
         infoMessage.append(deviceInfo.getFreeHeapSpace());
      }
      if (wholeResetReasonMessage != null) {
         infoMessage.append("\nReset reason: ");
         infoMessage.append(wholeResetReasonMessage);
      }
      if (deviceInfo.getSystemRestartReason() != null && deviceInfo.getSystemRestartReason().length() > 1) {
         infoMessage.append("\nSystem restart reason: ");
         infoMessage.append(deviceInfo.getSystemRestartReason());
      }

      LOGGER.debug(infoMessage);
   }

   String describeResetReason(String wholeResetReasonMessage) {
      if (wholeResetReasonMessage == null || wholeResetReasonMessage.length() < 1) {
         return null;
      }

      Matcher matcher = SINGLE_DIGIT_PATTERN.matcher(wholeResetReasonMessage);

      if (matcher.find()) {
         String resetReasonNoString = matcher.group(2);
         int resetReasonNo = Integer.parseInt(resetReasonNoString);
         String resetReasonDescription = Esp8266ResetReasons.getReason(resetReasonNo);

         return wholeResetReasonMessage.replaceFirst(resetReasonNoString, resetReasonDescription);
      } else {
         return wholeResetReasonMessage;
      }
   }

   private boolean isEnvSensor(DeviceInfo deviceInfo) {
      return deviceInfo.getTemperature() != null || deviceInfo.getHumidity() != null ||
            deviceInfo.getLight() != null;
   }

   @Autowired
   public void setMainLogic(MainLogic mainLogic) {
      this.mainLogic = mainLogic;
   }

   @Autowired
   public void setEnvSensorsBean(EnvSensorsBean envSensorsBean) {
      this.envSensorsBean = envSensorsBean;
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }

   @Autowired
   public void setManagerStatesBean(ManagerStatesBean managerStatesBean) {
      this.managerStatesBean = managerStatesBean;
   }

   @Autowired
   public void setKeepAliveStatusesBean(KeepAliveStatusesBean keepAliveStatusesBean) {
      this.keepAliveStatusesBean = keepAliveStatusesBean;
   }
}
