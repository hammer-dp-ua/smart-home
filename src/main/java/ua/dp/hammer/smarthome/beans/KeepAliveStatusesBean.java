package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.entities.DeviceTypeNameEntity;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class KeepAliveStatusesBean {
   private static final Logger LOGGER = LogManager.getLogger(KeepAliveStatusesBean.class);

   private DevicesRepository devicesRepository;

   private Map<String, LocalDateTime> devicesStatusesTimestamps = new ConcurrentHashMap<>();
   private Map<Class, Consumer<String>> subscribers = new ConcurrentHashMap<>();

   public void update(String deviceName) {
      devicesStatusesTimestamps.put(deviceName, LocalDateTime.now());
   }

   public void addSubscriber(Class subscriber, Consumer<String> executionBlock) {
      subscribers.put(subscriber, executionBlock);
   }

   public void deleteSubscriber(Class subscriber) {
      subscribers.remove(subscriber);
   }

   @Scheduled(fixedDelay=5000)
   public void checkStatuses() {
      for (Map.Entry<String, LocalDateTime> entry : devicesStatusesTimestamps.entrySet()) {
         String deviceName = entry.getKey();
         LocalDateTime lastStatusTime = entry.getValue();
         DeviceTypeNameEntity deviceEntity = devicesRepository.getDeviceTypeNameEntity(deviceName);

         if (deviceEntity == null) {
            LOGGER.warn("'" + deviceName + "'" + " device hasn't been found in repository");
            continue;
         }

         int deviceKeepAliveIntervalSec = deviceEntity.getType().getKeepAliveIntervalSec();
         int deviceKeepAliveThresholdSec = deviceKeepAliveIntervalSec + (deviceKeepAliveIntervalSec / 2);
         LocalDateTime currentTime = LocalDateTime.now();

         if (currentTime.isAfter(lastStatusTime.plusSeconds(deviceKeepAliveThresholdSec))) {
            subscribers.values().forEach(s -> s.accept(deviceName));
         }
      }
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
