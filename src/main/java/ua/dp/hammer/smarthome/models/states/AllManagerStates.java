package ua.dp.hammer.smarthome.models.states;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AllManagerStates {
   private final static AllManagerStates INSTANCE = new AllManagerStates();

   private AllManagerStates() {
   }

   public static AllManagerStates getInstance() {
      return INSTANCE;
   }

   private Set<ProjectorState> projectorsState = Collections.synchronizedSet(new HashSet<>());
   private FanState fanState = new FanState();
   private AlarmsState alarmsState = new AlarmsState();
   private Set<ShutterState> shuttersState = Collections.synchronizedSet(new HashSet<>());

   public Set<ProjectorState> getProjectorsState() {
      return projectorsState;
   }

   public FanState getFanState() {
      return fanState;
   }

   public AlarmsState getAlarmsState() {
      return alarmsState;
   }

   public Set<ShutterState> getShuttersState() {
      return shuttersState;
   }
}
