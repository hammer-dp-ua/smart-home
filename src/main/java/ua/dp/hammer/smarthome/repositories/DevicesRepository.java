package ua.dp.hammer.smarthome.repositories;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.AlarmSensorEntity;
import ua.dp.hammer.smarthome.entities.AlarmSourceSetupEntity;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.entities.DeviceTypeEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.alarms.Alarm;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceType;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Repository
public class DevicesRepository {
   private static final Logger LOGGER = LogManager.getLogger(DevicesRepository.class);

   public static final String DEVICE_DOESNT_EXIST_ERROR = "Device doesn't exist";
   public static final String ALARM_SOURCE_DOESNT_EXIST_ERROR = "Alarm source doesn't exist";
   public static final String UNKNOWN_TYPE_ERROR = "Unknown device type: ";
   public static final String ALREADY_EXISTS_ERROR = "Such device already exists: ";

   // Something like second level cache
   private final Map<DeviceType, DeviceTypeEntity> allDeviceTypeEntities = new HashMap<>();
   private final Map<String, DeviceSetupEntity> allDeviceTypeNameEntities = new HashMap<>();
   private final Map<String, AlarmSourceSetupEntity> alarmSourcesSetup = new HashMap<>();

   @PersistenceContext
   private EntityManager entityManager;

   @PostConstruct
   public void init() {
      loadAndSaveAllDeviceTypeEntities();
      loadAndSaveAllDeviceTypeNameEntities();
      loadAndSaveAllAlarmSourcesSetup();
   }

   public void saveTechnicalDeviceInfo(TechnicalDeviceInfoEntity infoEntity, String deviceName) {
      DeviceSetupEntity deviceTypeName = getDeviceTypeNameEntity(deviceName);

      if (deviceTypeName == null) {
         LOGGER.warn("Unknown device name: '" + deviceName + "'. Add it into a list of known devices.");
         return;
      }

      infoEntity.setTypeName(deviceTypeName);
      entityManager.persist(infoEntity);
   }

   public DeviceSetupEntity getDeviceTypeNameEntity(String deviceName) {
      return allDeviceTypeNameEntities.get(deviceName);
   }

   public Collection<DeviceSetupEntity> getAllDeviceTypeNameEntities() {
      return allDeviceTypeNameEntities.values();
   }

   public List<DeviceSetupEntity> getDevicesByType(DeviceType type) {
      return allDeviceTypeNameEntities.values()
            .stream()
            .filter(device -> device.getType().getType() == type)
            .collect(Collectors.toList());
   }

   private boolean isDeviceTypeExists(DeviceTypeEntity deviceType) {
      return deviceType != null && allDeviceTypeEntities.containsKey(deviceType.getType());
   }

   public DeviceTypeEntity getDeviceTypeEntity(DeviceType deviceType) {
      return allDeviceTypeEntities.get(deviceType);
   }

   public void saveNewDevice(DeviceSetupEntity entity) {
      if (!isDeviceTypeExists(entity.getType())) {
         throw new DeviceSetupException(UNKNOWN_TYPE_ERROR + entity.getType());
      }
      if (getDeviceTypeNameEntity(entity.getName()) != null) {
         throw new DeviceSetupException(ALREADY_EXISTS_ERROR + entity.getName());
      }

      entityManager.persist(entity);
      allDeviceTypeNameEntities.put(entity.getName(), entity);
   }

   public void deleteDevice(Integer id) {
      DeviceSetupEntity persistedDevice = findDevice(id);

      entityManager.remove(persistedDevice);
      allDeviceTypeNameEntities.remove(persistedDevice.getName());
   }

   public void modifyDevice(DeviceSetupInfo device) {
      DeviceSetupEntity persistedDeviceEntity = findDevice(device.getId());
      String persistedName = persistedDeviceEntity.getName();
      DeviceTypeEntity persistedDeviceTypeEntity = findDeviceType(device.getType());

      persistedDeviceEntity.setName(device.getName());
      persistedDeviceEntity.setIp4Address(device.getIp4Address());
      persistedDeviceEntity.setType(persistedDeviceTypeEntity);

      entityManager.persist(persistedDeviceEntity);

      allDeviceTypeNameEntities.remove(persistedName);
      allDeviceTypeNameEntities.put(device.getName(), persistedDeviceEntity);
   }

   public void addAlarmSource(String source) {
      if (StringUtils.isEmpty(source) || alarmSourcesSetup.containsKey(source)) {
         return;
      }

      AlarmSourceSetupEntity alarmSourceSetupEntity = new AlarmSourceSetupEntity();
      alarmSourceSetupEntity.setSource(source);
      entityManager.persist(alarmSourceSetupEntity);
      alarmSourcesSetup.put(source, alarmSourceSetupEntity);
   }

   public void deleteAlarmSource(String source) {
      if (StringUtils.isEmpty(source)) {
         return;
      }

      alarmSourcesSetup.remove(source);

      TypedQuery<AlarmSourceSetupEntity> query =
            entityManager.createQuery("from " + AlarmSourceSetupEntity.class.getSimpleName() + " where source = :source",
                  AlarmSourceSetupEntity.class);
      query.setParameter("source", source);
      List<AlarmSourceSetupEntity> persistedDevices = query.getResultList();

      if (persistedDevices.isEmpty()) {
         throw new DeviceSetupException(ALARM_SOURCE_DOESNT_EXIST_ERROR);
      }

      entityManager.remove(persistedDevices.get(0));
   }

   public List<String> getAlarmSources() {
      return alarmSourcesSetup.keySet()
            .stream()
            .sorted()
            .collect(Collectors.toList());
   }

   public void saveAlarmEvent(@NotNull Alarm alarm) {
      DeviceSetupEntity deviceSetupEntity = this.getDeviceTypeNameEntity(alarm.getDeviceName());

      if (deviceSetupEntity != null && StringUtils.isNotEmpty(alarm.getAlarmSource())) {
         AlarmSensorEntity alarmSensorEntity = new AlarmSensorEntity();

         alarmSensorEntity.setTypeName(deviceSetupEntity);
         alarmSensorEntity.setSource(alarmSourcesSetup.get(alarm.getAlarmSource()));
         alarmSensorEntity.setAlarmDateTime(LocalDateTime.now());
         entityManager.persist(alarmSensorEntity);

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Saved alarm state: " + alarm);
         }
      }
   }

   private DeviceTypeEntity findDeviceType(DeviceType type) {
      TypedQuery<DeviceTypeEntity> query =
            entityManager.createQuery("from " + DeviceTypeEntity.class.getSimpleName() + " where type = :type",
                  DeviceTypeEntity.class);
      query.setParameter("type", type);
      List<DeviceTypeEntity> result = query.getResultList();

      if (result.isEmpty()) {
         throw new DeviceSetupException(UNKNOWN_TYPE_ERROR + type);
      }
      return result.get(0);
   }

   private DeviceSetupEntity findDevice(Integer id) {
      TypedQuery<DeviceSetupEntity> query =
            entityManager.createQuery("from " + DeviceSetupEntity.class.getSimpleName() + " where id = :id",
                  DeviceSetupEntity.class);
      query.setParameter("id", id);
      List<DeviceSetupEntity> persistedDevices = query.getResultList();

      if (persistedDevices.isEmpty()) {
         throw new DeviceSetupException(DEVICE_DOESNT_EXIST_ERROR);
      }

      return persistedDevices.get(0);
   }

   public static TechnicalDeviceInfoEntity createTechnicalDeviceInfoEntity(DeviceInfo deviceInfo) {
      TechnicalDeviceInfoEntity deviceInfoEntity = new TechnicalDeviceInfoEntity();

      Integer gain = StringUtils.isEmpty(deviceInfo.getGain()) || !deviceInfo.getGain().matches("-?\\d+") ? null :
            Integer.parseInt(deviceInfo.getGain().trim());
      deviceInfoEntity.setGain(gain);
      deviceInfoEntity.setUptimeSec(deviceInfo.getUptime());
      deviceInfoEntity.setErrors(deviceInfo.getErrors());
      deviceInfoEntity.setFreeHeap(deviceInfo.getFreeHeapSpace());
      deviceInfoEntity.setFirmwareTimestamp(StringUtils.isEmpty(deviceInfo.getBuildTimestamp()) ? null :
            deviceInfo.getBuildTimestamp());
      deviceInfoEntity.setResetReason(StringUtils.isEmpty(deviceInfo.getResetReason()) ? null :
            deviceInfo.getResetReason());
      deviceInfoEntity.setSystemRestartReason(StringUtils.isEmpty(deviceInfo.getSystemRestartReason()) ? null :
            deviceInfo.getSystemRestartReason());
      deviceInfoEntity.setInfoDt(LocalDateTime.now());
      return deviceInfoEntity;
   }

   private void loadAndSaveAllDeviceTypeEntities() {
      TypedQuery<DeviceTypeEntity> query =
            entityManager.createQuery("from " + DeviceTypeEntity.class.getSimpleName() + " e", DeviceTypeEntity.class);
      List<DeviceTypeEntity> result = query.getResultList();
      LOGGER.info("Loaded " + result.size() + " instances of " + DeviceTypeEntity.class.getSimpleName());

      allDeviceTypeEntities.clear();
      for (DeviceTypeEntity entity : result) {
         allDeviceTypeEntities.put(entity.getType(), entity);
      }
   }

   private void loadAndSaveAllDeviceTypeNameEntities() {
      TypedQuery<DeviceSetupEntity> query =
            entityManager.createQuery("from " + DeviceSetupEntity.class.getSimpleName() + " e", DeviceSetupEntity.class);
      List<DeviceSetupEntity> result = query.getResultList();
      LOGGER.info("Loaded " + result.size() + " instances of " + DeviceSetupEntity.class.getSimpleName());

      allDeviceTypeNameEntities.clear();
      for (DeviceSetupEntity entity : result) {
         allDeviceTypeNameEntities.put(entity.getName(), entity);
      }
   }

   private void loadAndSaveAllAlarmSourcesSetup() {
      TypedQuery<AlarmSourceSetupEntity> query =
            entityManager.createQuery("from " + AlarmSourceSetupEntity.class.getSimpleName() + " e", AlarmSourceSetupEntity.class);
      List<AlarmSourceSetupEntity> result = query.getResultList();

      LOGGER.info("Loaded " + result.size() + " instances of " + AlarmSourceSetupEntity.class.getSimpleName());

      alarmSourcesSetup.clear();
      for (AlarmSourceSetupEntity entity : result) {
         alarmSourcesSetup.put(entity.getSource(), entity);
      }
   }
}
