package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.entities.DeviceTypeNameEntity;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;
import ua.dp.hammer.smarthome.models.setup.GeneralDevice;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.repositories.SettingsRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/server/devicesSetup")
public class DevicesSetupController {
   private static final Logger LOGGER = LogManager.getLogger(DevicesSetupController.class);

   private DevicesRepository devicesRepository;
   private SettingsRepository settingsRepository;

   @GetMapping(path = "/allDevices")
   public List<GeneralDevice> getAllDevices() {
      List<DeviceTypeNameEntity> entities = devicesRepository.getAllDeviceTypeNameEntities()
            .stream()
            .sorted(Comparator.comparingInt(d -> d.getType().getType().ordinal()))
            .collect(Collectors.toList());
      List<GeneralDevice> result = new ArrayList<>(entities.size());

      for (DeviceTypeNameEntity entity : entities) {
         GeneralDevice generalDevice = new GeneralDevice();

         generalDevice.setName(entity.getName());
         generalDevice.setType(entity.getType().getType());
         generalDevice.setKeepAliveIntervalSec(entity.getType().getKeepAliveIntervalSec());
         result.add(generalDevice);
      }
      return result;
   }

   @PostMapping(path = "/addDevice", consumes="application/json")
   public void addDevice(@RequestBody GeneralDevice device) {
      Assert.hasLength(device.getName(), "Name shouldn't be empty");
      Assert.notNull(device.getType(), "Type should't be empty");

      DeviceTypeNameEntity entity = new DeviceTypeNameEntity();

      entity.setName(device.getName());
      entity.setType(devicesRepository.getDeviceTypeEntity(device.getType()));
      entity.setIp4Address(device.getIp4Address());
      devicesRepository.saveNewDevice(entity);
   }

   @GetMapping(path = "/deleteDevice")
   public void deleteDevice(@RequestParam("deviceName") String deviceName) {
      devicesRepository.deleteDevice(deviceName);
   }

   @GetMapping(path = "/getFanSettings")
   public FanSettingsInfo getFanSettings(@RequestParam("name") String name) {
      return settingsRepository.getFanSettings(name);
   }

   @PostMapping(path = "/saveFanSettings", consumes="application/json")
   public void saveFanSettings(@RequestBody FanSettingsInfo fanSetup) {
      settingsRepository.saveFanSetting(fanSetup);
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }

   @Autowired
   public void setSettingsRepository(SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }
}
