package ua.dp.hammer.smarthome.models;

public enum Esp8266ResetReasons {

   POWER_REBOOT(0, "Power reboot"), HARDWARE_WDT_RESET(1, "Hardware WDT reset"), FATAL_EXCEPTION(2, "Fatal exception"),
   SOFTWARE_WATCHDOG_RESET(3, "Software watchdog reset"), SOFTWARE_RESET(4, "Software reset"), DEEP_SLEEP(5, "Deep-sleep"),
   HARDWARE_RESET(6, "Hardware reset");

   private int no;
   private String reason;

   Esp8266ResetReasons(int no, String reason) {
      this.no = no;
      this.reason = reason;
   }

   public static String getReason(int no) {
      for (Esp8266ResetReasons foundReason : Esp8266ResetReasons.values()) {
         if (foundReason.no == no) {
            return foundReason.reason;
         }
      }
      return "unknown reason";
   }
}
