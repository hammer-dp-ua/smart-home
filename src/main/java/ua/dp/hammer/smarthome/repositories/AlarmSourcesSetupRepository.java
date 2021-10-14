package ua.dp.hammer.smarthome.repositories;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.AlarmSourceSetupEntity;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;
import ua.dp.hammer.smarthome.models.setup.AlarmSourceSetupInfo;

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

   public AlarmSourceSetupEntity getAlarmSource(Integer aaId) {
      if (aaId == null) {
         return null;
      }

      TypedQuery<AlarmSourceSetupEntity> query =
            entityManager.createQuery("from " + AlarmSourceSetupEntity.class.getSimpleName() + " where id = :aaId",
            AlarmSourceSetupEntity.class);
      query.setParameter("aaId", aaId);
      List<AlarmSourceSetupEntity> persistedAlarmSources = query.getResultList();

      if (persistedAlarmSources.isEmpty()) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(AlarmSourceSetupEntity.class.getSimpleName() + " doesn't exist: " + aaId);
         }
         return null;
      }
      return persistedAlarmSources.get(0);
   }

   public AlarmSourceSetupEntity getDeviceTypeNameEntityOrThrowException(AlarmInfo alarmInfo) {
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
         String errorMsg = AlarmSourceSetupEntity.class.getSimpleName() + " doesn't exist: " + alarmInfo;

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(errorMsg);
         }
         throw new DeviceSetupException(errorMsg);
      }
      return persistedAlarmSources.get(0);
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

   public void addAlarmSource(AlarmSourceSetupInfo alarmSourceSetupInfo) {
      if (alarmSourceSetupInfo.getAaId() != null || isAlarmInfoEmpty(alarmSourceSetupInfo)) {
         return;
      }

      DeviceSetupEntity deviceSetupEntity = devicesRepository
            .getDeviceTypeNameEntityOrThrowException(alarmSourceSetupInfo.getDeviceName());
      AlarmSourceSetupEntity alarmSourceSetupEntity = new AlarmSourceSetupEntity();

      alarmSourceSetupEntity.setDeviceSetup(deviceSetupEntity);
      alarmSourceSetupEntity.setSource(alarmSourceSetupInfo.getAlarmSource());
      alarmSourceSetupEntity.setIgnoreAlarms(alarmSourceSetupInfo.isIgnoreAlarms());
      entityManager.persist(alarmSourceSetupEntity);
   }

   public void deleteAlarmSource(Integer aaId) {
      if (aaId == null) {
         return;
      }

      AlarmSourceSetupEntity alarmSourceSetupEntity = getAlarmSource(aaId);
      if (alarmSourceSetupEntity == null) {
         return;
      }

      entityManager.remove(alarmSourceSetupEntity);
   }

   public void modifyAlarmSource(AlarmSourceSetupInfo alarmSourceSetupInfo) {
      if (isAlarmInfoEmpty(alarmSourceSetupInfo)) {
         return;
      }

      AlarmSourceSetupEntity alarmSourceSetupEntity = getAlarmSource(alarmSourceSetupInfo.getAaId());
      if (alarmSourceSetupEntity == null ||
            alarmSourceSetupEntity.isIgnoreAlarms() == alarmSourceSetupInfo.isIgnoreAlarms()) {
         return;
      }

      alarmSourceSetupEntity.setIgnoreAlarms(alarmSourceSetupInfo.isIgnoreAlarms());
      entityManager.persist(alarmSourceSetupEntity);
   }

   private boolean isAlarmInfoEmpty(AlarmSourceSetupInfo alarmSourceSetupInfo) {
      boolean empty = alarmSourceSetupInfo == null || StringUtils.isEmpty(alarmSourceSetupInfo.getAlarmSource()) ||
            StringUtils.isEmpty(alarmSourceSetupInfo.getDeviceName());

      if (empty && LOGGER.isDebugEnabled()) {
         LOGGER.debug("Empty alarm info: " + alarmSourceSetupInfo);
      }
      return empty;
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
