package ua.dp.hammer.smarthome.models;

public class DeviceInfo {
   private String gain;
   private int errors;
   private Float humidity;
   private Float temperature;
   private boolean serverIsAvailable;
   private String deviceName;
   private int uptime;
   private String buildTimestamp;
   private int freeHeapSpace;
   private String resetReason;
   private String systemRestartReason;
   private int pendingConnectionErrors;
   private Short light;

   public String getGain() {
      return gain;
   }

   public void setGain(String gain) {
      this.gain = gain;
   }

   public int getErrors() {
      return errors;
   }

   public void setErrors(int errors) {
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

   public int getUptime() {
      return uptime;
   }

   public void setUptime(int uptime) {
      this.uptime = uptime;
   }

   public String getBuildTimestamp() {
      return buildTimestamp;
   }

   public void setBuildTimestamp(String buildTimestamp) {
      this.buildTimestamp = buildTimestamp;
   }

   public int getFreeHeapSpace() {
      return freeHeapSpace;
   }

   public void setFreeHeapSpace(int freeHeapSpace) {
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
}
