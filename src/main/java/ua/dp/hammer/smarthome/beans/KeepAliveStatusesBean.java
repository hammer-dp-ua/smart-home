package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.entities.DeviceTypeNameEntity;
import ua.dp.hammer.smarthome.models.states.keepalive.KeepAliveState;
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

   private Map<String, LocalDateTime> devicesStatusesTimestamps = new ConcurrentHashMap<>();
   private Map<Class, Consumer<Set<String>>> subscribers = new ConcurrentHashMap<>();
   private Map<DeferredResult<List<KeepAliveState>>, Set<PhoneAwareDeviceState>> allKeepAliveStatesDeferredResponses =
         new ConcurrentHashMap<>();
   private Queue<DeferredResult<Set<String>>> unavailableDevicesDeferredResponses = new ConcurrentLinkedQueue<>();

   @Async
   public void update(String deviceName) {
      devicesStatusesTimestamps.put(deviceName, LocalDateTime.now());

      for (Map.Entry<DeferredResult<List<KeepAliveState>>, Set<PhoneAwareDeviceState>> entry :
            allKeepAliveStatesDeferredResponses.entrySet()) {
         DeferredResult<List<KeepAliveState>> deferredResult = entry.getKey();
         Set<PhoneAwareDeviceState> phoneAwareDevicesStates = entry.getValue();
         List<KeepAliveState> deferredResultList = new LinkedList<>();

         devicesRepository.getAllDeviceTypeNameEntities().forEach(deviceEntity -> {
            LocalDateTime lastStatusTime = devicesStatusesTimestamps.get(deviceEntity.getName());
            Long lastStatusTimeMs = lastStatusTime != null ?
                  lastStatusTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null;
            KeepAliveState keepAliveState = null;

            if (lastStatusTimeMs == null) {
               keepAliveState = new KeepAliveState();
               keepAliveState.setName(deviceEntity.getName());
            } else {
               PhoneAwareDeviceState phoneAwareDeviceStateToSearch =
                     new PhoneAwareDeviceState(deviceEntity.getName(), lastStatusTimeMs);

               if (!phoneAwareDevicesStates.contains(phoneAwareDeviceStateToSearch)) {
                  keepAliveState = new KeepAliveState();
                  keepAliveState.setName(deviceEntity.getName());
                  keepAliveState.setLastDeviceRequestTimestamp(lastStatusTimeMs);
                  keepAliveState.setNotAvailable(isNotAvailable(deviceEntity.getName(), lastStatusTime));
               }
            }

            if (keepAliveState != null) {
               deferredResultList.add(keepAliveState);
            }
         });
         deferredResult.setResult(deferredResultList);
      }
      allKeepAliveStatesDeferredResponses.clear();
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
                                                  DeferredResult<List<KeepAliveState>> response) {
      allKeepAliveStatesDeferredResponses.put(response, knownStatuses);
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
