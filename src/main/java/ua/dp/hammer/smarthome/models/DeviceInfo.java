package ua.dp.hammer.smarthome.models;

import ua.dp.hammer.smarthome.models.states.ShutterStateRaw;

import java.util.List;

public class DeviceInfo {
   private String gain;
   private Integer errors;
   private Float humidity;
   private Float temperature;
   private Integer temperatureRaw;
   private boolean serverIsAvailable;
   private String deviceName;
   private Integer uptime;
   private String buildTimestamp;
   private Integer freeHeapSpace;
   private String resetReason;
   private String systemRestartReason;
   private int pendingConnectionErrors;
   private Short light;
   private List<ShutterStateRaw> shutterStates;

   public String getGain() {
      return gain;
   }

   public void setGain(String gain) {
      this.gain = gain;
   }

   public Integer getErrors() {
      return errors;
   }

   public void setErrors(Integer errors) {
      this.errors = errors;
   }

   public Float getHumidity() {
      return humidity;
   }

   public void setHumidity(Float humidity) {
      this.humidity = humidity;
   }

   public Float getTemperature() {
      return temperature;
   }

   public void setTemperature(Float temperature) {
      this.temperature = temperature;
   }

   public Integer getTemperatureRaw() {
      return temperatureRaw;
   }

   public void setTemperatureRaw(Integer temperatureRaw) {
      this.temperatureRaw = temperatureRaw;
   }

   public boolean isServerIsAvailable() {
      return serverIsAvailable;
   }

   public void setServerIsAvailable(boolean serverIsAvailable) {
      this.serverIsAvailable = serverIsAvailable;
   }

   public String getDeviceName() {
      return deviceName;
   }

   public void setDeviceName(String deviceName) {
      this.deviceName = deviceName;
   }

   public Integer getUptime() {
      return uptime;
   }

   public void setUptime(Integer uptime) {
      this.uptime = uptime;
   }

   public String getBuildTimestamp() {
      return buildTimestamp;
   }

   public void setBuildTimestamp(String buildTimestamp) {
      this.buildTimestamp = buildTimestamp;
   }

   public Integer getFreeHeapSpace() {
      return freeHeapSpace;
   }

   public void setFreeHeapSpace(Integer freeHeapSpace) {
      this.freeHeapSpace = freeHeapSpace;
   }

   public String getResetReason() {
      return resetReason;
   }

   public void setResetReason(String resetReason) {
      this.resetReason = resetReason;
   }

   public String getSystemRestartReason() {
      return systemRestartReason;
   }

   public void setSystemRestartReason(String systemRestartReason) {
      this.systemRestartReason = systemRestartReason;
   }

   public int getPendingConnectionErrors() {
      return pendingConnectionErrors;
   }

   public void setPendingConnectionErrors(int pendingConnectionErrors) {
      this.pendingConnectionErrors = pendingConnectionErrors;
   }

   public Short getLight() {
      return light;
   }

   public void setLight(Short light) {
      this.light = light;
   }

   public List<ShutterStateRaw> getShutterStates() {
      return shutterStates;
   }

   public void setShutterStates(List<ShutterStateRaw> shutterStates) {
      this.shutterStates = shutterStates;
   }
}
