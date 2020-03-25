package ua.dp.hammer.smarthome.models.states;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProjectorState extends CommonSate {
   private boolean turnedOn;

   private int hashCode = -1;

   public ProjectorState(String name) {
      setDeviceName(name);
   }

   public boolean isTurnedOn() {
      return turnedOn;
   }

   public void setTurnedOn(boolean turnedOn) {
      this.turnedOn = turnedOn;
   }

   public void setNewState(ProjectorState projectorState) {
      turnedOn = projectorState.turnedOn;
      super.setNewSate(projectorState);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ProjectorState)) {
         return false;
      }

      ProjectorState thatObject = (ProjectorState) o;
      return turnedOn == thatObject.turnedOn &&
            isNotAvailable() == thatObject.isNotAvailable() &&
            getDeviceName().equals(thatObject.getDeviceName());
   }

   @Override
   public int hashCode() {
      if (hashCode == -1) {
         hashCode = new HashCodeBuilder()
               .append(getDeviceName())
               .append(isNotAvailable())
               .append(turnedOn)
               .hashCode();
      }
      return hashCode;
   }
}
