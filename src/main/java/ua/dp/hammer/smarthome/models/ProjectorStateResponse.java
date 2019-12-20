package ua.dp.hammer.smarthome.models;

public class ProjectorStateResponse {
   public ProjectorStateResponse(String state) {
      this.state = state;
   }

   private String state;

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }
}
