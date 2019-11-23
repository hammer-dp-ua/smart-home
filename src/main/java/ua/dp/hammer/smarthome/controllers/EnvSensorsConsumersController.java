package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.beans.EnvSensorsBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.repositories.EnvSensorsRepository;

import java.util.Collection;

@RestController
@RequestMapping(path = "/server/envSensors")
public class EnvSensorsConsumersController {
   private static final Logger LOGGER = LogManager.getLogger(EnvSensorsConsumersController.class);

   private MainLogic mainLogic;
   private EnvSensorsBean envSensorsBean;
   private EnvSensorsRepository envSensorsRepository;

   @GetMapping(path = "/info/allSensors")
   public Collection<DeviceInfo> getAllSensorsInfo() {
      return envSensorsBean.getEnvSensors();
   }

   @GetMapping(path = "/dbVersion")
   public String getDbVersion() {
      return envSensorsRepository.loadDbVersion();
   }

   @Autowired
   public void setMainLogic(MainLogic mainLogic) {
      this.mainLogic = mainLogic;
   }

   @Autowired
   public void setEnvSensorsBean(EnvSensorsBean envSensorsBean) {
      this.envSensorsBean = envSensorsBean;
   }

   @Autowired
   public void setEnvSensorsRepository(EnvSensorsRepository envSensorsRepository) {
      this.envSensorsRepository = envSensorsRepository;
   }
}
