package ua.dp.hammer.smarthome.models;

public class FanRequestInfo extends DeviceInfo {
   // switched by fan's switcher
   private boolean switchedOnManually;
   private Integer switchedOnManuallySecondsLeft;

   public boolean isSwitchedOnManually() {
      return switchedOnManually;
   }

   public void setSwitchedOnManually(boolean switchedOnManually) {
      this.switchedOnManually = switchedOnManually;
   }

   public Integer getSwitchedOnManuallySecondsLeft() {
      return switchedOnManuallySecondsLeft;
   }

   public void setSwitchedOnManuallySecondsLeft(Integer switchedOnManuallySecondsLeft) {
      this.switchedOnManuallySecondsLeft = switchedOnManuallySecondsLeft;
   }
}
