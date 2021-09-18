package ua.dp.hammer.smarthome.models.alarms;

import java.util.HashSet;
import java.util.Set;

public class StreetMotionDetectors {
   private final Set<MotionDetector> motionDetectors = new HashSet<>();

   public Set<MotionDetector> getMotionDetectors() {
      return motionDetectors;
   }
}
