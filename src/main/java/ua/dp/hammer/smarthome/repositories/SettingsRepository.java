package ua.dp.hammer.smarthome.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.FanSetupEntity;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Transactional
@Repository
public class SettingsRepository {
   public static final String FAN_SETUP_SETTINGS_DOESNT_EXIST_ERROR = "Fan settings doesn't exist: ";

   @PersistenceContext
   private EntityManager entityManager;

   private DevicesRepository devicesRepository;

   public FanSettingsInfo getFanSettings(String name) {
      FanSetupEntity fanSetupEntity = getFanSettingSetupOrThrowException(name);
      FanSettingsInfo fanSettingsInfo = new FanSettingsInfo();

      fanSettingsInfo.setName(fanSetupEntity.getTypeName().getName());
      fanSettingsInfo.setAfterFallingThresholdWorkTimeoutMinutes(fanSetupEntity.getAfterFallingThresholdWorkTimeoutMinutes());
      fanSettingsInfo.setManuallyTurnedOnTimeoutMinutes(fanSetupEntity.getManuallyTurnedOnTimeoutMinutes());
      fanSettingsInfo.setTurnOnHumidityThreshold(fanSetupEntity.getTurnOnHumidityThreshold());
      return fanSettingsInfo;
   }

   public void saveFanSetting(FanSettingsInfo newFanSetup) {
      FanSetupEntity currentFanSetup = getFanSettingSetup(newFanSetup.getName());

      if (currentFanSetup == null) {
         currentFanSetup = new FanSetupEntity();
         currentFanSetup.setTypeName(devicesRepository.getDeviceTypeNameEntityOrThrowException(newFanSetup.getName()));
      }

      currentFanSetup.setTurnOnHumidityThreshold(newFanSetup.getTurnOnHumidityThreshold());
      currentFanSetup.setManuallyTurnedOnTimeoutMinutes(newFanSetup.getManuallyTurnedOnTimeoutMinutes());
      currentFanSetup.setAfterFallingThresholdWorkTimeoutMinutes(newFanSetup.getAfterFallingThresholdWorkTimeoutMinutes());
      entityManager.persist(currentFanSetup);
   }

   public FanSetupEntity getFanSettingSetupOrThrowException(String name) {
      FanSetupEntity fanSetupEntity = getFanSettingSetup(name);

      if (fanSetupEntity == null) {
         throw new DeviceSetupException(FAN_SETUP_SETTINGS_DOESNT_EXIST_ERROR + name);
      }
      return fanSetupEntity;
   }

   private FanSetupEntity getFanSettingSetup(String name) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<FanSetupEntity> criteria = cb.createQuery(FanSetupEntity.class);
      Root<FanSetupEntity> root = criteria.from(FanSetupEntity.class);

      criteria.select(root).where(cb.equal(root.get("typeName").<String>get("name"), name));
      TypedQuery<FanSetupEntity> query = entityManager.createQuery(criteria);
      return query.getResultList().size() == 0 ? null : query.getResultList().get(0);
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
