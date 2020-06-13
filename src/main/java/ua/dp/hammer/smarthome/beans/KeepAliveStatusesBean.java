package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.entities.DeviceTypeNameEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.states.keepalive.DeviceTechInfo;
import ua.dp.hammer.smarthome.models.states.keepalive.PhoneAwareDeviceState;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Component
public class KeepAliveStatusesBean {
   private static final Logger LOGGER = LogManager.getLogger(KeepAliveStatusesBean.class);

   private DevicesRepository devicesRepository;

   private final Map<String, LocalDateTime> devicesStatusesTimestamps = new ConcurrentHashMap<>();
   private final Map<String, DeviceInfo> devicesInfo = new ConcurrentHashMap<>();
   private final Map<Class, Consumer<Set<String>>> subscribers = new ConcurrentHashMap<>();
   private final Map<DeferredResult<List<DeviceTechInfo>>, Set<PhoneAwareDeviceState>> devicesTechInfoDeferredResponses =
         new ConcurrentHashMap<>();
   private final Queue<DeferredResult<Set<String>>> unavailableDevicesDeferredResponses = new ConcurrentLinkedQueue<>();

   //@Async
   public void update(DeviceInfo deviceInfo) {
      devicesStatusesTimestamps.put(deviceInfo.getDeviceName(), LocalDateTime.now());
      devicesInfo.put(deviceInfo.getDeviceName(), deviceInfo);
      updateDevicesTechInfoDeferred();
   }

   private void updateDevicesTechInfoDeferred() {
      for (Map.Entry<DeferredResult<List<DeviceTechInfo>>, Set<PhoneAwareDeviceState>> entry :
            devicesTechInfoDeferredResponses.entrySet()) {
         DeferredResult<List<DeviceTechInfo>> deferredResult = entry.getKey();
         Set<PhoneAwareDeviceState> phoneAwareDevicesStates = entry.getValue();
         List<DeviceTechInfo> deferredResultList = new LinkedList<>();

         devicesRepository.getAllDeviceTypeNameEntities().forEach(deviceEntity -> {
            LocalDateTime lastStatusTime = devicesStatusesTimestamps.get(deviceEntity.getName());
            Long lastStatusTimeMs = lastStatusTime != null ?
                  lastStatusTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
            DeviceTechInfo deviceTechInfo = null;

            if (lastStatusTimeMs == null) {
               deviceTechInfo = new DeviceTechInfo();
               deviceTechInfo.setDeviceName(deviceEntity.getName());
               deviceTechInfo.setDeviceType(deviceEntity.getType().getType());
            } else {
               PhoneAwareDeviceState phoneAwareDeviceStateToSearch =
                     new PhoneAwareDeviceState(deviceEntity.getName(), lastStatusTimeMs);
               boolean phoneInfoOutdated = !phoneAwareDevicesStates.contains(phoneAwareDeviceStateToSearch);
               boolean notAvailable = isNotAvailable(deviceEntity.getName(), lastStatusTime);

               if (phoneInfoOutdated || notAvailable) {
                  deviceTechInfo = new DeviceTechInfo();
                  deviceTechInfo.setDeviceName(deviceEntity.getName());
                  deviceTechInfo.setLastDeviceRequestTimestamp(lastStatusTimeMs);
                  deviceTechInfo.setNotAvailable(notAvailable);
                  deviceTechInfo.setDeviceType(deviceEntity.getType().getType());

                  DeviceInfo deviceInfo = devicesInfo.get(deviceEntity.getName());
                  if (deviceInfo != null) {
                     deviceTechInfo.setUptime(deviceInfo.getUptime());
                     deviceTechInfo.setBuildTimestamp(deviceInfo.getBuildTimestamp());
                  }
               }
            }

            if (deviceTechInfo != null) {
               // Response will contain only required elements
               deferredResultList.add(deviceTechInfo);
            }
         });
         deferredResult.setResult(deferredResultList);
      }
      devicesTechInfoDeferredResponses.clear();
   }

   public DeferredResult<Set<String>> getUnavailableDevices(Class subscriber) {
      DeferredResult<Set<String>> deferredResponse = new DeferredResult<>();

      unavailableDevicesDeferredResponses.add(deferredResponse);
      addSubscriber(subscriber, (unavailableDeviceNames) -> {
         deleteSubscriber(subscriber);
         unavailableDevicesDeferredResponses.forEach(response -> response.setResult(unavailableDeviceNames));
         unavailableDevicesDeferredResponses.clear();
      });
      return deferredResponse;
   }

   public void addSubscriber(Class subscriber, Consumer<Set<String>> executionBlock) {
      subscribers.put(subscriber, executionBlock);
   }

   public void deleteSubscriber(Class subscriber) {
      subscribers.remove(subscriber);
   }

   public void addNewToAllKeepAliveStatesDeferred(Set<PhoneAwareDeviceState> knownStatuses,
                                                  DeferredResult<List<DeviceTechInfo>> response) {
      devicesTechInfoDeferredResponses.put(response, knownStatuses);
   }

   @Scheduled(fixedDelay=5000)
   public void checkStatuses() {
      Set<String> notAvailableDevices = new HashSet<>();

      for (Map.Entry<String, LocalDateTime> entry : devicesStatusesTimestamps.entrySet()) {
         String deviceName = entry.getKey();
         LocalDateTime lastStatusTime = entry.getValue();

         if (isNotAvailable(deviceName, lastStatusTime)) {
            notAvailableDevices.add(deviceName);
         }
      }

      if (!notAvailableDevices.isEmpty()) {
         subscribers.values().forEach(subscriber -> subscriber.accept(notAvailableDevices));
      }
   }

   private boolean isNotAvailable(String name, LocalDateTime lastStatusTime) {
      DeviceTypeNameEntity deviceEntity = devicesRepository.getDeviceTypeNameEntity(name);

      if (deviceEntity == null) {
         LOGGER.warn("'" + name + "'" + " device hasn't been found in repository");
         return false;
      }

      int deviceKeepAliveIntervalSec = deviceEntity.getType().getKeepAliveIntervalSec();
      int deviceKeepAliveThresholdSec = deviceKeepAliveIntervalSec + (deviceKeepAliveIntervalSec / 2);
      LocalDateTime currentTime = LocalDateTime.now();

      return currentTime.isAfter(lastStatusTime.plusSeconds(deviceKeepAliveThresholdSec));
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
