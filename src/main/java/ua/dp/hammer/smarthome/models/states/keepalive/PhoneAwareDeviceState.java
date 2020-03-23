package ua.dp.hammer.smarthome.models.states.keepalive;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PhoneAwareDeviceState {
   private String name;
   private long timestamp;

   public PhoneAwareDeviceState() {
   }

   public PhoneAwareDeviceState(String name, long timestamp) {
      this.name = name;
      this.timestamp = timestamp;
   }

   private int hashCode = -1;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof PhoneAwareDeviceState)) {
         return false;
      }

      PhoneAwareDeviceState thatObject = (PhoneAwareDeviceState) o;
      return name.equals(thatObject.name) && timestamp == thatObject.timestamp;
   }

   @Override
   public int hashCode() {
      if (hashCode == -1) {
         hashCode = new HashCodeBuilder()
               .append(name)
               .append(timestamp)
               .hashCode();
      }
      return hashCode;
   }
}
