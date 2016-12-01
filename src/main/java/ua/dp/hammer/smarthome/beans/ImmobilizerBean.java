package ua.dp.hammer.smarthome.beans;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ImmobilizerBean {

   private LocalDateTime activationDateTime;
   private LocalDateTime deactivationDateTime;

   public LocalDateTime getActivationDateTime() {
      return activationDateTime;
   }

   public void setActivationDateTime(LocalDateTime activatedDateTime) {
      this.activationDateTime = activatedDateTime;
   }

   public LocalDateTime getDeactivationDateTime() {
      return deactivationDateTime;
   }

   public void setDeactivationDateTime(LocalDateTime deactivationDateTime) {
      this.deactivationDateTime = deactivationDateTime;
   }
}
