package ua.dp.hammer.smarthome.models.setup;

public class GeneralDevice {
   private String name;
   private DeviceType type;
   private Integer keepAliveIntervalSec;
   private String ip4Address;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public DeviceType getType() {
      return type;
   }

   public void setType(DeviceType type) {
      this.type = type;
   }

   public Integer getKeepAliveIntervalSec() {
      return keepAliveIntervalSec;
   }

   public void setKeepAliveIntervalSec(Integer keepAliveIntervalSec) {
      this.keepAliveIntervalSec = keepAliveIntervalSec;
   }

   public String getIp4Address() {
      return ip4Address;
   }

   public void setIp4Address(String ip4Address) {
      this.ip4Address = ip4Address;
   }
}
