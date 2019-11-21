package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ua.dp.hammer.smarthome.beans.EnvSensorsBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorRestController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorRestController.class);

   private final static Pattern SINGLE_DIGIT_PATTERN = Pattern.compile("(.*?)(\\d{1})(.*)");

   private MainLogic mainLogic;
   private EnvSensorsBean envSensorsBean;

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Request esp8266Request) {
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(esp8266Request);
         serverStatus.setIncludeDebugInfo(true);
      }

      if (isEnvSensor(esp8266Request)) {
         envSensorsBean.addEnvSensorState(esp8266Request);
      }
      mainLogic.setUpdateFirmwareStatus(serverStatus, esp8266Request.getDeviceName());
      return serverStatus;
   }

   @GetMapping(path = "/alarm")
   public ServerStatus receiveAlarm(@RequestParam(value = "alarmSource", required = false) String alarmSource) {
      LOGGER.info("Alarm. Source: " + alarmSource);

      mainLogic.receiveAlarm(alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/falseAlarm")
   public ServerStatus receiveFalseAlarm(@RequestParam("alarmSource") String alarmSource) {
      LOGGER.info("False alarm. Source: " + alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerActivated")
   public ServerStatus receiveImmobilizerActivation(@RequestBody Esp8266Request esp8266Request) {
      LOGGER.info("Immobilizer activated: '" + esp8266Request.getDeviceName() + "'");

      mainLogic.receiveImmobilizerActivation();
      return new ServerStatus(StatusCodes.OK);
   }

   @PostMapping(path = "/bathroomFan", consumes="application/json")
   public FanResponse receiveBathroomParameters(@RequestBody Esp8266Request esp8266Request) {
      FanResponse fanResponse = new FanResponse(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(esp8266Request);
         fanResponse.setIncludeDebugInfo(true);
      }

      envSensorsBean.addEnvSensorState(esp8266Request);

      fanResponse.setTurnOn(envSensorsBean.getBathroomFanState(esp8266Request.getHumidity()));
      fanResponse.setManuallyTurnedOnTimeout(envSensorsBean.getManuallyTurnedOnFanTimeoutMinutes());
      return fanResponse;
   }

   private void writeGeneralDebugInfo(Esp8266Request esp8266Request) {
      StringBuilder infoMessage = new StringBuilder();
      String wholeResetReasonMessage = describeResetReason(esp8266Request.getResetReason());
      String gain = esp8266Request.getGain() != null ? esp8266Request.getGain().trim() : null;

      long uptimeDays = 0;
      long uptimeHours = 0;
      long uptimeMinutes = 0;
      long uptimeSeconds = 0;

      if (esp8266Request.getUptime() > 0) {
         long secondsRemaining = esp8266Request.getUptime();

         uptimeDays = esp8266Request.getUptime() / 60L / 60L / 24L;
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

      infoMessage.append("Gain of '").append(esp8266Request.getDeviceName()).append("': ")
            .append(gain).append("dB");

      if (esp8266Request.getErrors() > 0) {
         infoMessage.append("\nErrors: ");
         infoMessage.append(esp8266Request.getErrors());
      }
      if (esp8266Request.getPendingConnectionErrors() > 0) {
         infoMessage.append("\nPending connection errors: ");
         infoMessage.append(esp8266Request.getPendingConnectionErrors());
      }
      if (esp8266Request.getUptime() > 0) {
         infoMessage.append("\nUptime: ");
         infoMessage.append(uptimeDays).append("days ");
         infoMessage.append(uptimeHours).append("hours ");
         infoMessage.append(uptimeMinutes).append("minutes ");
         infoMessage.append(uptimeSeconds).append("seconds");
      }
      if (esp8266Request.getBuildTimestamp() != null && esp8266Request.getBuildTimestamp().length() > 1) {
         infoMessage.append("\nBuild timestamp: ");
         infoMessage.append(esp8266Request.getBuildTimestamp());
      }

      if (isEnvSensor(esp8266Request))
      {
         infoMessage.append("\n");
         if (esp8266Request.getTemperature() != null)
         {
            infoMessage.append("Temperature: ");
            infoMessage.append(esp8266Request.getTemperature());
         }
         if (esp8266Request.getHumidity() != null)
         {
            infoMessage.append(", Humidity: ");
            infoMessage.append(esp8266Request.getHumidity());
         }
         if (esp8266Request.getLight() != null)
         {
            infoMessage.append(", Light sensor value: ");
            infoMessage.append(esp8266Request.getLight());
         }
      }

      if (esp8266Request.getFreeHeapSpace() > 0) {
         infoMessage.append("\nFree heap: ");
         infoMessage.append(esp8266Request.getFreeHeapSpace());
      }
      if (wholeResetReasonMessage != null) {
         infoMessage.append("\nReset reason: ");
         infoMessage.append(wholeResetReasonMessage);
      }
      if (esp8266Request.getSystemRestartReason() != null && esp8266Request.getSystemRestartReason().length() > 1) {
         infoMessage.append("\nSystem restart reason: ");
         infoMessage.append(esp8266Request.getSystemRestartReason());
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

   private boolean isEnvSensor(Esp8266Request esp8266Request) {
      return esp8266Request.getTemperature() != null || esp8266Request.getHumidity() != null ||
            esp8266Request.getLight() != null;
   }

   @Autowired
   public void setMainLogic(MainLogic mainLogic) {
      this.mainLogic = mainLogic;
   }

   @Autowired
   public void setEnvSensorsBean(EnvSensorsBean envSensorsBean) {
      this.envSensorsBean = envSensorsBean;
   }
}
