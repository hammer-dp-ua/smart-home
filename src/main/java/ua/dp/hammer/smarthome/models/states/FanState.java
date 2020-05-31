package ua.dp.hammer.smarthome.models.states;

public class FanState extends CommonSate {
   private Integer minutesRemaining;
   private Boolean turnedOn;
   private boolean humidityThresholdDetected;

   public FanState(){}

   public FanState(Integer minutesRemaining, Boolean turnedOn) {
      this.minutesRemaining = minutesRemaining;
      this.turnedOn = turnedOn;
   }

   public Integer getMinutesRemaining() {
      return minutesRemaining;
   }

   public void setMinutesRemaining(int minutesRemaining) {
      this.minutesRemaining = minutesRemaining;
   }

   public Boolean isTurnedOn() {
      return turnedOn;
   }

   public void setTurnedOn(boolean turnedOn) {
      this.turnedOn = turnedOn;
   }

   public boolean isHumidityThresholdDetected() {
      return humidityThresholdDetected;
   }

   public void setHumidityThresholdDetected(boolean humidityThresholdDetected) {
      this.humidityThresholdDetected = humidityThresholdDetected;
   }
}
