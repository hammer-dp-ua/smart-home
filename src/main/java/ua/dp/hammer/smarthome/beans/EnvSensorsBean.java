package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.models.EnvSensor;
import ua.dp.hammer.smarthome.models.Esp8266Request;
import ua.dp.hammer.smarthome.models.ExtendedDeferredResult;

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

   private Map<String, EnvSensor> envSensorsStates = new ConcurrentHashMap<>();
   private Queue<ExtendedDeferredResult<EnvSensor>> envSensorsDeferredResults = new ConcurrentLinkedQueue<>();

   private Environment environment;

   @PostConstruct
   public void init() {
      thresholdBathroomHumidity = Float.parseFloat(environment.getRequiredProperty("thresholdBathroomHumidity"));
      manuallyTurnedOnFanTimeoutMinutes =
            Integer.parseInt(environment.getRequiredProperty("manuallyTurnedOnFanTimeoutMinutes"));
   }

   public void addEnvSensorState(Esp8266Request esp8266Request) {
      if (esp8266Request.getLight() != null) {
         streetLightValue = esp8266Request.getLight();
      }

      EnvSensor envSensor = new EnvSensor();

      envSensor.setName(esp8266Request.getDeviceName());
      envSensor.setTemperature(esp8266Request.getTemperature());
      envSensor.setHumidity(esp8266Request.getHumidity());
      envSensor.setLight(esp8266Request.getLight());
      envSensor.setGain(esp8266Request.getGain() != null ?
            Integer.parseInt(esp8266Request.getGain().trim()) : null);
      envSensor.setUptimeSeconds(esp8266Request.getUptime());
      envSensor.setErrors(esp8266Request.getErrors());
      envSensor.setFirmwareBuildTimestamp(esp8266Request.getBuildTimestamp());
      envSensor.setInfoTimestampMs(System.currentTimeMillis());
      envSensorsStates.put(esp8266Request.getDeviceName(), envSensor);
   }

   public Collection<EnvSensor> getEnvSensors() {
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
}
