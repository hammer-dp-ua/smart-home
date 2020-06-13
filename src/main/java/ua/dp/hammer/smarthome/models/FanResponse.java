package ua.dp.hammer.smarthome.models;

public class FanResponse extends ServerStatus {
   private boolean turnOn;
   private int manuallyTurnedOnTimeoutSetting;

   public FanResponse(){};

   public FanResponse(StatusCodes statusCodes) {
      super(statusCodes);
   }

   public boolean isTurnOn() {
      return turnOn;
   }

   public void setTurnOn(boolean turnOn) {
      this.turnOn = turnOn;
   }

   public int getManuallyTurnedOnTimeoutSetting() {
      return manuallyTurnedOnTimeoutSetting;
   }

   public void setManuallyTurnedOnTimeoutSetting(int manuallyTurnedOnTimeoutSetting) {
      this.manuallyTurnedOnTimeoutSetting = manuallyTurnedOnTimeoutSetting;
   }
}
