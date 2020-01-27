package ua.dp.hammer.smarthome.models.states;

public class ShutterStateRaw {
   private int shutterNo;
   private int shutterState;

   public int getShutterNo() {
      return shutterNo;
   }

   public void setShutterNo(int shutterNo) {
      this.shutterNo = shutterNo;
   }

   public int getShutterState() {
      return shutterState;
   }

   public void setShutterState(int shutterState) {
      this.shutterState = shutterState;
   }
}
