package ua.dp.hammer.smarthome.clients;

public enum StreetProjectors {
   ENTRANCE_PROJECTORS("192.168.0.23"),
   STOREHOUSE_PROJECTOR("192.168.0.22");

   private String ipAddress;

   StreetProjectors(String ipAddress) {
      this.ipAddress = ipAddress;
   }

   public String getIpAddress() {
      return ipAddress;
   }
}
