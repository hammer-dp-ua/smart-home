package ua.dp.hammer.smarthome.beans;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ImmobilizerBean {

   private LocalDateTime activatedDateTime;

   public LocalDateTime getActivatedDateTime() {
      return activatedDateTime;
   }

   public void setActivatedDateTime(LocalDateTime activatedDateTime) {
      this.activatedDateTime = activatedDateTime;
   }
}
