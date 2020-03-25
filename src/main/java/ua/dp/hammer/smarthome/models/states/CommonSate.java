package ua.dp.hammer.smarthome.models.states;

public class CommonSate {
   private String deviceName;
   private boolean notAvailable = true;

   public String getDeviceName() {
      return deviceName;
   }

   public CommonSate setDeviceName(String deviceName) {
      this.deviceName = deviceName;
      return this;
   }

   public boolean isNotAvailable() {
      return notAvailable;
   }

   public void setNotAvailable(boolean notAvailable) {
      this.notAvailable = notAvailable;
   }

   public void setNewSate(CommonSate newSate) {
      setDeviceName(newSate.getDeviceName());
      setNotAvailable(newSate.isNotAvailable());
   }
}
