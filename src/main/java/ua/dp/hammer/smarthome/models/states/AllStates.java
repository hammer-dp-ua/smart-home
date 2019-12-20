package ua.dp.hammer.smarthome.models.states;

public class AllStates {
   private final static AllStates INSTANCE = new AllStates();

   private AllStates() {
   }

   public static AllStates getInstance() {
      return INSTANCE;
   }

   private ProjectorState projectorState = new ProjectorState();
   private FanState fanState = new FanState();
   private AlarmsState alarmsState = new AlarmsState();

   public ProjectorState getProjectorState() {
      return projectorState;
   }

   public FanState getFanState() {
      return fanState;
   }

   public AlarmsState getAlarmsState() {
      return alarmsState;
   }
}
