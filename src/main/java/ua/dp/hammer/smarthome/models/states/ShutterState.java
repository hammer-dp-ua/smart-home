package ua.dp.hammer.smarthome.models.states;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ShutterState extends CommonSate {
   private int shutterNo;
   private ShutterStates state;

   private int hashCode = -1;

   public ShutterState() {}

   public ShutterState(String name, int shutterNo, ShutterStates state, boolean notAvailable) {
      if (name == null || name.length() == 0) {
         throw new IllegalStateException("Name can't be empty");
      }

      this.shutterNo = shutterNo;
      this.state = state;
      super.setName(name);
      super.setNotAvailable(notAvailable);
   }

   public int getShutterNo() {
      return shutterNo;
   }

   public void setShutterNo(int shutterNo) {
      this.shutterNo = shutterNo;
   }

   public ShutterStates getState() {
      return state;
   }

   public void setState(ShutterStates state) {
      this.state = state;
   }

   public void setNewState(ShutterState newState) {
      shutterNo = newState.shutterNo;
      state = newState.state;
      super.setNewSate(newState);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ShutterState)) {
         return false;
      }

      ShutterState thatObject = (ShutterState) o;
      return getName().equals(thatObject.getName()) &&
            shutterNo == thatObject.shutterNo &&
            state == thatObject.state &&
            isNotAvailable() == thatObject.isNotAvailable();
   }

   @Override
   public int hashCode() {
      if (hashCode == -1) {
         hashCode = new HashCodeBuilder()
               .append(getName())
               .append(shutterNo)
               .append(state)
               .append(isNotAvailable())
               .hashCode();
      }
      return hashCode;
   }
}
