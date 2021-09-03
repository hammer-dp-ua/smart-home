package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.alarms.Alarm;
import ua.dp.hammer.smarthome.models.alarms.MotionDetector;
import ua.dp.hammer.smarthome.models.alarms.StreetMotionDetectors;
import ua.dp.hammer.smarthome.utils.Utils;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class AlarmsMonitorBean {
   private static final Logger LOGGER = LogManager.getLogger(AlarmsMonitorBean.class);

   private Queue<DeferredResult<StreetMotionDetectors>> allStatesDeferredResults;
   private final StreetMotionDetectors streetMotionDetectors = new StreetMotionDetectors();

   @PostConstruct
   public void init() {
      allStatesDeferredResults = new ConcurrentLinkedQueue<>();
   }

   public void addStateDeferredResult(DeferredResult<StreetMotionDetectors> defResult) {
      allStatesDeferredResults.add(defResult);
   }

   public void receiveAlarm(@NotNull Alarm alarm) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Alarm received: " + alarm + ". Deferred queue size: " + allStatesDeferredResults.size());
      }

      MotionDetector motionDetector = new MotionDetector();
      motionDetector.setName(alarm.getDeviceName());
      motionDetector.setSource(alarm.getAlarmSource());
      motionDetector.setTriggerTimestamp(Utils.jodaLocalDateTimeToMilli(LocalDateTime.now()));

      streetMotionDetectors.getMotionDetectors().add(motionDetector);

      while (!allStatesDeferredResults.isEmpty()) {
         DeferredResult<StreetMotionDetectors> queueElement = allStatesDeferredResults.poll();
         queueElement.setResult(streetMotionDetectors);
      }
   }
}
