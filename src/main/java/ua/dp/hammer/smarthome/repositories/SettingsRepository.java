package ua.dp.hammer.smarthome.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dp.hammer.smarthome.entities.FanSetupEntity;
import ua.dp.hammer.smarthome.models.FanSetupInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Transactional
@Repository
public class SettingsRepository {
   @PersistenceContext
   private EntityManager entityManager;

   private DevicesRepository devicesRepository;

   public FanSetupInfo getFanSetting(String name) {
      FanSetupEntity fanSetupEntity = getFanSettingSetup(name);
      FanSetupInfo fanSetupInfo = new FanSetupInfo();

      fanSetupInfo.setName(fanSetupEntity.getTypeName().getName());
      fanSetupInfo.setAfterFallingThresholdWorkTimeoutMinutes(fanSetupEntity.getAfterFallingThresholdWorkTimeoutMinutes());
      fanSetupInfo.setManuallyTurnedOnTimeoutMinutes(fanSetupEntity.getManuallyTurnedOnTimeoutMinutes());
      fanSetupInfo.setTurnOnHumidityThreshold(fanSetupEntity.getTurnOnHumidityThreshold());
      return fanSetupInfo;
   }

   public void saveFanSetting(FanSetupInfo newFanSetup) {
      FanSetupEntity currentFanSetup = getFanSettingSetup(newFanSetup.getName());

      currentFanSetup.setTurnOnHumidityThreshold(newFanSetup.getTurnOnHumidityThreshold());
      currentFanSetup.setManuallyTurnedOnTimeoutMinutes(newFanSetup.getManuallyTurnedOnTimeoutMinutes());
      currentFanSetup.setAfterFallingThresholdWorkTimeoutMinutes(newFanSetup.getAfterFallingThresholdWorkTimeoutMinutes());
      entityManager.persist(currentFanSetup);
   }

   public FanSetupEntity getFanSettingSetup(String name) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<FanSetupEntity> criteria = cb.createQuery(FanSetupEntity.class);
      Root<FanSetupEntity> root = criteria.from(FanSetupEntity.class);

      criteria.select(root).where(cb.equal(root.get("typeName").<String>get("name"), name));
      TypedQuery<FanSetupEntity> query = entityManager.createQuery(criteria);
      return query.getSingleResult();
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }
}
