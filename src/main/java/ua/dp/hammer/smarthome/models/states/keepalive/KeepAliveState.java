package ua.dp.hammer.smarthome.models.states.keepalive;

import ua.dp.hammer.smarthome.models.states.CommonSate;

public class KeepAliveState extends CommonSate {
   private Long lastDeviceRequestTimestamp;
   private Integer uptime;

   public Long getLastDeviceRequestTimestamp() {
      return lastDeviceRequestTimestamp;
   }

   public void setLastDeviceRequestTimestamp(Long lastDeviceRequestTimestamp) {
      this.lastDeviceRequestTimestamp = lastDeviceRequestTimestamp;
   }

   public Integer getUptime() {
      return uptime;
   }

   public void setUptime(Integer uptime) {
      this.uptime = uptime;
   }
}
