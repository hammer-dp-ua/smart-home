package ua.dp.hammer.smarthome.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.DeviceType;
import ua.dp.hammer.smarthome.entities.DeviceTypeEntity;
import ua.dp.hammer.smarthome.entities.TechnicalDeviceInfoEntity;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Repository
public class EnvSensorsRepository {
   private static final Logger LOGGER = LogManager.getLogger(EnvSensorsRepository.class);

   // Something like second level cache
   private Map<DeviceType, DeviceTypeEntity> allDeviceTypeEntities = new HashMap<>();

   @PersistenceContext
   private EntityManager entityManager;

   @PostConstruct
   public void init() {
      loadAndSaveAllDeviceTypeEntities();
   }

   public void saveEnvSensorInfo(TechnicalDeviceInfoEntity infoEntity) {
      infoEntity.setType(allDeviceTypeEntities.get(DeviceType.ENV_SENSOR));
      entityManager.persist(infoEntity);
   }

   public DeviceTypeEntity getDeviceTypeEntity(DeviceType deviceType) {
      return allDeviceTypeEntities.get(deviceType);
   }

   public String loadDbVersion() {
      Query query = entityManager.createNativeQuery("SELECT version()");
      return (String) query.getSingleResult();
   }

   public void loadAndSaveAllDeviceTypeEntities() {
      TypedQuery<DeviceTypeEntity> query =
            entityManager.createQuery("from " + DeviceTypeEntity.class.getSimpleName() + " e", DeviceTypeEntity.class);
      List<DeviceTypeEntity> result = query.getResultList();
      LOGGER.info("Loaded " + result.size() + " instances of " + DeviceTypeEntity.class.getSimpleName());

      for (DeviceTypeEntity entity : result) {
         allDeviceTypeEntities.put(entity.getType(), entity);
      }
   }
}
