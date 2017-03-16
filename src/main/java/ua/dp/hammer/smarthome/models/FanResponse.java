package ua.dp.hammer.smarthome.models;

public class FanResponse extends ServerStatus {
   private boolean turnOn;
   private int manuallyTurnedOnTimeout;

   public FanResponse(StatusCodes statusCodes) {
      super(statusCodes);
   }

   public boolean isTurnOn() {
      return turnOn;
   }

   public void setTurnOn(boolean turnOn) {
      this.turnOn = turnOn;
   }

   public int getManuallyTurnedOnTimeout() {
      return manuallyTurnedOnTimeout;
   }

   public void setManuallyTurnedOnTimeout(int manuallyTurnedOnTimeout) {
      this.manuallyTurnedOnTimeout = manuallyTurnedOnTimeout;
   }
}
