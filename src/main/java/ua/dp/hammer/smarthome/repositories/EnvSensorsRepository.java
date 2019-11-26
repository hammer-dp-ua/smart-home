package ua.dp.hammer.smarthome.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Transactional
@Repository
public class EnvSensorsRepository {
   private static final Logger LOGGER = LogManager.getLogger(EnvSensorsRepository.class);

   private EntityManager entityManager;
   private CommonDevicesRepository commonDevicesRepository;

   public String loadDbVersion() {
      Query query = entityManager.createNativeQuery("SELECT version()");
      return (String) query.getSingleResult();
   }

   @PersistenceContext
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }

   //@Autowired
   public void setCommonDevicesRepository(CommonDevicesRepository commonDevicesRepository) {
      this.commonDevicesRepository = commonDevicesRepository;
   }
}
