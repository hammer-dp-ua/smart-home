package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.AlarmsMonitorBean;
import ua.dp.hammer.smarthome.models.alarms.StreetMotionDetectors;

import static ua.dp.hammer.smarthome.controllers.AlarmsMonitorRestController.CONTROLLER_PATH;

@RestController
@RequestMapping(path = CONTROLLER_PATH)
public class AlarmsMonitorRestController {
   private static final Logger LOGGER = LogManager.getLogger(AlarmsMonitorRestController.class);

   public static final String CONTROLLER_PATH = "/server/alarmsMonitor";
   public static final String GET_CURRENT_STATES_DEFERRED = "/getCurrentStatesDeferred";

   private AlarmsMonitorBean alarmsMonitorBean;

   @GetMapping(path = GET_CURRENT_STATES_DEFERRED)
   public DeferredResult<StreetMotionDetectors> getCurrentStatesDeferred() {
      DeferredResult<StreetMotionDetectors> deferredResult = new DeferredResult<>(300_000L);

      alarmsMonitorBean.addStateDeferredResult(deferredResult);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("New DeferredResult added for current states");
      }
      return deferredResult;
   }

   @Autowired
   public void setAlarmsBean(AlarmsMonitorBean alarmsMonitorBean) {
      this.alarmsMonitorBean = alarmsMonitorBean;
   }
}
