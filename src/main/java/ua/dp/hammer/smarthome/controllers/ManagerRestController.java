package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.beans.EnvSensorsBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;

import static ua.dp.hammer.smarthome.utils.Utils.getClientIpAddr;

@RestController
@RequestMapping(path = "/server/manager")
public class ManagerRestController {
   private static final Logger LOGGER = LogManager.getLogger(ManagerRestController.class);

   private MainLogic mainLogic;
   private EnvSensorsBean envSensorsBean;

   @GetMapping(path = "/testAlarm")
   public ServerStatus receiveTestAlarm(HttpServletRequest request,
                                        @RequestParam(value = "alarmSource", required = false) String alarmSource) {
      String clientIp = getClientIpAddr(request);
      LOGGER.info("Test alarm: " + clientIp + ", source: " + alarmSource);

      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/updateFirmware")
   public String updateFirmware(@RequestParam("deviceToUpdateIp") String ipAddress) {
      mainLogic.setDeviceNameToUpdateFirmware(ipAddress);
      return ipAddress;
   }

   @GetMapping(path = "/switchProjectors")
   public String switchProjectorsManually(@RequestParam("switchState") String switchState,
                                          HttpServletRequest request) {
      if (LOGGER.isDebugEnabled())
      {
         Enumeration<String> headerNamesEnum = request.getHeaderNames();

         while (headerNamesEnum.hasMoreElements())
         {
            String headerName = headerNamesEnum.nextElement();
            LOGGER.debug("Header: " + headerName + ", value: " + request.getHeader(headerName));
         }
      }

      if ("turnOn".equals(switchState)) {
         mainLogic.turnProjectorsOnManually();
      } else if ("turnOff".equals(switchState)) {
         mainLogic.turnProjectorsOffManually();
      }
      return switchState;
   }

   @GetMapping(path = "/turnOnBathroomFun")
   public String turnOnBathroomFun() {
      LOGGER.info("Bathroom fan will be turned on");

      envSensorsBean.turnOnBathroomFan();
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

   @Autowired
   public void setMainLogic(MainLogic mainLogic) {
      this.mainLogic = mainLogic;
   }

   @Autowired
   public void setEnvSensorsBean(EnvSensorsBean envSensorsBean) {
      this.envSensorsBean = envSensorsBean;
   }
}
