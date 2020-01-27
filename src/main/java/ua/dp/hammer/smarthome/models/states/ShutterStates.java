package ua.dp.hammer.smarthome.models.states;

public enum ShutterStates {
   SHUTTER_OPENING(1),
   SHUTTER_CLOSING(2),
   SHUTTER_OPENED(3),
   SHUTTER_CLOSED(4);

   private int no;

   ShutterStates(int no) {
      this.no = no;
   }

   public int getNo() {
      return no;
   }

   public static ShutterStates getState(int no) {
      for (ShutterStates state : ShutterStates.values()) {
         if (state.no == no) {
            return state;
         }
      }
      return null;
   }
}
