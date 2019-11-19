package ua.dp.hammer.smarthome.models;

public class EnvSensor {
   private String name;
   private Float temperature;
   private Float humidity;
   private Integer light;
   private Integer gain;
   private Long uptimeSeconds;
   private Integer errors;
   private String firmwareBuildTimestamp;
   private long infoTimestampMs;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Float getTemperature() {
      return temperature;
   }

   public void setTemperature(Float temperature) {
      this.temperature = temperature;
   }

   public Float getHumidity() {
      return humidity;
   }

   public void setHumidity(Float humidity) {
      this.humidity = humidity;
   }

   public Integer getLight() {
      return light;
   }

   public void setLight(Integer light) {
      this.light = light;
   }

   public Integer getGain() {
      return gain;
   }

   public void setGain(Integer gain) {
      this.gain = gain;
   }

   public Long getUptimeSeconds() {
      return uptimeSeconds;
   }

   public void setUptimeSeconds(Long uptimeSeconds) {
      this.uptimeSeconds = uptimeSeconds;
   }

   public Integer getErrors() {
      return errors;
   }

   public void setErrors(Integer errors) {
      this.errors = errors;
   }

   public String getFirmwareBuildTimestamp() {
      return firmwareBuildTimestamp;
   }

   public void setFirmwareBuildTimestamp(String firmwareBuildTimestamp) {
      this.firmwareBuildTimestamp = firmwareBuildTimestamp;
   }

   public long getInfoTimestampMs() {
      return infoTimestampMs;
   }

   public void setInfoTimestampMs(long infoTimestampMs) {
      this.infoTimestampMs = infoTimestampMs;
   }
}
