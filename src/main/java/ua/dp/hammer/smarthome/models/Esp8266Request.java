package ua.dp.hammer.smarthome.models;

public class Esp8266Request {
   private String gain;
   private boolean debugInfoIncluded;
   private int errors;
   private int usartOverrunErrors;
   private int usartIdleLineDetections;
   private int usartNoiseDetection;
   private int usartFramingErrors;
   private int lastErrorTask;
   private String usartData;
   private long timeStamp;
   private float humidity;
   private float temperature;
   private boolean serverIsAvailable;
   private String deviceName;

   public String getGain() {
      return gain;
   }

   public void setGain(String gain) {
      this.gain = gain;
   }

   public boolean isDebugInfoIncluded() {
      return debugInfoIncluded;
   }

   public void setDebugInfoIncluded(boolean debugInfoIncluded) {
      this.debugInfoIncluded = debugInfoIncluded;
   }

   public int getErrors() {
      return errors;
   }

   public void setErrors(int errors) {
      this.errors = errors;
   }

   public int getUsartOverrunErrors() {
      return usartOverrunErrors;
   }

   public void setUsartOverrunErrors(int usartOverrunErrors) {
      this.usartOverrunErrors = usartOverrunErrors;
   }

   public int getUsartIdleLineDetections() {
      return usartIdleLineDetections;
   }

   public void setUsartIdleLineDetections(int usartIdleLineDetections) {
      this.usartIdleLineDetections = usartIdleLineDetections;
   }

   public int getUsartNoiseDetection() {
      return usartNoiseDetection;
   }

   public void setUsartNoiseDetection(int usartNoiseDetection) {
      this.usartNoiseDetection = usartNoiseDetection;
   }

   public int getUsartFramingErrors() {
      return usartFramingErrors;
   }

   public void setUsartFramingErrors(int usartFramingErrors) {
      this.usartFramingErrors = usartFramingErrors;
   }

   public int getLastErrorTask() {
      return lastErrorTask;
   }

   public void setLastErrorTask(int lastErrorTask) {
      this.lastErrorTask = lastErrorTask;
   }

   public String getUsartData() {
      return usartData;
   }

   public void setUsartData(String usartData) {
      this.usartData = usartData;
   }

   public long getTimeStamp() {
      return timeStamp;
   }

   public void setTimeStamp(long timeStamp) {
      this.timeStamp = timeStamp;
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
}
