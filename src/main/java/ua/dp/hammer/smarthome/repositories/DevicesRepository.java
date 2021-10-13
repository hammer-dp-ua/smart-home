package ua.dp.hammer.smarthome.repositories;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.entities.DeviceTypeEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceTypeInfo;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Repository
public class DevicesRepository {
   private static final Logger LOGGER = LogManager.getLogger(DevicesRepository.class);

   public static final String DEVICE_DOESNT_EXIST_ERROR = "Device doesn't exist";
   public static final String UNKNOWN_TYPE_ERROR = "Unknown device type: ";
   public static final String ALREADY_EXISTS_ERROR = "Such device already exists: ";

   // Something like second level cache
   private final Map<String, DeviceTypeEntity> allDeviceTypeEntities = new HashMap<>();
   private final Map<String, DeviceSetupEntity> allDeviceTypeNameEntities = new HashMap<>();

   @PersistenceContext
   private EntityManager entityManager;

   @PostConstruct
   public void init() {
      loadAndSaveAllDeviceTypeEntities();
      loadAndSaveAllDeviceTypeNameEntities();
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
      DeviceSetupEntity result = allDeviceTypeNameEntities.get(deviceName);

      if (result == null && LOGGER.isDebugEnabled()) {
         LOGGER.debug("Device setup doesn't exist: " + deviceName);
      }
      return result;
   }

   public DeviceSetupEntity getDeviceTypeNameEntityOrThrowException(String deviceName) {
      DeviceSetupEntity result = getDeviceTypeNameEntity(deviceName);

      if (result == null) {
         throw new DeviceSetupException(DEVICE_DOESNT_EXIST_ERROR);
      }
      return result;
   }

   public Collection<DeviceSetupEntity> getAllDeviceTypeNameEntities() {
      return allDeviceTypeNameEntities.values();
   }

   public List<DeviceSetupEntity> getDevicesByType(String type) {
      return allDeviceTypeNameEntities.values()
            .stream()
            .filter(device -> device.getType().getType().equals(type))
            .collect(Collectors.toList());
   }

   private boolean isDeviceTypeExists(DeviceTypeEntity deviceType) {
      return deviceType != null && allDeviceTypeEntities.containsKey(deviceType.getType());
   }

   private DeviceTypeEntity getDeviceTypeEntity(String deviceType) {
      return allDeviceTypeEntities.get(deviceType);
   }

   public void saveNewDevice(DeviceSetupInfo device) {
      DeviceSetupEntity entity = new DeviceSetupEntity();

      entity.setName(device.getName());
      entity.setType(getDeviceTypeEntity(device.getType()));
      entity.setIp4Address(device.getIp4Address());

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
      DeviceTypeEntity persistedDeviceTypeEntity = findDeviceTypeOrThrowException(device.getType());

      persistedDeviceEntity.setName(device.getName());
      persistedDeviceEntity.setIp4Address(device.getIp4Address());
      persistedDeviceEntity.setType(persistedDeviceTypeEntity);

      entityManager.persist(persistedDeviceEntity);

      allDeviceTypeNameEntities.remove(persistedName);
      allDeviceTypeNameEntities.put(device.getName(), persistedDeviceEntity);
   }

   public void addDeviceType(@NotNull DeviceTypeInfo deviceTypeInfo) {
      if (allDeviceTypeEntities.get(deviceTypeInfo.getType()) != null) {
         return;
      }

      DeviceTypeEntity deviceTypeEntity = new DeviceTypeEntity();
      deviceTypeEntity.setType(deviceTypeInfo.getType());
      deviceTypeEntity.setKeepAliveIntervalSec(deviceTypeInfo.getKeepAliveIntervalSec());

      entityManager.persist(deviceTypeEntity);
      allDeviceTypeEntities.put(deviceTypeInfo.getType(), deviceTypeEntity);
   }

   public void deleteDeviceType(@NotNull DeviceTypeInfo deviceTypeInfo) {
      if (allDeviceTypeEntities.get(deviceTypeInfo.getType()) == null) {
         return;
      }

      TypedQuery<DeviceTypeEntity> query =
            entityManager.createQuery("from " + DeviceTypeEntity.class.getSimpleName() + " where type = :type",
                  DeviceTypeEntity.class);
      query.setParameter("type", deviceTypeInfo.getType());
      List<DeviceTypeEntity> result = query.getResultList();

      if (result.size() == 0) {
         return;
      }

      DeviceTypeEntity foundEntity = result.get(0);
      entityManager.remove(foundEntity);
      allDeviceTypeEntities.remove(deviceTypeInfo.getType());
   }

   public void modifyDeviceType(@NotNull DeviceTypeInfo deviceTypeInfo) {
      if (allDeviceTypeEntities.get(deviceTypeInfo.getType()) == null) {
         return;
      }

      TypedQuery<DeviceTypeEntity> query =
            entityManager.createQuery("from " + DeviceTypeEntity.class.getSimpleName() + " where type = :type",
                  DeviceTypeEntity.class);
      query.setParameter("type", deviceTypeInfo.getType());
      List<DeviceTypeEntity> result = query.getResultList();

      if (result.size() == 0) {
         return;
      }

      DeviceTypeEntity foundEntity = result.get(0);
      foundEntity.setKeepAliveIntervalSec(deviceTypeInfo.getKeepAliveIntervalSec());
      entityManager.persist(foundEntity);
      allDeviceTypeEntities.put(deviceTypeInfo.getType(), foundEntity);
   }

   public void saveDeviceType(@NotNull DeviceTypeInfo deviceTypeInfo) {
      if (allDeviceTypeEntities.get(deviceTypeInfo.getType()) != null) {
         modifyDeviceType(deviceTypeInfo);
      } else {
         addDeviceType(deviceTypeInfo);
      }
   }

   public List<DeviceTypeInfo> getAllDeviceTypes() {
      return allDeviceTypeEntities.values()
            .stream()
            .map(e -> new DeviceTypeInfo(e.getType(), e.getKeepAliveIntervalSec()))
            .sorted(Comparator.comparing(DeviceTypeInfo::getType))
            .collect(Collectors.toList());
   }

   private DeviceTypeEntity findDeviceTypeOrThrowException(String type) {
      DeviceTypeEntity persistedDeviceTypeEntity = allDeviceTypeEntities.get(type);

      if (persistedDeviceTypeEntity == null) {
         throw new DeviceSetupException(UNKNOWN_TYPE_ERROR + type);
      }
      return persistedDeviceTypeEntity;
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
}
