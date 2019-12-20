package ua.dp.hammer.smarthome.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.StringUtils;
import ua.dp.hammer.smarthome.entities.DeviceType;
import ua.dp.hammer.smarthome.entities.DeviceTypeEntity;
import ua.dp.hammer.smarthome.entities.DeviceTypeNameEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;
import ua.dp.hammer.smarthome.models.DeviceInfo;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Repository
public class CommonDevicesRepository {
   private static final Logger LOGGER = LogManager.getLogger(CommonDevicesRepository.class);

   // Something like second level cache
   private Map<DeviceType, DeviceTypeEntity> allDeviceTypeEntities = new HashMap<>();
   private Map<String, DeviceTypeNameEntity> allDeviceTypeNameEntities = new HashMap<>();

   @PersistenceContext
   private EntityManager entityManager;

   @PostConstruct
   public void init() {
      loadAndSaveAllDeviceTypeEntities();
      loadAndSaveAllDeviceTypeNameEntities();
   }

   public void saveTechnicalDeviceInfo(TechnicalDeviceInfoEntity infoEntity, String deviceName) {
      DeviceTypeNameEntity deviceTypeName = getDeviceTypeNameEntity(deviceName);

      if (deviceTypeName == null) {
         LOGGER.warn("Unknown device name: '" + deviceName + "'. Add it into a list of known devices.");
         return;
      }

      infoEntity.setTypeName(deviceTypeName);
      entityManager.persist(infoEntity);
   }

   public DeviceTypeNameEntity getDeviceTypeNameEntity(String deviceName) {
      return allDeviceTypeNameEntities.get(deviceName);
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

      for (DeviceTypeEntity entity : result) {
         allDeviceTypeEntities.put(entity.getType(), entity);
      }
   }

   private void loadAndSaveAllDeviceTypeNameEntities() {
      TypedQuery<DeviceTypeNameEntity> query =
            entityManager.createQuery("from " + DeviceTypeNameEntity.class.getSimpleName() + " e", DeviceTypeNameEntity.class);
      List<DeviceTypeNameEntity> result = query.getResultList();
      LOGGER.info("Loaded " + result.size() + " instances of " + DeviceTypeNameEntity.class.getSimpleName());

      for (DeviceTypeNameEntity entity : result) {
         allDeviceTypeNameEntities.put(entity.getName(), entity);
      }
   }
}
