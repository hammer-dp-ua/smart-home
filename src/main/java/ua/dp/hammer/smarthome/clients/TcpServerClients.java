package ua.dp.hammer.smarthome.clients;

public enum TcpServerClients {
   ENTRANCE_PROJECTORS("192.168.0.23");

   private String ipAddress;

   TcpServerClients(String ipAddress) {
      this.ipAddress = ipAddress;
   }

   public String getIpAddress() {
      return ipAddress;
   }
}
