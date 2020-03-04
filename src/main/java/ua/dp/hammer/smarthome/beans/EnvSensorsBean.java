package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.entities.EnvSensorEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.repositories.EnvSensorsRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class EnvSensorsBean {
   private static final Logger LOGGER = LogManager.getLogger(EnvSensorsBean.class);

   private LocalDateTime manualEnabledFanTime;
   private float thresholdBathroomHumidity;
   private int manuallyTurnedOnFanTimeoutMinutes;
   private int streetLightValue;
   private Timer fanStateTimer;

   private Map<String, DeviceInfo> envSensorsStates = new ConcurrentHashMap<>();
   private Queue<DeferredResult<List<DeviceInfo>>> envSensorsDeferredResults = new ConcurrentLinkedQueue<>();

   private Environment environment;
   private EnvSensorsRepository envSensorsRepository;
   private DevicesRepository devicesRepository;
   private ManagerStatesBean managerStatesBean;

   @PostConstruct
   public void init() {
      thresholdBathroomHumidity = Float.parseFloat(environment.getRequiredProperty("thresholdBathroomHumidity"));
      manuallyTurnedOnFanTimeoutMinutes =
            Integer.parseInt(environment.getRequiredProperty("manuallyTurnedOnFanTimeoutMinutes"));
   }

   public void addEnvSensorState(DeviceInfo deviceInfo) {
      if (deviceInfo.getLight() != null) {
         int lightPercentages = ((int) deviceInfo.getLight()) * 100 / 1024;

         deviceInfo.setLight((short) lightPercentages);
         streetLightValue = lightPercentages;
      }

      envSensorsStates.put(deviceInfo.getDeviceName(), deviceInfo);

      List<DeviceInfo> infoList = new ArrayList<>(envSensorsStates.size());
      infoList.addAll(envSensorsStates.values());
      while (!envSensorsDeferredResults.isEmpty()) {
         DeferredResult<List<DeviceInfo>> request = envSensorsDeferredResults.poll();
         request.setResult(infoList);
      }

      EnvSensorEntity envSensorEntity = new EnvSensorEntity();
      envSensorEntity.setTemperature(deviceInfo.getTemperature());
      envSensorEntity.setHumidity(deviceInfo.getHumidity());
      envSensorEntity.setLight(deviceInfo.getLight());
      TechnicalDeviceInfoEntity deviceInfoEntity =
            DevicesRepository.createTechnicalDeviceInfoEntity(deviceInfo);
      deviceInfoEntity.addEnvSensor(envSensorEntity);
      devicesRepository.saveTechnicalDeviceInfo(deviceInfoEntity, deviceInfo.getDeviceName());
   }

   public Collection<DeviceInfo> getEnvSensors() {
      return envSensorsStates.values();
   }

   public void addEnvSensorsDeferredResults(DeferredResult<List<DeviceInfo>> deferredResult) {
      envSensorsDeferredResults.add(deferredResult);
   }

   public boolean setBathroomFanState(float humidity) {
      boolean toBeTurnedOn = false;

      if (humidity >= thresholdBathroomHumidity) {
         toBeTurnedOn = true;
      }

      LocalDateTime currentTime = LocalDateTime.now();
      boolean manuallyEnabled = (manualEnabledFanTime != null) &&
            currentTime.isBefore(manualEnabledFanTime.plusMinutes(10));
      toBeTurnedOn |= manuallyEnabled;
      boolean isCurrentlyTurnedOn = managerStatesBean.getAllManagerStates().getFanState().isTurnedOn();

      if (toBeTurnedOn != isCurrentlyTurnedOn) {
         int minutesRemaining = (manuallyEnabled && toBeTurnedOn) ? manuallyTurnedOnFanTimeoutMinutes : 0;
         managerStatesBean.changeFunState(toBeTurnedOn, minutesRemaining);

         if (fanStateTimer != null) {
            fanStateTimer.cancel();
         }
         fanStateTimer = new Timer();
         fanStateTimer.scheduleAtFixedRate(new TimerTask() {
             private int executionsAmount = 0;

             @Override
             public void run() {
                executionsAmount++;

                if (executionsAmount >= manuallyTurnedOnFanTimeoutMinutes) {
                   managerStatesBean.changeFunState(false, 0);

                   cancel();
                } else {
                   int minutesRemaining = manuallyTurnedOnFanTimeoutMinutes - executionsAmount;
                   managerStatesBean.changeFunState(true, minutesRemaining);
                }
             }
          },
         60 * 1000L,
         60 * 1000L);
      }
      return toBeTurnedOn;
   }

   public void setBathroomFanState() {
      manualEnabledFanTime = LocalDateTime.now();
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
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }

   @Autowired
   public void setManagerStatesBean(ManagerStatesBean managerStatesBean) {
      this.managerStatesBean = managerStatesBean;
   }
}
