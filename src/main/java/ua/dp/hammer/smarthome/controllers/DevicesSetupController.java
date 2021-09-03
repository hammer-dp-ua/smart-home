package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.entities.DeviceSetupEntity;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.StatusResponse;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.repositories.SettingsRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = DevicesSetupController.CONTROLLER_PATH)
public class DevicesSetupController {
   public static final String CONTROLLER_PATH = "/server/devicesSetup";
   public static final String ADD_ALARM_SOURCE_PATH = "/addAlarmSource";
   public static final String DELETE_ALARM_SOURCE_PATH = "/deleteAlarmSource";
   public static final String GET_ALARM_SOURCES_PATH = "/getAlarmSources";

   public static final String EMPTY_NAME_ERROR = "Name shouldn't be empty";
   public static final String EMPTY_TYPE_ERROR = "Type shouldn't be empty";
   public static final String EMPTY_ID_ERROR = "ID shouldn't be empty";

   private static final Logger LOGGER = LogManager.getLogger(DevicesSetupController.class);

   private DevicesRepository devicesRepository;
   private SettingsRepository settingsRepository;

   @GetMapping(path = "/allDevices")
   public List<DeviceSetupInfo> getAllDevices() {
      List<DeviceSetupEntity> entities = devicesRepository.getAllDeviceTypeNameEntities()
            .stream()
            .sorted(Comparator.comparingInt(d -> d.getType().getType().ordinal()))
            .collect(Collectors.toList());
      List<DeviceSetupInfo> result = new ArrayList<>(entities.size());

      for (DeviceSetupEntity entity : entities) {
         DeviceSetupInfo deviceSetupInfo = new DeviceSetupInfo();

         deviceSetupInfo.setId(entity.getId());
         deviceSetupInfo.setName(entity.getName());
         deviceSetupInfo.setType(entity.getType().getType());
         deviceSetupInfo.setKeepAliveIntervalSec(entity.getType().getKeepAliveIntervalSec());
         deviceSetupInfo.setIp4Address(entity.getIp4Address());
         result.add(deviceSetupInfo);
      }
      return result;
   }

   @PostMapping(path = "/addDevice", consumes="application/json")
   public StatusResponse addDevice(@RequestBody DeviceSetupInfo device) {
      if (StringUtils.isEmpty(device.getName())) {
         throw new DeviceSetupException(EMPTY_NAME_ERROR);
      }
      if (device.getType() == null) {
         throw new DeviceSetupException(EMPTY_TYPE_ERROR);
      }

      DeviceSetupEntity entity = new DeviceSetupEntity();

      entity.setName(device.getName());
      entity.setType(devicesRepository.getDeviceTypeEntity(device.getType()));
      entity.setIp4Address(device.getIp4Address());
      devicesRepository.saveNewDevice(entity);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = "/deleteDevice")
   public StatusResponse deleteDevice(@RequestParam(name = "id", required = false) Integer id) {
      if (id == null) {
         throw new DeviceSetupException(EMPTY_ID_ERROR);
      }

      devicesRepository.deleteDevice(id);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = "/modifyDevice")
   public StatusResponse modifyDevice(@RequestBody DeviceSetupInfo device) {
      if (device.getId() == null) {
         throw new DeviceSetupException(EMPTY_ID_ERROR);
      }
      if (device.getType() == null) {
         throw new DeviceSetupException(EMPTY_TYPE_ERROR);
      }
      if (device.getName() == null) {
         throw new DeviceSetupException(EMPTY_NAME_ERROR);
      }

      devicesRepository.modifyDevice(device);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = ADD_ALARM_SOURCE_PATH)
   public StatusResponse addAlarmSource(@RequestParam("source") String source) {
      devicesRepository.addAlarmSource(source);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = DELETE_ALARM_SOURCE_PATH)
   public StatusResponse deleteAlarmSource(@RequestParam("source") String source) {
      devicesRepository.deleteAlarmSource(source);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = GET_ALARM_SOURCES_PATH)
   public List<String> getAlarmSources() {
      return devicesRepository.getAlarmSources();
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
