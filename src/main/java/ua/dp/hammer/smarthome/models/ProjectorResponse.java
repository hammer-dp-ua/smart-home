package ua.dp.hammer.smarthome.models;

public class ProjectorResponse extends ServerStatus {
   private boolean turnOn;

   public ProjectorResponse(ServerStatus serverStatus) {
      super();
      super.setStatusCode(serverStatus.getStatusCode());
      super.setIncludeDebugInfo(serverStatus.isIncludeDebugInfo());
      super.setUpdateFirmware(serverStatus.isUpdateFirmware());
   }

   public boolean isTurnOn() {
      return turnOn;
   }

   public void setTurnOn(boolean turnOn) {
      this.turnOn = turnOn;
   }
}
