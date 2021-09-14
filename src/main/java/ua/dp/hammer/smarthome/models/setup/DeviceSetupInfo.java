package ua.dp.hammer.smarthome.models.setup;

public class DeviceSetupInfo {
   private Integer id;
   private String type;
   private String name;
   private Integer keepAliveIntervalSec;
   private String ip4Address;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
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

   public String getIp4Address() {
      return ip4Address;
   }

   public void setIp4Address(String ip4Address) {
      this.ip4Address = ip4Address;
   }
}
