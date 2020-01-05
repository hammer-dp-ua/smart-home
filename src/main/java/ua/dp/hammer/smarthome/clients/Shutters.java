package ua.dp.hammer.smarthome.clients;

public enum Shutters {
   ROOM_SHUTTER("Room shutter", "192.168.0.27", 19),
   KITCHEN_SHUTTER_1("Kitchen shutter 1", "192.168.0.29", 25),
   KITCHEN_SHUTTER_2("Kitchen shutter 2", "192.168.0.29", 25);

   private String name;
   private String ipAddress;
   private int openingTimeSeconds;

   Shutters(String name, String ipAddress, int openingTimeSeconds) {
      this.name = name;
      this.ipAddress = ipAddress;
      this.openingTimeSeconds = openingTimeSeconds;
   }

   public String getName() {
      return name;
   }

   public String getIpAddress() {
      return ipAddress;
   }

   public int getOpeningTimeSeconds() {
      return openingTimeSeconds;
   }
}
