package ua.dp.hammer.smarthome.models.setup;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AlarmSourceSetupInfo {
   private Integer aaId;
   private String alarmSource;
   private String deviceName;
   private boolean ignoreAlarms;

   public AlarmSourceSetupInfo() {
   }

   public AlarmSourceSetupInfo(Integer aaId, String alarmSource, String deviceName, boolean ignoreAlarms) {
      this.aaId = aaId;
      this.alarmSource = alarmSource;
      this.deviceName = deviceName;
      this.ignoreAlarms = ignoreAlarms;
   }

   public Integer getAaId() {
      return aaId;
   }

   public void setAaId(Integer aaId) {
      this.aaId = aaId;
   }

   public String getAlarmSource() {
      return alarmSource;
   }

   public void setAlarmSource(String alarmSource) {
      this.alarmSource = alarmSource;
   }

   public String getDeviceName() {
      return deviceName;
   }

   public void setDeviceName(String deviceName) {
      this.deviceName = deviceName;
   }

   public boolean isIgnoreAlarms() {
      return ignoreAlarms;
   }

   public void setIgnoreAlarms(boolean ignoreAlarms) {
      this.ignoreAlarms = ignoreAlarms;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
      {
         return true;
      }

      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      AlarmSourceSetupInfo thatObject = (AlarmSourceSetupInfo) o;

      return new EqualsBuilder()
            .append(alarmSource, thatObject.alarmSource)
            .append(deviceName, thatObject.deviceName)
            .append(ignoreAlarms, thatObject.ignoreAlarms)
            .isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37)
            .append(alarmSource)
            .append(deviceName)
            .append(ignoreAlarms)
            .toHashCode();
   }
}
