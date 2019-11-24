package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.entities.EnvSensorEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.ExtendedDeferredResult;
import ua.dp.hammer.smarthome.repositories.CommonDevicesRepository;
import ua.dp.hammer.smarthome.repositories.EnvSensorsRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class EnvSensorsBean {
   private static final Logger LOGGER = LogManager.getLogger(EnvSensorsBean.class);

   private LocalDateTime thresholdHumidityStartTime;
   private float thresholdBathroomHumidity;
   private int manuallyTurnedOnFanTimeoutMinutes;
   private int streetLightValue;

   private Map<String, DeviceInfo> envSensorsStates = new ConcurrentHashMap<>();
   private Queue<ExtendedDeferredResult<DeviceInfo>> envSensorsDeferredResults = new ConcurrentLinkedQueue<>();

   private Environment environment;
   private EnvSensorsRepository envSensorsRepository;
   private CommonDevicesRepository commonDevicesRepository;

   @PostConstruct
   public void init() {
      thresholdBathroomHumidity = Float.parseFloat(environment.getRequiredProperty("thresholdBathroomHumidity"));
      manuallyTurnedOnFanTimeoutMinutes =
            Integer.parseInt(environment.getRequiredProperty("manuallyTurnedOnFanTimeoutMinutes"));
   }

   public void addEnvSensorState(DeviceInfo deviceInfo) {
      if (deviceInfo.getLight() != null) {
         streetLightValue = deviceInfo.getLight();
      }

      envSensorsStates.put(deviceInfo.getDeviceName(), deviceInfo);

      EnvSensorEntity envSensorEntity = new EnvSensorEntity();
      envSensorEntity.setTemperature(deviceInfo.getTemperature());
      envSensorEntity.setHumidity(deviceInfo.getHumidity());
      envSensorEntity.setLight(deviceInfo.getLight());
      TechnicalDeviceInfoEntity deviceInfoEntity =
            CommonDevicesRepository.createTechnicalDeviceInfoEntity(deviceInfo);
      deviceInfoEntity.addEnvSensor(envSensorEntity);
      commonDevicesRepository.saveTechnicalDeviceInfo(deviceInfoEntity, deviceInfo.getDeviceName());
   }

   public Collection<DeviceInfo> getEnvSensors() {
      return envSensorsStates.values();
   }

   public boolean getBathroomFanState(float humidity) {
      if (humidity >= thresholdBathroomHumidity) {
         thresholdHumidityStartTime = LocalDateTime.now();
      }

      LocalDateTime currentTime = LocalDateTime.now();
      return thresholdHumidityStartTime != null && currentTime.isBefore(thresholdHumidityStartTime.plusMinutes(10));
   }

   public void turnOnBathroomFan() {
      thresholdHumidityStartTime = LocalDateTime.now();
   }

   public int getManuallyTurnedOnFanTimeoutMinutes() {
      return manuallyTurnedOnFanTimeoutMinutes;
   }

   public int getStreetLightValue() {
      return streetLightValue;
   }

   @Autowired
   public void setEnvironment(Environment environment) {
      this.environment = environment;
   }

   //@Autowired
   public void setEnvSensorsRepository(EnvSensorsRepository envSensorsRepository) {
      this.envSensorsRepository = envSensorsRepository;
   }

   @Autowired
   public void setCommonDevicesRepository(CommonDevicesRepository commonDevicesRepository) {
      this.commonDevicesRepository = commonDevicesRepository;
   }
}
