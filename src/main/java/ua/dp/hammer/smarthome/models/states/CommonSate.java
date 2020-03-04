package ua.dp.hammer.smarthome.models.states;

public class CommonSate {
   private String name;
   private boolean notAvailable = true;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public boolean isNotAvailable() {
      return notAvailable;
   }

   public void setNotAvailable(boolean notAvailable) {
      this.notAvailable = notAvailable;
   }
}
