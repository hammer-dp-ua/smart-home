package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import ua.dp.hammer.smarthome.models.Esp8266Data;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorController.class);

   @RequestMapping(path = "/statusInfo", method = RequestMethod.POST, consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Data esp8266Data, @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      String gain = esp8266Data.getGain() != null ? esp8266Data.getGain().trim() : null;

      LOGGER.info("Gain of " + clientIp + ": " + gain);
      return new ServerStatus(StatusCodes.OK);
   }

   @RequestMapping(path = "/alarm", method = RequestMethod.GET)
   public ServerStatus receiveAlarm() {


      return new ServerStatus(StatusCodes.OK);
   }
}
