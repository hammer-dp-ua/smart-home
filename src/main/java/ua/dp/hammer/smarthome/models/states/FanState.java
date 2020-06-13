package ua.dp.hammer.smarthome.models.states;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.beans.Transient;

public class FanState extends CommonSate {
   private Integer minutesRemaining;
   private Boolean turnedOn;
   private boolean humidityThresholdDetected;
   private Boolean turningOnStateProlonged;

   public FanState(){}

   public Integer getMinutesRemaining() {
      return minutesRemaining;
   }

   public void setMinutesRemaining(int minutesRemaining) {
      this.minutesRemaining = minutesRemaining;
   }

   public Boolean isTurnedOn() {
      return turnedOn;
   }

   @Transient
   public boolean isTurnedOnSafe() {
      return turnedOn != null && turnedOn;
   }

   public void setTurnedOn(Boolean turnedOn) {
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

   @Override
   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }
}
