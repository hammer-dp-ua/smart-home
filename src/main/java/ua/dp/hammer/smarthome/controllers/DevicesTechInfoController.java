package ua.dp.hammer.smarthome.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.KeepAliveStatusesBean;
import ua.dp.hammer.smarthome.models.states.keepalive.DeviceTechInfo;
import ua.dp.hammer.smarthome.models.states.keepalive.PhoneAwareDeviceState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/server/devicesTechInfo")
public class DevicesTechInfoController {

   private KeepAliveStatusesBean keepAliveStatusesBean;

   @PostMapping(path = "/getAllDevicesTechInfoStates", consumes="application/json")
   public DeferredResult<List<DeviceTechInfo>> getAllDevicesTechInfoStates(HashSet<PhoneAwareDeviceState> knownStatuses) {
      DeferredResult<List<DeviceTechInfo>> response = new DeferredResult<>(300_000L);

      keepAliveStatusesBean.addNewToAllKeepAliveStatesDeferred(knownStatuses, response);
      return response;
   }

   @GetMapping("/getUnavailableDevices")
   public DeferredResult<Set<String>> getUnavailableDevices() {
      return keepAliveStatusesBean.getUnavailableDevices(DevicesTechInfoController.class);
   }

   @Autowired
   public void setKeepAliveStatusesBean(KeepAliveStatusesBean keepAliveStatusesBean) {
      this.keepAliveStatusesBean = keepAliveStatusesBean;
   }
}
