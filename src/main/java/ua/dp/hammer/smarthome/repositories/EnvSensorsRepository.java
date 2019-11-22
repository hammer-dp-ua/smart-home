package ua.dp.hammer.smarthome.repositories;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class EnvSensorsRepository {
   @PersistenceContext
   private EntityManager entityManager;

   public String loadDbVersion() {
      Query query = entityManager.createNativeQuery("SELECT version()");
      return (String) query.getSingleResult();
   }
}
