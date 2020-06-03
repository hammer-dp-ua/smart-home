package ua.dp.hammer.smarthome.models.states;

import java.beans.Transient;

public class FanState extends CommonSate {
   private Integer minutesRemaining;
   private Boolean turnedOn;
   private boolean humidityThresholdDetected;
   private Boolean turningOnStateProlonged;

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

   public boolean isTurnedOnSafe() {
      return turnedOn != null && turnedOn;
   }

   public void setTurnedOn(boolean turnedOn) {
      this.turnedOn = turnedOn;
   }

   @Transient
   public boolean isHumidityThresholdDetected() {
      return humidityThresholdDetected;
   }

   public void setHumidityThresholdDetected(boolean humidityThresholdDetected) {
      this.humidityThresholdDetected = humidityThresholdDetected;
   }

   @Transient
   public Boolean getTurningOnStateProlonged() {
      return turningOnStateProlonged;
   }

   @Transient
   public Boolean isTurningOnStateProlongedSafe() {
      return turningOnStateProlonged != null && turningOnStateProlonged;
   }

   public void setTurningOnStateProlonged(boolean turningOnStateProlonged) {
      this.turningOnStateProlonged = turningOnStateProlonged;
   }
}
