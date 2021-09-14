package ua.dp.hammer.smarthome.boot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.dp.hammer.smarthome.controllers.AlarmsMonitorRestController;
import ua.dp.hammer.smarthome.controllers.Esp8266ExternalDevicesCommunicatorRestController;
import ua.dp.hammer.smarthome.controllers.ManagerRestController;
import ua.dp.hammer.smarthome.controllers.SettingsController;
import ua.dp.hammer.smarthome.controllers.SetupController;
import ua.dp.hammer.smarthome.models.FanRequestInfo;
import ua.dp.hammer.smarthome.models.FanResponse;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.StatusResponse;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;
import ua.dp.hammer.smarthome.models.alarms.MotionDetector;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceType;
import ua.dp.hammer.smarthome.models.setup.DeviceTypeInfo;
import ua.dp.hammer.smarthome.models.states.AlarmsState;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.FanState;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;
import ua.dp.hammer.smarthome.utils.Utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static ua.dp.hammer.smarthome.controllers.Esp8266ExternalDevicesCommunicatorRestController.BATHROOM_FAN_PATH;

/**
 * Spring Boot's DataSource initialization (spring.datasource.initialization-mode=always) will apply
 * classpath:schema.sql and classpath:data.sql files automatically without using @SqlGroup and @Sql on a test class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      args = {"--logging.config=log4j2_tests.xml", "--secondsInMinute=1", "--spring.datasource.initialization-mode=always"})
@ActiveProfiles("integration")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
//@Sql("classpath:schema.sql")
public class SpringBootTests {

   private final String FAN_NAME = "Bathroom fan";
   private final String MOTION_DETECTOR_NAME = "Motion detector";

   @Test
   public void testGetAllDevices(@Autowired TestRestTemplate restTemplate) {
      DeviceSetupInfo[] response = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);

      assertThat(response).isNotNull();
      assertThat(response).isEmpty();
   }

   @Test
   public void testAddAndDeleteDeviceType(@Autowired TestRestTemplate restTemplate) {
      DeviceTypeInfo[] allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);

      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse).isEmpty();

      DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
      StatusResponse addDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      assertThat(addDeviceTypeResponse).isNotNull();
      assertThat(addDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceTypeResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_TYPE_ERROR);

      deviceTypeInfo.setType(DeviceType.PROJECTOR.toString());
      addDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);
      assertThat(addDeviceTypeResponse).isNotNull();
      assertThat(addDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);
      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse.length).isEqualTo(1);
      assertThat(allDeviceTypesResponse[0].getType()).isEqualTo(deviceTypeInfo.getType());
      assertThat(allDeviceTypesResponse[0].getKeepAliveIntervalSec()).isNull();

      deviceTypeInfo.setKeepAliveIntervalSec(60);
      addDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);
      assertThat(addDeviceTypeResponse).isNotNull();
      assertThat(addDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);
      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse.length).isEqualTo(1);
      assertThat(allDeviceTypesResponse[0].getType()).isEqualTo(deviceTypeInfo.getType());
      assertThat(allDeviceTypesResponse[0].getKeepAliveIntervalSec()).isNull();

      StatusResponse modifyDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.MODIFY_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);
      assertThat(modifyDeviceTypeResponse).isNotNull();
      assertThat(modifyDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);
      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse.length).isEqualTo(1);
      assertThat(allDeviceTypesResponse[0].getType()).isEqualTo(deviceTypeInfo.getType());
      assertThat(allDeviceTypesResponse[0].getKeepAliveIntervalSec()).isEqualTo(deviceTypeInfo.getKeepAliveIntervalSec());

      DeviceTypeInfo deviceTypeInfo2 = new DeviceTypeInfo();
      deviceTypeInfo2.setType(DeviceType.ENV_SENSOR.toString());
      deviceTypeInfo2.setKeepAliveIntervalSec(30);

      addDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo2, StatusResponse.class);
      assertThat(addDeviceTypeResponse).isNotNull();
      assertThat(addDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);
      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse.length).isEqualTo(2);

      assertThat(allDeviceTypesResponse[0].getType()).isEqualTo(deviceTypeInfo2.getType());
      assertThat(allDeviceTypesResponse[0].getKeepAliveIntervalSec()).isEqualTo(deviceTypeInfo2.getKeepAliveIntervalSec());
      assertThat(allDeviceTypesResponse[1].getType()).isEqualTo(deviceTypeInfo.getType());
      assertThat(allDeviceTypesResponse[1].getKeepAliveIntervalSec()).isEqualTo(deviceTypeInfo.getKeepAliveIntervalSec());

      StatusResponse deleteDeviceTypeResponse = restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.DELETE_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      assertThat(deleteDeviceTypeResponse).isNotNull();
      assertThat(deleteDeviceTypeResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDeviceTypesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICE_TYPES_PATH,
            DeviceTypeInfo[].class);
      assertThat(allDeviceTypesResponse).isNotNull();
      assertThat(allDeviceTypesResponse.length).isEqualTo(1);
      assertThat(allDeviceTypesResponse[0].getType()).isEqualTo(deviceTypeInfo2.getType());
      assertThat(allDeviceTypesResponse[0].getKeepAliveIntervalSec()).isEqualTo(deviceTypeInfo2.getKeepAliveIntervalSec());
   }

   @Test
   public void testAddAndDeleteDevice(@Autowired TestRestTemplate restTemplate) {
      DeviceSetupInfo newDevice = new DeviceSetupInfo();
      newDevice.setName(null); // skip
      newDevice.setType(DeviceType.ENV_SENSOR.toString());

      StatusResponse addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_NAME_ERROR);

      newDevice.setName("ABC");
      newDevice.setType(null);
      addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_TYPE_ERROR);

      newDevice.setType(DeviceType.PROJECTOR.toString());
      newDevice.setIp4Address("192.168.0.55");
      addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).startsWith(DevicesRepository.UNKNOWN_TYPE_ERROR);

      DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
      deviceTypeInfo.setType(DeviceType.PROJECTOR.toString());
      restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      DeviceSetupInfo[] allDevicesResponse = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);

      assertThat(allDevicesResponse).isNotNull();
      assertThat(allDevicesResponse).isNotEmpty();

      DeviceSetupInfo savedDevice = Arrays.stream(allDevicesResponse)
            .filter(device -> device.getName().equals(newDevice.getName()))
            .findFirst()
            .orElse(null);

      assertThat(savedDevice).isNotNull();
      assertThat(savedDevice.getIp4Address()).isEqualTo(newDevice.getIp4Address());
      assertThat(savedDevice.getType()).isEqualTo(newDevice.getType());

      StatusResponse deleteDeviceResponse = restTemplate.getForObject(getDeleteDeviceUri(), StatusResponse.class);
      assertThat(deleteDeviceResponse).isNotNull();
      assertThat(deleteDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(deleteDeviceResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_ID_ERROR);

      deleteDeviceResponse = restTemplate.getForObject(getDeleteDeviceUri() + "?id=", StatusResponse.class);
      assertThat(deleteDeviceResponse).isNotNull();
      assertThat(deleteDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(deleteDeviceResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_ID_ERROR);

      deleteDeviceResponse = restTemplate.getForObject(getDeleteDeviceUri() + "?id=123456789", StatusResponse.class);
      assertThat(deleteDeviceResponse).isNotNull();
      assertThat(deleteDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(deleteDeviceResponse.getErrorMessage()).isEqualTo(DevicesRepository.DEVICE_DOESNT_EXIST_ERROR);

      deleteDeviceResponse = restTemplate.getForObject(getDeleteDeviceUri() + "?id=" + savedDevice.getId(), StatusResponse.class);
      assertThat(deleteDeviceResponse).isNotNull();
      assertThat(deleteDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDevicesResponse = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);

      assertThat(allDevicesResponse).isNotNull();
      assertThat(allDevicesResponse).isEmpty();
   }

   @Test
   public void testDeviceModification(@Autowired TestRestTemplate restTemplate) {
      DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
      deviceTypeInfo.setType(DeviceType.ENV_SENSOR.toString());
      restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      DeviceSetupInfo newDevice = new DeviceSetupInfo();
      newDevice.setName("ABCD");
      newDevice.setType(DeviceType.ENV_SENSOR.toString());

      restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);
      DeviceSetupInfo[] allDevicesResponse = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);
      DeviceSetupInfo savedDevice = Arrays.stream(allDevicesResponse)
            .filter(device -> device.getName().equals(newDevice.getName()))
            .findFirst()
            .orElse(null);
      assertThat(savedDevice).isNotNull();

      DeviceSetupInfo modifiedDevice = new DeviceSetupInfo();
      modifiedDevice.setId(null);
      modifiedDevice.setName(savedDevice.getName());
      modifiedDevice.setType(savedDevice.getType());
      StatusResponse modifyDeviceResponse = restTemplate.postForObject(getModifyDeviceUri(), modifiedDevice, StatusResponse.class);

      assertThat(modifyDeviceResponse).isNotNull();
      assertThat(modifyDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(modifyDeviceResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_ID_ERROR);

      modifiedDevice.setId(savedDevice.getId());
      modifiedDevice.setName(savedDevice.getName());
      modifiedDevice.setType(null);
      modifyDeviceResponse = restTemplate.postForObject(getModifyDeviceUri(), modifiedDevice, StatusResponse.class);

      assertThat(modifyDeviceResponse).isNotNull();
      assertThat(modifyDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(modifyDeviceResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_TYPE_ERROR);

      modifiedDevice.setId(savedDevice.getId());
      modifiedDevice.setName(null);
      modifiedDevice.setType(savedDevice.getType());

      modifyDeviceResponse = restTemplate.postForObject(getModifyDeviceUri(), modifiedDevice, StatusResponse.class);

      assertThat(modifyDeviceResponse).isNotNull();
      assertThat(modifyDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(modifyDeviceResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_NAME_ERROR);

      modifiedDevice.setName("ABCDE");
      modifiedDevice.setType(DeviceType.PROJECTOR.toString());
      modifiedDevice.setIp4Address("192.168.0.57");

      deviceTypeInfo = new DeviceTypeInfo();
      deviceTypeInfo.setType(DeviceType.PROJECTOR.toString());
      restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      modifyDeviceResponse = restTemplate.postForObject(getModifyDeviceUri(), modifiedDevice, StatusResponse.class);

      assertThat(modifyDeviceResponse).isNotNull();
      assertThat(modifyDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      allDevicesResponse = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);
      savedDevice = Arrays.stream(allDevicesResponse)
            .filter(device -> device.getName().equals(modifiedDevice.getName()))
            .findFirst()
            .orElse(null);

      assertThat(savedDevice).isNotNull();
      assertThat(savedDevice.getId()).isEqualTo(modifiedDevice.getId());
      assertThat(savedDevice.getType()).isEqualTo(modifiedDevice.getType());
      assertThat(savedDevice.getIp4Address()).isEqualTo(modifiedDevice.getIp4Address());

      StatusResponse deleteDeviceResponse = restTemplate.getForObject(getDeleteDeviceUri() + "?id=" + savedDevice.getId(), StatusResponse.class);
      assertThat(deleteDeviceResponse).isNotNull();
      assertThat(deleteDeviceResponse.getStatusCode()).isEqualTo(StatusCodes.OK);
   }

   @Test
   public void testFanTurnedOff(@Autowired TestRestTemplate restTemplate) {
      saveFanSetup(restTemplate);

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(10.0F);

      FanResponse fanResponse = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(fanResponse).isNotNull();
      assertThat(fanResponse.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(fanResponse.isTurnOn()).isFalse();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isNull();
   }

   @Test
   public void testFanTurnedOnByItsSwitcherAndThenHumidityThresholdDetected(@Autowired TestRestTemplate restTemplate) {
      saveFanSetup(restTemplate);

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(true);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(10);
      fanRequestInfo.setGain("-67.0");
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(10.0F);

      FanResponse fanResponse = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(fanResponse).isNotNull();
      assertThat(fanResponse.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(fanResponse.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();

      fanRequestInfo.setHumidity(99.0F);
      fanResponse = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(fanResponse).isNotNull();
      assertThat(fanResponse.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(fanResponse.isTurnOn()).isTrue();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
   }

   @Test
   public void testFanTurnedOnByHumidityThreshold(@Autowired TestRestTemplate restTemplate) {
      saveFanSetup(restTemplate);

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(99.0F);

      FanResponse response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
   }

   @Test
   public void testFanTurnedOnBySmartphoneAndThenHumidityThresholdDetected(@Autowired TestRestTemplate restTemplate) {
      saveFanSetup(restTemplate);

      FanState fanTurnOnResponse = restTemplate.getForObject(getFanTurnOnUri(), FanState.class);

      assertThat(fanTurnOnResponse).isNotNull();
      assertThat(fanTurnOnResponse.isTurnedOn()).isTrue();

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setGain("-67.0");
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(10.0F);

      FanResponse response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();

      fanRequestInfo.setHumidity(99.0F);

      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
   }

   @Test
   public void testFanTurnedOnBySmartphoneWithTimeout(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
      saveFanSetup(restTemplate);

      FanSettingsInfo fanSettingsRequest = new FanSettingsInfo();
      fanSettingsRequest.setManuallyTurnedOnTimeoutMinutes(1);
      fanSettingsRequest.setAfterFallingThresholdWorkTimeoutMinutes(30);
      fanSettingsRequest.setTurnOnHumidityThreshold(85.0F);
      fanSettingsRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingsUri(), fanSettingsRequest, Void.class);

      FanState fanTurnOnResponse = restTemplate.getForObject(getFanTurnOnUri(), FanState.class);

      assertThat(fanTurnOnResponse).isNotNull();
      assertThat(fanTurnOnResponse.isTurnedOn()).isTrue();

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setGain("-67.0");
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(10.0F);

      FanResponse response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(1);

      Thread.sleep(1500);

      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isFalse();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isFalse();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(0);
   }

   @Test
   public void testFanTurnedOnBySmartphoneWithTimeout2(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
      saveFanSetup(restTemplate);

      int timeout = 2;
      FanSettingsInfo fanSettingsRequest = new FanSettingsInfo();
      fanSettingsRequest.setManuallyTurnedOnTimeoutMinutes(timeout);
      fanSettingsRequest.setAfterFallingThresholdWorkTimeoutMinutes(30);
      fanSettingsRequest.setTurnOnHumidityThreshold(85.0F);
      fanSettingsRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingsUri(), fanSettingsRequest, Void.class);

      FanState fanTurnOnResponse = restTemplate.getForObject(getFanTurnOnUri(), FanState.class);

      assertThat(fanTurnOnResponse).isNotNull();
      assertThat(fanTurnOnResponse.isTurnedOn()).isTrue();

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setGain("-67.0");
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(10.0F);

      FanResponse response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout);

      Thread.sleep(1500);

      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout - 1);

      Thread.sleep(1000);

      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isFalse();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isFalse();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout - 2);
   }

   @Test
   public void testFanTurnedOnByHumidityThresholdWithTimeout(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
      saveFanSetup(restTemplate);

      int timeout = 2;
      FanSettingsInfo fanSettingsRequest = new FanSettingsInfo();
      fanSettingsRequest.setManuallyTurnedOnTimeoutMinutes(10);
      fanSettingsRequest.setAfterFallingThresholdWorkTimeoutMinutes(timeout);
      fanSettingsRequest.setTurnOnHumidityThreshold(85.0F);
      fanSettingsRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingsUri(), fanSettingsRequest, Void.class);

      FanRequestInfo fanRequestInfo = new FanRequestInfo();
      fanRequestInfo.setDeviceName(FAN_NAME);
      fanRequestInfo.setSwitchedOnManually(false);
      fanRequestInfo.setSwitchedOnManuallySecondsLeft(0);
      fanRequestInfo.setTemperature(27.0F);
      fanRequestInfo.setHumidity(99.0F);

      // Should start working
      FanResponse response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      AllManagerStates allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(0);

      // Should still work
      fanRequestInfo.setHumidity(80.0F);
      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout);

      Thread.sleep(1500);

      // Should still work
      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isTrue();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isTrue();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout - 1);

      Thread.sleep(1000);

      // Should stop working
      response = restTemplate.postForObject(getFanUri(), fanRequestInfo, FanResponse.class);

      assertThat(response).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(StatusCodes.OK);
      assertThat(response.isTurnOn()).isFalse();

      allStatesResponse = restTemplate.getForObject(getCurrentStatesUri(), AllManagerStates.class);

      assertThat(allStatesResponse).isNotNull();
      assertThat(allStatesResponse.getFanState().isNotAvailable()).isFalse();
      assertThat(allStatesResponse.getFanState().isTurnedOn()).isFalse();
      assertThat(allStatesResponse.getFanState().getMinutesRemaining()).isEqualTo(timeout - 2);
   }

   @Test
   public void testAlarmSourcesSetup(@Autowired TestRestTemplate restTemplate) {
      saveAlarmsSetup(restTemplate);

      AlarmInfo alarmInfo1 = new AlarmInfo("ALARM_SOURCE_1", MOTION_DETECTOR_NAME);
      AlarmInfo alarmInfo2 = new AlarmInfo("ALARM_SOURCE_2", MOTION_DETECTOR_NAME);

      AlarmInfo[] allAlarmSourcesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH +
            SetupController.GET_ALARM_SOURCES_PATH, AlarmInfo[].class);

      assertThat(allAlarmSourcesResponse).isNotNull();
      assertThat(allAlarmSourcesResponse.length).isEqualTo(0);

      StatusResponse added = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.ADD_ALARM_SOURCE_PATH, alarmInfo1, StatusResponse.class);
      assertThat(added).isNotNull();
      assertThat(added.getStatusCode()).isEqualTo(StatusCodes.OK);

      added = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.ADD_ALARM_SOURCE_PATH, alarmInfo2, StatusResponse.class);
      assertThat(added).isNotNull();
      assertThat(added.getStatusCode()).isEqualTo(StatusCodes.OK);

      allAlarmSourcesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH +
            SetupController.GET_ALARM_SOURCES_PATH, AlarmInfo[].class);

      assertThat(allAlarmSourcesResponse).isNotNull();
      assertThat(allAlarmSourcesResponse.length).isEqualTo(2);
      assertThat(allAlarmSourcesResponse[0]).isEqualTo(alarmInfo1);
      assertThat(allAlarmSourcesResponse[1]).isEqualTo(alarmInfo2);

      StatusResponse deleted = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.DELETE_ALARM_SOURCE_PATH, alarmInfo2, StatusResponse.class);
      assertThat(deleted).isNotNull();
      assertThat(deleted.getStatusCode()).isEqualTo(StatusCodes.OK);

      allAlarmSourcesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH +
            SetupController.GET_ALARM_SOURCES_PATH, AlarmInfo[].class);
      assertThat(allAlarmSourcesResponse).isNotNull();
      assertThat(allAlarmSourcesResponse.length).isEqualTo(1);
      assertThat(allAlarmSourcesResponse[0]).isEqualTo(alarmInfo1);

      deleted = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.DELETE_ALARM_SOURCE_PATH, alarmInfo1, StatusResponse.class);
      assertThat(deleted).isNotNull();
      assertThat(deleted.getStatusCode()).isEqualTo(StatusCodes.OK);

      allAlarmSourcesResponse = restTemplate.getForObject(SetupController.CONTROLLER_PATH +
            SetupController.GET_ALARM_SOURCES_PATH, AlarmInfo[].class);
      assertThat(allAlarmSourcesResponse).isNotNull();
      assertThat(allAlarmSourcesResponse.length).isEqualTo(0);
   }

   @Test
   public void testAlarmsAddAndHistory(@Autowired TestRestTemplate restTemplate) throws InterruptedException, ExecutionException, TimeoutException {
      saveAlarmsSetup(restTemplate);

      AlarmInfo alarmInfo1 = new AlarmInfo("ALARM_SOURCE_1", MOTION_DETECTOR_NAME);
      AlarmInfo alarmInfo2 = new AlarmInfo("ALARM_SOURCE_2", MOTION_DETECTOR_NAME);

      restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.ADD_ALARM_SOURCE_PATH, alarmInfo1, StatusResponse.class);
      restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.ADD_ALARM_SOURCE_PATH, alarmInfo2, StatusResponse.class);

      AlarmsState alarmsStateResponse = restTemplate.getForObject(ManagerRestController.CONTROLLER_PATH +
                  ManagerRestController.IGNORE_ALARMS_PATH + "?timeout=1&doNotTurnOnProjectors=true", AlarmsState.class);
      assertThat(alarmsStateResponse).isNotNull();
      assertThat(alarmsStateResponse.isIgnoring()).isTrue();
      assertThat(alarmsStateResponse.getMinutesRemaining()).isEqualTo(1);

      ServerStatus alarmResponse = restTemplate.getForObject(Esp8266ExternalDevicesCommunicatorRestController.CONTROLLER_PATH +
            Esp8266ExternalDevicesCommunicatorRestController.ALARM_PATH + "?alarmSource=" + alarmInfo1.getAlarmSource() +
                  "&deviceName=" + alarmInfo1.getDeviceName(),
            ServerStatus.class);

      assertThat(alarmResponse).isNotNull();
      assertThat(alarmResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      MotionDetector[] historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
            AlarmsMonitorRestController.GET_HISTORY,
            MotionDetector[].class);

      assertThat(historyResponse).isNotNull();
      assertThat(historyResponse.length).isEqualTo(1);

      Thread.sleep(100);

      alarmResponse = restTemplate.getForObject(Esp8266ExternalDevicesCommunicatorRestController.CONTROLLER_PATH +
            Esp8266ExternalDevicesCommunicatorRestController.ALARM_PATH + "?alarmSource=" + alarmInfo2.getAlarmSource() +
            "&deviceName=" + alarmInfo2.getDeviceName(),
            ServerStatus.class);

      assertThat(alarmResponse).isNotNull();
      assertThat(alarmResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY,
            MotionDetector[].class);

      assertThat(historyResponse).isNotNull();
      assertThat(historyResponse.length).isEqualTo(2);
      assertThat(historyResponse[0].getName()).isEqualTo(alarmInfo2.getDeviceName());
      assertThat(historyResponse[0].getSource()).isEqualTo(alarmInfo2.getAlarmSource());
      assertThat(historyResponse[1].getName()).isEqualTo(alarmInfo1.getDeviceName());
      assertThat(historyResponse[1].getSource()).isEqualTo(alarmInfo1.getAlarmSource());

      LocalDateTime currentTime = LocalDateTime.now();
      long minus1Hour = Utils.localDateTimeToMilli(currentTime.minusHours(1));
      long plus1Hour = Utils.localDateTimeToMilli(currentTime.plusHours(1));

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY + "?fromMs=" + minus1Hour,
            MotionDetector[].class);

      assertThat(historyResponse.length).isEqualTo(2);

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY + "?fromMs=" + plus1Hour,
            MotionDetector[].class);

      assertThat(historyResponse.length).isEqualTo(0);

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY + "?toMs=" + minus1Hour,
            MotionDetector[].class);

      assertThat(historyResponse.length).isEqualTo(0);

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY + "?toMs=" + plus1Hour,
            MotionDetector[].class);

      assertThat(historyResponse.length).isEqualTo(2);

      historyResponse = restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
                  AlarmsMonitorRestController.GET_HISTORY + "?fromMs=" + minus1Hour + "&toMs=" + plus1Hour,
            MotionDetector[].class);

      assertThat(historyResponse.length).isEqualTo(2);
   }

   private void saveFanSetup(TestRestTemplate restTemplate) {
      DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
      deviceTypeInfo.setType(DeviceType.ENV_SENSOR.toString());
      restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      DeviceSetupInfo fanDevice = new DeviceSetupInfo();
      fanDevice.setName(FAN_NAME);
      fanDevice.setType(DeviceType.ENV_SENSOR.toString());

      restTemplate.postForObject(getAddDeviceSetupUri(), fanDevice, StatusResponse.class);

      FanSettingsInfo fanSettingsInfo = new FanSettingsInfo();
      fanSettingsInfo.setName(FAN_NAME);
      fanSettingsInfo.setTurnOnHumidityThreshold(85.0F);
      fanSettingsInfo.setManuallyTurnedOnTimeoutMinutes(10);
      fanSettingsInfo.setAfterFallingThresholdWorkTimeoutMinutes(10);

      restTemplate.postForObject(SettingsController.CONTROLLER_PATH + SettingsController.SAVE_FAN_SETTINGS_PATH,
            fanSettingsInfo, StatusResponse.class);
   }

   private void saveAlarmsSetup(TestRestTemplate restTemplate) {
      DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
      deviceTypeInfo.setType(DeviceType.MOTION_DETECTOR.toString());
      restTemplate.postForObject(SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_TYPE_PATH,
            deviceTypeInfo, StatusResponse.class);

      DeviceSetupInfo motionDetector = new DeviceSetupInfo();
      motionDetector.setName(MOTION_DETECTOR_NAME);
      motionDetector.setType(DeviceType.MOTION_DETECTOR.toString());

      restTemplate.postForObject(getAddDeviceSetupUri(), motionDetector, StatusResponse.class);
   }

   private String getFanUri() {
      return Esp8266ExternalDevicesCommunicatorRestController.CONTROLLER_PATH + BATHROOM_FAN_PATH;
   }

   private String getFanTurnOnUri() {
      return "/server/manager/turnOnBathroomFan";
   }

   private String getCurrentStatesUri() {
      return "/server/manager/getCurrentStates";
   }

   private String getSaveFanSettingsUri() {
      return "/server/settings/saveFanSettings";
   }

   private String getAllDevicesSetupUri() {
      return SetupController.CONTROLLER_PATH + SetupController.GET_ALL_DEVICES_PATH;
   }

   private String getAddDeviceSetupUri() {
      return SetupController.CONTROLLER_PATH + SetupController.ADD_DEVICE_PATH;
   }

   private String getDeleteDeviceUri() {
      return SetupController.CONTROLLER_PATH + SetupController.DELETE_DEVICE_PATH;
   }

   private String getModifyDeviceUri() {
      return SetupController.CONTROLLER_PATH + SetupController.MODIFY_DEVICE_PATH;
   }
}
