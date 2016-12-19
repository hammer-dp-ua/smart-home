package ua.dp.hammer.smarthome.models;

public class ProjectorResponse extends ServerStatus {
   private boolean turnOn;
   private long timeStamp;

   public ProjectorResponse(StatusCodes statusCodes) {
      super(statusCodes);
      timeStamp = System.currentTimeMillis();
   }

   public boolean isTurnOn() {
      return turnOn;
   }

   public void setTurnOn(boolean turnOn) {
      this.turnOn = turnOn;
   }

   public long getTimeStamp() {
      return timeStamp;
   }

   public void setTimeStamp(long timeStamp) {
      this.timeStamp = timeStamp;
   }
}
