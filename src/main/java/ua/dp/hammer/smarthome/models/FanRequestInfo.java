package ua.dp.hammer.smarthome.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class FanRequestInfo extends DeviceInfo {
   // switched by fan's switcher
   private boolean switchedOnManually;
   private int switchedOnManuallySecondsLeft;

   public boolean isSwitchedOnManually() {
      return switchedOnManually;
   }

   public void setSwitchedOnManually(boolean switchedOnManually) {
      this.switchedOnManually = switchedOnManually;
   }

   public int getSwitchedOnManuallySecondsLeft() {
      return switchedOnManuallySecondsLeft;
   }

   public void setSwitchedOnManuallySecondsLeft(int switchedOnManuallySecondsLeft) {
      this.switchedOnManuallySecondsLeft = switchedOnManuallySecondsLeft;
   }

   @Override
   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }
}
