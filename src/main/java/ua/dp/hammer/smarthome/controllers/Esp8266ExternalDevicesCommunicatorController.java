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

   @RequestMapping(path = "/statusInfo", method = RequestMethod.POST, consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Data esp8266Data, @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      String gain = esp8266Data.getGain() != null ? esp8266Data.getGain().trim() : null;

      LOGGER.info("Gain of " + clientIp + ": " + gain + "\r\nErrors: " + esp8266Data.getErrors() + "\r\nOverrun Errors: " + esp8266Data.getUsartOverrunErrors() +
            "\r\nIdle Line Detections: " + esp8266Data.getUsartIdleLineDetections() + "\r\nNoise Detection: " + esp8266Data.getUsartNoiseDetection() +
            "\r\nFraming Errors: " + esp8266Data.getUsartFramingErrors() + "\r\nLast Error Task: " + esp8266Data.getLastErrorTask() + "\r\nUSART data: " +
            esp8266Data.getUsartData());
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);
      serverStatus.setIncludeDebugInfo(true);
      return serverStatus;
   }

   @RequestMapping(path = "/alarm", method = RequestMethod.GET)
   public ServerStatus receiveAlarm(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Alarm: " + clientIp);
      cameraBean.startVideoRecording();
      return new ServerStatus(StatusCodes.OK);
   }
}
