package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.EnvSensorsBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.beans.ManagerStatesBean;
import ua.dp.hammer.smarthome.models.ProjectorStateResponse;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.states.AlarmsState;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.FanState;
import ua.dp.hammer.smarthome.models.states.ShutterStateRaw;
import ua.dp.hammer.smarthome.models.states.ShutterStates;

import javax.servlet.http.HttpServletRequest;

import static ua.dp.hammer.smarthome.controllers.ManagerRestController.CONTROLLER_PATH;
import static ua.dp.hammer.smarthome.utils.Utils.getClientIpAddr;

@RestController
@RequestMapping(path = CONTROLLER_PATH)
public class ManagerRestController {
   private static final Logger LOGGER = LogManager.getLogger(ManagerRestController.class);

   public static final String CONTROLLER_PATH = "/server/manager";
   public static final String IGNORE_ALARMS_PATH = "/ignoreAlarms";

   private MainLogic mainLogic;
   private EnvSensorsBean envSensorsBean;
   private ManagerStatesBean managerStatesBean;

   @GetMapping(path = "/testAlarm")
   public ServerStatus receiveTestAlarm(HttpServletRequest request,
                                        @RequestParam(value = "alarmSource", required = false) String alarmSource,
                                        @RequestParam(value = "deviceName", required = false) String deviceName) {
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
   public ProjectorStateResponse switchProjectorsManually(@RequestParam("switchState") String switchState,
                                                          HttpServletRequest request) {
      if ("turnOn".equals(switchState)) {
         mainLogic.turnProjectorsOnManually();
      } else if ("turnOff".equals(switchState)) {
         mainLogic.turnProjectorsOffManually();
      }
      return new ProjectorStateResponse(switchState);
   }

   @GetMapping(path = "/turnOnBathroomFan")
   public FanState turnOnBathroomFun() {
      LOGGER.info("Bathroom fan will be turned on");

      envSensorsBean.setManualEnabledFanTime();

      FanState currentState = managerStatesBean.getAllManagerStates().getFanState();
      FanState newState = new FanState();

      newState.setTurnedOn(true);
      newState.setDeviceName(currentState.getDeviceName());
      newState.setMinutesRemaining(0);
      newState.setNotAvailable(currentState.isNotAvailable());
      return newState;
   }

   /**
    *
    * @param timeout in minutes. If parameter is 0, alarms will be ignored until switched on manually,
    *                if parameter is -1 the method stops alarms ignoring
    */
   @GetMapping(path = IGNORE_ALARMS_PATH)
   public AlarmsState ignoreAlarms(@RequestParam("timeout") int timeout,
                                   @RequestParam(value = "doNotTurnOnProjectors", required = false) Boolean doNotTurnOnProjectors) {
      return mainLogic.ignoreAlarms(timeout, doNotTurnOnProjectors);
   }

   @GetMapping(path = "/shutters")
   public ShutterStateRaw doShutter(@RequestParam("name") String name,
                                    @RequestParam("no") int no,
                                    @RequestParam("open") boolean open) {
      // Closing/opening/closed/opened state will be set in ManagerStatesBean by a shutter status request
      mainLogic.doShutter(name, no, open);
      int shutterState = open ? ShutterStates.SHUTTER_OPENING.getNo() : ShutterStates.SHUTTER_CLOSING.getNo();
      ShutterStateRaw shutterStateRaw = new ShutterStateRaw();

      shutterStateRaw.setShutterNo(no);
      shutterStateRaw.setShutterState(shutterState);
      return shutterStateRaw;
   }

   @GetMapping(path = "/getCurrentStates")
   public AllManagerStates getCurrentStates() {
      return managerStatesBean.getAllManagerStates();
   }

   @GetMapping(path = "/getCurrentStatesDeferred")
   public DeferredResult<AllManagerStates> getCurrentStatesDeferred() {
      DeferredResult<AllManagerStates> deferredResult = new DeferredResult<>(300_000L);

      managerStatesBean.addStateDeferredResult(deferredResult);
      return deferredResult;
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
   public void setManagerStatesBean(ManagerStatesBean managerStatesBean) {
      this.managerStatesBean = managerStatesBean;
   }
}
