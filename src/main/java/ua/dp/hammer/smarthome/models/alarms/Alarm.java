package ua.dp.hammer.smarthome.models.alarms;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Alarm {
   private String alarmSource;
   private String deviceName;

   public Alarm(String alarmSource, String deviceName) {
      this.alarmSource = alarmSource;
      this.deviceName = deviceName;
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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      Alarm alarm = (Alarm) o;

      return new EqualsBuilder().append(alarmSource, alarm.alarmSource).append(deviceName, alarm.deviceName).isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37).append(alarmSource).append(deviceName).toHashCode();
   }

   @Override
   public String toString() {
      return "Alarm{" +
            "alarmSource='" + alarmSource + '\'' +
            ", deviceName='" + deviceName + '\'' +
            '}';
   }
}
