package ua.dp.hammer.smarthome.models.states;

public class FanState extends TurnState {
   private int minutesRemaining;

   public FanState(){}

   public FanState(boolean turnedOn, int minutesRemaining) {
      super.setTurnedOn(turnedOn);
      this.minutesRemaining = minutesRemaining;
   }

   public int getMinutesRemaining() {
      return minutesRemaining;
   }

   public void setMinutesRemaining(int minutesRemaining) {
      this.minutesRemaining = minutesRemaining;
   }
}
