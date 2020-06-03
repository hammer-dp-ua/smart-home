package ua.dp.hammer.smarthome.models.states;

public class CommonSate {
   private String deviceName;
   private Boolean notAvailable;

   public String getDeviceName() {
      return deviceName;
   }

   public void setDeviceName(String deviceName) {
      this.deviceName = deviceName;
   }

   public Boolean isNotAvailable() {
      return notAvailable;
   }

   public void setNotAvailable(Boolean notAvailable) {
      this.notAvailable = notAvailable;
   }

   public void setNewSate(CommonSate newSate) {
      setDeviceName(newSate.getDeviceName());
      setNotAvailable(newSate.isNotAvailable());
   }
}
