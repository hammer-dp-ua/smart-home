package ua.dp.hammer.smarthome.models.alarms;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MotionDetector {
   private String name;
   private String source;
   private Long triggerTimestamp;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public Long getTriggerTimestamp() {
      return triggerTimestamp;
   }

   public void setTriggerTimestamp(Long triggerTimestamp) {
      this.triggerTimestamp = triggerTimestamp;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      MotionDetector that = (MotionDetector) o;

      return new EqualsBuilder().append(name, that.name).append(source, that.source).isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37).append(name).append(source).toHashCode();
   }
}
