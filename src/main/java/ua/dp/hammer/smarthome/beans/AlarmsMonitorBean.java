package ua.dp.hammer.smarthome.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.entities.AlarmSensorEntity;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;
import ua.dp.hammer.smarthome.models.alarms.MotionDetector;
import ua.dp.hammer.smarthome.models.alarms.StreetMotionDetectors;
import ua.dp.hammer.smarthome.repositories.AlarmSourcesSetupRepository;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.utils.Utils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Transactional
@Component
public class AlarmsMonitorBean {
   private static final Logger LOGGER = LogManager.getLogger(AlarmsMonitorBean.class);

   private Queue<DeferredResult<StreetMotionDetectors>> allStatesDeferredResults;
   private final StreetMotionDetectors streetMotionDetectors = new StreetMotionDetectors();

   @PersistenceContext
   private EntityManager entityManager;

   private DevicesRepository devicesRepository;
   private AlarmSourcesSetupRepository alarmSourcesSetupRepository;

   @PostConstruct
   public void init() {
      allStatesDeferredResults = new ConcurrentLinkedQueue<>();
   }

   public void addStateDeferredResult(DeferredResult<StreetMotionDetectors> defResult) {
      allStatesDeferredResults.add(defResult);
   }

   public void receiveAlarm(@NotNull AlarmInfo alarm) {
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

      saveAlarmEvent(alarm);
   }

   private void saveAlarmEvent(AlarmInfo alarm) {
      DeviceSetupEntity deviceSetupEntity = devicesRepository.getDeviceTypeNameEntity(alarm.getDeviceName());

      if (deviceSetupEntity != null && StringUtils.isNotEmpty(alarm.getAlarmSource())) {
         AlarmSensorEntity alarmSensorEntity = new AlarmSensorEntity();

         alarmSensorEntity.setTypeName(deviceSetupEntity);
         alarmSensorEntity.setSource(alarmSourcesSetupRepository.getAlarmSource(alarm));
         alarmSensorEntity.setAlarmDateTime(LocalDateTime.now());
         entityManager.persist(alarmSensorEntity);

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Saved alarm state: " + alarm);
         }
      }
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }

   @Autowired
   public void setAlarmSourcesSetupRepository(AlarmSourcesSetupRepository alarmSourcesSetupRepository) {
      this.alarmSourcesSetupRepository = alarmSourcesSetupRepository;
   }
}
