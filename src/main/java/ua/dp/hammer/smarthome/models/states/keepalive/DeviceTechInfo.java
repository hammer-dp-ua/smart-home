package ua.dp.hammer.smarthome.models.states.keepalive;

import ua.dp.hammer.smarthome.models.states.CommonSate;

public class DeviceTechInfo extends CommonSate {
   private Long lastDeviceRequestTimestamp;
   private Integer uptime;
   private String buildTimestamp;
   private String deviceType;

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

   public String getBuildTimestamp() {
      return buildTimestamp;
   }

   public void setBuildTimestamp(String buildTimestamp) {
      this.buildTimestamp = buildTimestamp;
   }

   public String getDeviceType() {
      return deviceType;
   }

   public void setDeviceType(String deviceType) {
      this.deviceType = deviceType;
   }
}
