package ua.dp.hammer.smarthome.models.states;

public class CommonSate {
   private String name;
   private boolean notAvailable = true;

   public String getName() {
      return name;
   }

   public CommonSate setName(String name) {
      this.name = name;
      return this;
   }

   public boolean isNotAvailable() {
      return notAvailable;
   }

   public void setNotAvailable(boolean notAvailable) {
      this.notAvailable = notAvailable;
   }

   public void setNewSate(CommonSate newSate) {
      setName(newSate.getName());
      setNotAvailable(newSate.isNotAvailable());
   }
}
