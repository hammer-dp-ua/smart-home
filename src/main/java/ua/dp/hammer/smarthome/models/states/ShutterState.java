package ua.dp.hammer.smarthome.models.states;

public class ShutterState {
   private String name;
   private boolean opened;

   public ShutterState(String name, boolean opened) {
      if (name == null || name.length() == 0) {
         throw new IllegalStateException("Name can't be empty");
      }

      this.name = name;
      this.opened = opened;
   }

   public String getName() {
      return name;
   }

   public boolean isOpened() {
      return opened;
   }

   public void setOpened(boolean opened) {
      this.opened = opened;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ShutterState)) {
         return false;
      }

      ShutterState thatObject = (ShutterState) o;
      return name.equals(thatObject.name);
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }
}
