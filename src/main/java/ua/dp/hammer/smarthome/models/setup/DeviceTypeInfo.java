package ua.dp.hammer.smarthome.models.setup;

public class DeviceTypeInfo {
   private String type;
   private Integer keepAliveIntervalSec;

   public DeviceTypeInfo() {
   }

   public DeviceTypeInfo(String type, Integer keepAliveIntervalSec) {
      this.type = type;
      this.keepAliveIntervalSec = keepAliveIntervalSec;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public Integer getKeepAliveIntervalSec() {
      return keepAliveIntervalSec;
   }

   public void setKeepAliveIntervalSec(Integer keepAliveIntervalSec) {
      this.keepAliveIntervalSec = keepAliveIntervalSec;
   }
}
