package ua.dp.hammer.smarthome.models;

public class FanSettingsInfo {
   private String name;
   private Float turnOnHumidityThreshold;
   private Integer manuallyTurnedOnTimeoutMinutes;
   private Integer afterFallingThresholdWorkTimeoutMinutes;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Float getTurnOnHumidityThreshold() {
      return turnOnHumidityThreshold;
   }

   public void setTurnOnHumidityThreshold(Float turnOnHumidityThreshold) {
      this.turnOnHumidityThreshold = turnOnHumidityThreshold;
   }

   public Integer getManuallyTurnedOnTimeoutMinutes() {
      return manuallyTurnedOnTimeoutMinutes;
   }

   public void setManuallyTurnedOnTimeoutMinutes(Integer manuallyTurnedOnTimeoutMinutes) {
      this.manuallyTurnedOnTimeoutMinutes = manuallyTurnedOnTimeoutMinutes;
   }

   public Integer getAfterFallingThresholdWorkTimeoutMinutes() {
      return afterFallingThresholdWorkTimeoutMinutes;
   }

   public void setAfterFallingThresholdWorkTimeoutMinutes(Integer afterFallingThresholdWorkTimeoutMinutes) {
      this.afterFallingThresholdWorkTimeoutMinutes = afterFallingThresholdWorkTimeoutMinutes;
   }
}
