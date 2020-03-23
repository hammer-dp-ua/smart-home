package ua.dp.hammer.smarthome.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.KeepAliveStatusesBean;
import ua.dp.hammer.smarthome.models.states.keepalive.KeepAliveState;
import ua.dp.hammer.smarthome.models.states.keepalive.PhoneAwareDeviceState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/server/keepAlive")
public class KeepAliveStatusesController {

   private KeepAliveStatusesBean keepAliveStatusesBean;

   @PostMapping(path = "/getAllKeepAliveStates", consumes="application/json")
   public DeferredResult<List<KeepAliveState>> getAllKeepAliveStates(HashSet<PhoneAwareDeviceState> knownStatuses) {
      DeferredResult<List<KeepAliveState>> response = new DeferredResult<>(300_000L);

      keepAliveStatusesBean.addNewToAllKeepAliveStatesDeferred(knownStatuses, response);
      return response;
   }

   @GetMapping("/getUnavailableDevices")
   public DeferredResult<Set<String>> getUnavailableDevices() {
      return keepAliveStatusesBean.getUnavailableDevices(KeepAliveStatusesController.class);
   }

   @Autowired
   public void setKeepAliveStatusesBean(KeepAliveStatusesBean keepAliveStatusesBean) {
      this.keepAliveStatusesBean = keepAliveStatusesBean;
   }
}
