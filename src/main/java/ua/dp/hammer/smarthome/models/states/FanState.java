package ua.dp.hammer.smarthome.models.states;

public class FanState extends CommonSate {
   private int minutesRemaining;
   private boolean turnedOn;

   public FanState(){}

   public FanState(boolean turnedOn, int minutesRemaining) {
      this.turnedOn = turnedOn;
      this.minutesRemaining = minutesRemaining;
   }

   public int getMinutesRemaining() {
      return minutesRemaining;
   }

   public void setMinutesRemaining(int minutesRemaining) {
      this.minutesRemaining = minutesRemaining;
   }

   public boolean isTurnedOn() {
      return turnedOn;
   }

   public void setTurnedOn(boolean turnedOn) {
      this.turnedOn = turnedOn;
   }
}
