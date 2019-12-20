package ua.dp.hammer.smarthome.models.states;

public class AlarmsState {
   private boolean ignoring;
   private int minutesRemaining;

   public AlarmsState(){}

   public AlarmsState(boolean ignoring, int minutesRemaining) {
      this.ignoring = ignoring;
      this.minutesRemaining = minutesRemaining;
   }

   public boolean isIgnoring() {
      return ignoring;
   }

   public void setIgnoring(boolean ignoring) {
      this.ignoring = ignoring;
   }

   public int getMinutesRemaining() {
      return minutesRemaining;
   }

   public void setMinutesRemaining(int minutesRemaining) {
      this.minutesRemaining = minutesRemaining;
   }
}
