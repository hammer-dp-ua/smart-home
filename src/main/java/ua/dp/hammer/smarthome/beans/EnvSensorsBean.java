package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.entities.EnvSensorEntity;
import ua.dp.hammer.smarthome.entities.FanSetupEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.FanRequestInfo;
import ua.dp.hammer.smarthome.models.states.FanState;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.repositories.EnvSensorsRepository;
import ua.dp.hammer.smarthome.repositories.SettingsRepository;

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
   private int streetLightValue;
   private Timer fanStateTimer;

   private final Map<String, DeviceInfo> envSensorsStates = new ConcurrentHashMap<>();
   private final Queue<DeferredResult<List<DeviceInfo>>> envSensorsDeferredResults = new ConcurrentLinkedQueue<>();

   private Environment environment;
   private EnvSensorsRepository envSensorsRepository;
   private DevicesRepository devicesRepository;
   private ManagerStatesBean managerStatesBean;
   private SettingsRepository settingsRepository;

   @PostConstruct
   public void init() {
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

   public boolean setManualEnabledFanTime(FanRequestInfo fanRequest) {
      FanSetupEntity fanSetting = settingsRepository.getFanSettingSetup(fanRequest.getDeviceName());
      FanState currentFanState = managerStatesBean.getAllManagerStates().getFanState();
      FanState fanState = new FanState();
      boolean currentTurnedOnState = currentFanState.isTurnedOnSafe();

      fanState.setNotAvailable(false);
      fanState.setDeviceName(fanRequest.getDeviceName());

      if (fanRequest.getHumidity() >= fanSetting.getTurnOnHumidityThreshold()) {
         currentFanState.setHumidityThresholdDetected(true);
         fanState.setTurnedOn(true);

         if (fanStateTimer != null) {
            fanStateTimer.cancel();
         }
      } else if (currentFanState.isHumidityThresholdDetected()) {
         int timeoutMinutes = fanSetting.getAfterFallingThresholdWorkTimeoutMinutes();
         scheduleFanTurningOff(timeoutMinutes, fanRequest.getDeviceName());
         fanState.setMinutesRemaining(timeoutMinutes);
         fanState.setTurnedOn(true);
         currentFanState.setHumidityThresholdDetected(false);
         currentFanState.setTurningOnStateProlonged(true);
      }

      boolean toBeTurnedOn = currentFanState.isTurningOnStateProlongedSafe();
      LocalDateTime currentTime = LocalDateTime.now();
      boolean manuallyEnabled = (manualEnabledFanTime != null) &&
            currentTime.isBefore(manualEnabledFanTime.plusMinutes(fanSetting.getManuallyTurnedOnTimeoutMinutes()));
      toBeTurnedOn |= manuallyEnabled;

      if (fanRequest.isSwitchedOnManually() &&
            fanRequest.getSwitchedOnManuallySecondsLeft() != null &&
            !currentFanState.isTurningOnStateProlongedSafe()) {
         int minutesLeft = fanRequest.getSwitchedOnManuallySecondsLeft() / 60;
         int moduloSeconds = fanRequest.getSwitchedOnManuallySecondsLeft() % 60;
         if (moduloSeconds > 30) {
            // If it's 9:59 or 9:31, consider it as 10 minutes
            minutesLeft++;
         }

         fanState.setTurnedOn(true);
         fanState.setMinutesRemaining(minutesLeft);
      } else if (toBeTurnedOn != currentTurnedOnState &&
            !currentFanState.isTurningOnStateProlongedSafe()) {
         int timeoutMinutes = manuallyEnabled ? fanSetting.getManuallyTurnedOnTimeoutMinutes() : 0;

         fanState.setTurnedOn(toBeTurnedOn);
         fanState.setMinutesRemaining(timeoutMinutes);

         if (timeoutMinutes > 0) {
            if (fanStateTimer != null) {
               fanStateTimer.cancel();
            }

            scheduleFanTurningOff(timeoutMinutes, fanRequest.getDeviceName());
         }
      }

      managerStatesBean.changeFunState(fanState);
      return toBeTurnedOn;
   }

   private void scheduleFanTurningOff(int timeoutMinutes, String name) {
      fanStateTimer = new Timer();
      fanStateTimer.scheduleAtFixedRate(new TimerTask() {
          private int executionsCounter = 0;

          @Override
          public void run() {
             executionsCounter++;

             if (executionsCounter >= timeoutMinutes) {
                FanState fanState = new FanState();

                fanState.setTurnedOn(false);
                fanState.setMinutesRemaining(0);
                fanState.setDeviceName(name);
                fanState.setTurningOnStateProlonged(false);
                managerStatesBean.changeFunState(fanState);

                cancel();
             } else {
                int minutesRemaining = timeoutMinutes - executionsCounter;
                FanState fanState = new FanState();

                fanState.setTurnedOn(true);
                fanState.setMinutesRemaining(minutesRemaining);
                fanState.setDeviceName(name);
                managerStatesBean.changeFunState(fanState);
             }
          }
       },
   60 * 1000L,
   60 * 1000L);
   }

   public void setManualEnabledFanTime() {
      manualEnabledFanTime = LocalDateTime.now();
   }

   public int getManuallyTurnedOnFanTimeoutMinutes(String name) {
      return settingsRepository.getFanSettingSetup(name).getManuallyTurnedOnTimeoutMinutes();
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

   @Autowired
   public void setSettingsRepository(SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }
}
