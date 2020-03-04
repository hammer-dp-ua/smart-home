package ua.dp.hammer.smarthome.models.states;

public class ProjectorState extends CommonSate {
   private boolean turnedOn;

   public boolean isTurnedOn() {
      return turnedOn;
   }

   public void setTurnedOn(boolean turnedOn) {
      this.turnedOn = turnedOn;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ProjectorState)) {
         return false;
      }

      ProjectorState thatObject = (ProjectorState) o;
      return getName().equals(thatObject.getName());
   }

   @Override
   public int hashCode() {
      return getName().hashCode();
   }
}
