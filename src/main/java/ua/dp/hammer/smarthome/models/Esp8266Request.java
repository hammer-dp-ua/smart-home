package ua.dp.hammer.smarthome.models;

public class Esp8266Request {
   private String gain;
   private int errors;
   private float humidity;
   private float temperature;
   private boolean serverIsAvailable;
   private String deviceName;
   private long uptime;
   private String buildTimestamp;
   private int freeHeapSpace;
   private String resetReason;
   private int pendingConnectionErrors;

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

   public float getHumidity() {
      return humidity;
   }

   public void setHumidity(float humidity) {
      this.humidity = humidity;
   }

   public float getTemperature() {
      return temperature;
   }

   public void setTemperature(float temperature) {
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

   public long getUptime() {
      return uptime;
   }

   public void setUptime(long uptime) {
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

   public int getPendingConnectionErrors() {
      return pendingConnectionErrors;
   }

   public void setPendingConnectionErrors(int pendingConnectionErrors) {
      this.pendingConnectionErrors = pendingConnectionErrors;
   }
}
