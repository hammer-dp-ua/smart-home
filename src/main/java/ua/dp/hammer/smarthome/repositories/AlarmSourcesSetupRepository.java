package ua.dp.hammer.smarthome.repositories;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.AlarmSourceSetupEntity;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Transactional
@Repository
public class AlarmSourcesSetupRepository {
   private static final Logger LOGGER = LogManager.getLogger(AlarmSourcesSetupRepository.class);

   private DevicesRepository devicesRepository;

   @PersistenceContext
   private EntityManager entityManager;

   public AlarmSourceSetupEntity getAlarmSource(AlarmInfo alarmInfo) {
      if (isAlarmInfoEmpty(alarmInfo)) {
         return null;
      }

      DeviceSetupEntity deviceSetupEntity = devicesRepository
            .getDeviceTypeNameEntityOrThrowException(alarmInfo.getDeviceName());

      TypedQuery<AlarmSourceSetupEntity> query =
            entityManager.createQuery("from " + AlarmSourceSetupEntity.class.getSimpleName() + " " +
               "where source = :source and deviceSetup = :deviceSetup",
            AlarmSourceSetupEntity.class);
      query.setParameter("source", alarmInfo.getAlarmSource());
      query.setParameter("deviceSetup", deviceSetupEntity);
      List<AlarmSourceSetupEntity> persistedAlarmSources = query.getResultList();

      if (persistedAlarmSources.isEmpty()) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(AlarmSourceSetupEntity.class.getSimpleName() + " doesn't exist: " + alarmInfo);
         }
         return null;
      }
      return persistedAlarmSources.get(0);
   }

   public void addAlarmSource(AlarmInfo alarmInfo) {
      if (isAlarmInfoEmpty(alarmInfo)) {
         return;
      }

      AlarmSourceSetupEntity alarmSourceSetupEntity = getAlarmSource(alarmInfo);
      if (alarmSourceSetupEntity != null) {
         return;
      }

      DeviceSetupEntity deviceSetupEntity = devicesRepository
            .getDeviceTypeNameEntityOrThrowException(alarmInfo.getDeviceName());
      alarmSourceSetupEntity = new AlarmSourceSetupEntity();

      alarmSourceSetupEntity.setDeviceSetup(deviceSetupEntity);
      alarmSourceSetupEntity.setSource(alarmInfo.getAlarmSource());
      entityManager.persist(alarmSourceSetupEntity);
   }

   public void deleteAlarmSource(AlarmInfo alarmInfo) {
      if (isAlarmInfoEmpty(alarmInfo)) {
         return;
      }

      AlarmSourceSetupEntity alarmSourceSetupEntity = getAlarmSource(alarmInfo);
      if (alarmSourceSetupEntity == null) {
         return;
      }

      entityManager.remove(alarmSourceSetupEntity);
   }

   public List<AlarmSourceSetupEntity> getAlarmSources() {
      TypedQuery<AlarmSourceSetupEntity> query =
            entityManager.createQuery("from " + AlarmSourceSetupEntity.class.getSimpleName() + " e " +
               "order by e.deviceSetup.name, e.source",
            AlarmSourceSetupEntity.class);
      List<AlarmSourceSetupEntity> result = query.getResultList();

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Found " + result.size() + " " + AlarmSourceSetupEntity.class.getSimpleName() + " entities");
      }
      return result;
   }

   private boolean isAlarmInfoEmpty(AlarmInfo alarmInfo) {
      boolean empty = alarmInfo == null || StringUtils.isEmpty(alarmInfo.getAlarmSource()) ||
            StringUtils.isEmpty(alarmInfo.getDeviceName());

      if (empty && LOGGER.isDebugEnabled()) {
         LOGGER.debug("Empty alarm info: " + alarmInfo);
      }
      return empty;
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
