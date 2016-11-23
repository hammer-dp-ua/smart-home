package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ua.dp.hammer.smarthome.beans.CameraBean;
import ua.dp.hammer.smarthome.models.Esp8266Data;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorController.class);

   @Autowired
   private CameraBean cameraBean;

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Data esp8266Data, @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      String gain = esp8266Data.getGain() != null ? esp8266Data.getGain().trim() : null;
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Gain of " + clientIp + ": " + gain +
               "\r\nErrors: " + esp8266Data.getErrors() +
               "\r\nOverrun Errors: " + esp8266Data.getUsartOverrunErrors() +
               "\r\nIdle Line Detections: " + esp8266Data.getUsartIdleLineDetections() +
               "\r\nNoise Detection: " + esp8266Data.getUsartNoiseDetection() +
               "\r\nFraming Errors: " + esp8266Data.getUsartFramingErrors() +
               "\r\nLast Error Task: " + esp8266Data.getLastErrorTask() +
               "\r\nUSART data: " + esp8266Data.getUsartData());
         serverStatus.setIncludeDebugInfo(true);
      }
      return serverStatus;
   }

   @GetMapping(path = "/alarm")
   public ServerStatus receiveAlarm(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Alarm: " + clientIp);
      cameraBean.startVideoRecording();
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/falseAlarm")
   public ServerStatus receiveFalseAlarm(@RequestHeader("X-FORWARDED-FOR") String clientIp,
                                         @RequestParam("alarmSource") String alarmSource) {
      LOGGER.info("False alarm from " + alarmSource + ": " + clientIp);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerActivated")
   public ServerStatus receiveImmobilizerActivation(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Immobilizer activated: " + clientIp);
      return new ServerStatus(StatusCodes.OK);
   }
}
