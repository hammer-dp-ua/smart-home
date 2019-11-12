package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorController.class);

   private final static Pattern SINGLE_DIGIT_PATTERN = Pattern.compile("(.*?)(\\d{1})(.*)");

   private int manuallyTurnedOnFanTimeoutMinutes;

   private Environment environment;
   private MainLogic mainLogic;

   @PostConstruct
   public void init() {
      manuallyTurnedOnFanTimeoutMinutes =
            Integer.parseInt(environment.getRequiredProperty("manuallyTurnedOnFanTimeoutMinutes"));
   }

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Request esp8266Request,
                                         HttpServletRequest request) {
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);
      String clientIp = request.getRemoteAddr();

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
         serverStatus.setIncludeDebugInfo(true);
      }
      mainLogic.setUpdateStatus(serverStatus, clientIp);

      if (esp8266Request.getLight() != null) {
         mainLogic.setStreetLightValue(esp8266Request.getLight());
      }
      return serverStatus;
   }

   @GetMapping(path = "/alarm")
   public ServerStatus receiveAlarm(HttpServletRequest request,
                                    @RequestParam(value = "alarmSource", required = false) String alarmSource) {
      String clientIp = request.getRemoteAddr();
      LOGGER.info("Alarm: " + clientIp + ", source: " + alarmSource);

      mainLogic.receiveAlarm(alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/testAlarm")
   public ServerStatus receiveTestAlarm(HttpServletRequest request,
                                        @RequestParam(value = "alarmSource", required = false) String alarmSource) {
      String clientIp = request.getRemoteAddr();
      LOGGER.info("Test alarm: " + clientIp + ", source: " + alarmSource);

      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/falseAlarm")
   public ServerStatus receiveFalseAlarm(HttpServletRequest request,
                                         @RequestParam("alarmSource") String alarmSource) {
      String clientIp = request.getRemoteAddr();
      LOGGER.info("False alarm: " + clientIp + ", source: " + alarmSource);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerActivated")
   public ServerStatus receiveImmobilizerActivation(HttpServletRequest request) {
      String clientIp = request.getRemoteAddr();
      LOGGER.info("Immobilizer activated: " + clientIp);

      mainLogic.receiveImmobilizerActivation();
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/updateFirmware")
   public String updateFirmware(@RequestParam("deviceToUpdateIp") String ipAddress) {
      mainLogic.setIpAddressToUpdateFirmware(ipAddress);
      return ipAddress;
   }

   @GetMapping(path = "/switchProjectorsManually")
   public String switchProjectorsManually(@RequestParam("switchState") String switchState) {
      if ("turnOn".equals(switchState)) {
         mainLogic.turnProjectorsOnManually();
      } else if ("turnOff".equals(switchState)) {
         mainLogic.turnProjectorsOffManually();
      }
      return switchState;
   }

   @PostMapping(path = "/bathroomFan", consumes="application/json")
   public FanResponse receiveBathroomParameters(@RequestBody Esp8266Request esp8266Request,
                                                HttpServletRequest request) {
      String clientIp = request.getRemoteAddr();
      FanResponse fanResponse = new FanResponse(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
         LOGGER.debug("Bathroom info. Humidity: " + esp8266Request.getHumidity() + "; Temperature: " + esp8266Request.getTemperature());
         fanResponse.setIncludeDebugInfo(true);
      }

      fanResponse.setTurnOn(mainLogic.getBathroomFanState(esp8266Request.getHumidity()));
      fanResponse.setManuallyTurnedOnTimeout(manuallyTurnedOnFanTimeoutMinutes);
      return fanResponse;
   }

   @GetMapping(path = "/turnOnBathroomFun")
   public String turnOnBathroomFun() {
      LOGGER.info("Bathroom fan will be turned on");

      mainLogic.turnOnBathroomFan();
      return "OK";
   }

   /**
    *
    * @param timeout in minutes. If parameter is 0, alarms will be ignored until switched on manually,
    *                if parameter is -1 the method stops alarms ignoring
    */
   @GetMapping(path = "/ignoreAlarms")
   public String ignoreAlarms(@RequestParam("timeout") int timeout) {
      return mainLogic.ignoreAlarms(timeout);
   }

   private void writeGeneralDebugInfo(String clientIp, Esp8266Request esp8266Request) {
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

      infoMessage.append("Gain of ").append(clientIp).append(" (").append(esp8266Request.getDeviceName())
            .append("): ").append(gain).append("dB");

      if (esp8266Request.getErrors() > 0) {
         infoMessage.append("\r\nErrors: ");
         infoMessage.append(esp8266Request.getErrors());
      }
      if (esp8266Request.getPendingConnectionErrors() > 0) {
         infoMessage.append("\r\nPending connection errors: ");
         infoMessage.append(esp8266Request.getPendingConnectionErrors());
      }
      if (esp8266Request.getUptime() > 0) {
         infoMessage.append("\r\nUptime: ");
         infoMessage.append(uptimeDays).append("days ");
         infoMessage.append(uptimeHours).append("hours ");
         infoMessage.append(uptimeMinutes).append("minutes ");
         infoMessage.append(uptimeSeconds).append("seconds");
      }
      if (esp8266Request.getBuildTimestamp() != null && esp8266Request.getBuildTimestamp().length() > 1) {
         infoMessage.append("\r\nBuild timestamp: ");
         infoMessage.append(esp8266Request.getBuildTimestamp());
      }
      if (esp8266Request.getTemperature() != null || esp8266Request.getHumidity() != null || esp8266Request.getLight() != null)
      {
         infoMessage.append("\r\n");
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
         infoMessage.append("\r\nFree heap: ");
         infoMessage.append(esp8266Request.getFreeHeapSpace());
      }
      if (wholeResetReasonMessage != null) {
         infoMessage.append("\r\nReset reason: ");
         infoMessage.append(wholeResetReasonMessage);
      }
      if (esp8266Request.getSystemRestartReason() != null && esp8266Request.getSystemRestartReason().length() > 1) {
         infoMessage.append("\r\nSystem restart reason: ");
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

   @Autowired
   public void setEnvironment(Environment environment) {
      this.environment = environment;
   }

   @Autowired
   public void setMainLogic(MainLogic mainLogic) {
      this.mainLogic = mainLogic;
   }
}
