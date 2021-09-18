package ua.dp.hammer.smarthome.models.alarms;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AlarmInfo {
   private String alarmSource;
   private String deviceName;
   private boolean ignoreAlarms;

   public AlarmInfo() {
   }

   public AlarmInfo(String alarmSource, String deviceName, boolean ignoreAlarms) {
      this.alarmSource = alarmSource;
      this.deviceName = deviceName;
      this.ignoreAlarms = ignoreAlarms;
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
      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      AlarmInfo alarm = (AlarmInfo) o;

      return new EqualsBuilder()
            .append(alarmSource, alarm.alarmSource)
            .append(deviceName, alarm.deviceName)
            .append(ignoreAlarms, alarm.ignoreAlarms)
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

   @Override
   public String toString() {
      return "Alarm{" +
            "alarmSource='" + alarmSource + '\'' +
            ", deviceName='" + deviceName + '\'' +
            '}';
   }
}
