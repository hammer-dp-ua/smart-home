package ua.dp.hammer.smarthome.models.states;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ShutterState {
   private String name;
   private int shutterNo;
   private ShutterStates state;
   private boolean notAvailable;

   private int hashCode = -1;

   public ShutterState(String name, int shutterNo, ShutterStates state, boolean notAvailable) {
      if (name == null || name.length() == 0) {
         throw new IllegalStateException("Name can't be empty");
      }

      this.name = name;
      this.shutterNo = shutterNo;
      this.state = state;
      this.notAvailable = notAvailable;
   }

   public String getName() {
      return name;
   }

   public int getShutterNo() {
      return shutterNo;
   }

   public ShutterStates getState() {
      return state;
   }

   public void setState(ShutterStates state) {
      this.state = state;
   }

   public boolean isNotAvailable() {
      return notAvailable;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ShutterState)) {
         return false;
      }

      ShutterState thatObject = (ShutterState) o;
      return name.equals(thatObject.name) && (shutterNo == thatObject.shutterNo);
   }

   @Override
   public int hashCode() {
      if (hashCode == -1) {
         hashCode = new HashCodeBuilder()
               .append(name)
               .append(shutterNo)
               .hashCode();
      }
      return hashCode;
   }
}
