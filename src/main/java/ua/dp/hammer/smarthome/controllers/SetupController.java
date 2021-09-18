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
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.StatusResponse;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceTypeInfo;
import ua.dp.hammer.smarthome.repositories.AlarmSourcesSetupRepository;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = SetupController.CONTROLLER_PATH)
public class SetupController {
   public static final String CONTROLLER_PATH = "/server/setup";
   public static final String GET_ALL_DEVICES_PATH = "/allDevices";
   public static final String ADD_DEVICE_PATH = "/addDevice";
   public static final String DELETE_DEVICE_PATH = "/deleteDevice";
   public static final String MODIFY_DEVICE_PATH = "/modifyDevice";
   public static final String ADD_DEVICE_TYPE_PATH = "/addDeviceType";
   public static final String DELETE_DEVICE_TYPE_PATH = "/deleteDeviceType";
   public static final String MODIFY_DEVICE_TYPE_PATH = "/modifyDeviceType";
   public static final String GET_ALL_DEVICE_TYPES_PATH = "/getAllDeviceTypes";
   public static final String ADD_ALARM_SOURCE_PATH = "/addAlarmSource";
   public static final String DELETE_ALARM_SOURCE_PATH = "/deleteAlarmSource";
   public static final String GET_ALARM_SOURCES_PATH = "/getAlarmSources";

   public static final String EMPTY_NAME_ERROR = "Name shouldn't be empty";
   public static final String EMPTY_TYPE_ERROR = "Type shouldn't be empty";
   public static final String EMPTY_ID_ERROR = "ID shouldn't be empty";

   private static final Logger LOGGER = LogManager.getLogger(SetupController.class);

   private DevicesRepository devicesRepository;
   private AlarmSourcesSetupRepository alarmSourcesSetupRepository;

   @GetMapping(path = GET_ALL_DEVICES_PATH)
   public List<DeviceSetupInfo> getAllDevices() {
      List<DeviceSetupEntity> entities = devicesRepository.getAllDeviceTypeNameEntities()
            .stream()
            .sorted(Comparator.comparing(d -> d.getType().getType()))
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

   @PostMapping(path = ADD_DEVICE_PATH, consumes="application/json")
   public StatusResponse addDevice(@RequestBody DeviceSetupInfo device) {
      if (StringUtils.isEmpty(device.getName())) {
         throw new DeviceSetupException(EMPTY_NAME_ERROR);
      }
      if (device.getType() == null) {
         throw new DeviceSetupException(EMPTY_TYPE_ERROR);
      }

      devicesRepository.saveNewDevice(device);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = DELETE_DEVICE_PATH)
   public StatusResponse deleteDevice(@RequestParam(name = "id", required = false) Integer id) {
      if (id == null) {
         throw new DeviceSetupException(EMPTY_ID_ERROR);
      }

      devicesRepository.deleteDevice(id);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = MODIFY_DEVICE_PATH)
   public StatusResponse modifyDevice(@RequestBody DeviceSetupInfo device) {
      if (device.getId() == null) {
         throw new DeviceSetupException(EMPTY_ID_ERROR);
      }
      if (StringUtils.isEmpty(device.getType())) {
         throw new DeviceSetupException(EMPTY_TYPE_ERROR);
      }
      if (StringUtils.isEmpty(device.getName())) {
         throw new DeviceSetupException(EMPTY_NAME_ERROR);
      }

      devicesRepository.modifyDevice(device);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = ADD_DEVICE_TYPE_PATH)
   public StatusResponse addDeviceType(@RequestBody DeviceTypeInfo deviceTypeInfo) {
      if (StringUtils.isEmpty(deviceTypeInfo.getType())) {
         throw new DeviceSetupException(EMPTY_TYPE_ERROR);
      }

      devicesRepository.addDeviceType(deviceTypeInfo);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = DELETE_DEVICE_TYPE_PATH)
   public StatusResponse deleteDeviceType(@RequestBody DeviceTypeInfo deviceTypeInfo) {
      devicesRepository.deleteDeviceType(deviceTypeInfo);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = MODIFY_DEVICE_TYPE_PATH)
   public StatusResponse modifyDeviceType(@RequestBody DeviceTypeInfo deviceTypeInfo) {
      devicesRepository.modifyDeviceType(deviceTypeInfo);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = GET_ALL_DEVICE_TYPES_PATH)
   public List<DeviceTypeInfo> getAllDeviceTypes() {
      return devicesRepository.getAllDeviceTypes();
   }

   @PostMapping(path = ADD_ALARM_SOURCE_PATH)
   public StatusResponse addAlarmSource(@RequestBody AlarmInfo alarmInfo) {
      alarmSourcesSetupRepository.addAlarmSource(alarmInfo);
      return new StatusResponse(StatusCodes.OK);
   }

   @PostMapping(path = DELETE_ALARM_SOURCE_PATH)
   public StatusResponse deleteAlarmSource(@RequestBody AlarmInfo alarmInfo) {
      alarmSourcesSetupRepository.deleteAlarmSource(alarmInfo);
      return new StatusResponse(StatusCodes.OK);
   }

   @GetMapping(path = GET_ALARM_SOURCES_PATH)
   public List<AlarmInfo> getAlarmSources() {
      return alarmSourcesSetupRepository.getAlarmSources()
            .stream()
            .map(e -> new AlarmInfo(e.getSource(), e.getDeviceSetup().getName(), e.isIgnoreAlarms()))
            .collect(Collectors.toList());
   }

   @Autowired
   public void setDevicesRepository(DevicesRepository devicesRepository) {
      this.devicesRepository = devicesRepository;
   }

   @Autowired
   public void setAlarmSourcesSetupRepository(AlarmSourcesSetupRepository alarmSourcesSetupRepository) {
      this.alarmSourcesSetupRepository = alarmSourcesSetupRepository;
   }
}
