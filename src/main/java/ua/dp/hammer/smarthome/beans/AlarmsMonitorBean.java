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
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

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
      motionDetector.setTriggerTimestamp(Utils.localDateTimeToMilli(LocalDateTime.now()));

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

         alarmSensorEntity.setDeviceSetup(deviceSetupEntity);
         alarmSensorEntity.setSource(alarmSourcesSetupRepository.getDeviceTypeNameEntityOrThrowException(alarm));
         alarmSensorEntity.setAlarmDateTime(LocalDateTime.now());
         entityManager.persist(alarmSensorEntity);

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Saved alarm state: " + alarm);
         }
      }
   }

   public List<MotionDetector> getHistory(Long fromMs, Long toMs) {
      String wherePart = StringUtils.EMPTY;
      LocalDateTime localDateTimeFrom = null;
      LocalDateTime localDateTimeTo = null;

      if (fromMs != null) {
         wherePart = " where alarmDateTime >= :from ";
         localDateTimeFrom = Utils.milliToLocalDateTime(fromMs);
      }

      if (toMs != null) {
         if (StringUtils.isNotEmpty(wherePart)) {
            wherePart += "and alarmDateTime <= :to";
         } else {
            wherePart = " where alarmDateTime <= :to";
         }
         localDateTimeTo = Utils.milliToLocalDateTime(toMs);
      }

      TypedQuery<AlarmSensorEntity> query =
            entityManager.createQuery("from " + AlarmSensorEntity.class.getSimpleName() + wherePart +
            " order by alarmDateTime desc",
            AlarmSensorEntity.class);

      if (localDateTimeFrom != null) {
         query.setParameter("from", localDateTimeFrom);
      }
      if (localDateTimeTo != null) {
         query.setParameter("to", localDateTimeTo);
      }

      List<AlarmSensorEntity> queryResult = query.getResultList();
      return queryResult
            .stream()
            .map(e -> new MotionDetector(e.getDeviceSetup().getName(), e.getSource().getSource(), Utils.localDateTimeToMilli(e.getAlarmDateTime())))
            .collect(Collectors.toList());
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
