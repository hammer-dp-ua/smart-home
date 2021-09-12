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
import ua.dp.hammer.smarthome.controllers.SetupController;
import ua.dp.hammer.smarthome.models.FanRequestInfo;
import ua.dp.hammer.smarthome.models.FanResponse;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.StatusResponse;
import ua.dp.hammer.smarthome.models.alarms.AlarmInfo;
import ua.dp.hammer.smarthome.models.alarms.MotionDetector;
import ua.dp.hammer.smarthome.models.alarms.StreetMotionDetectors;
import ua.dp.hammer.smarthome.models.setup.DeviceSetupInfo;
import ua.dp.hammer.smarthome.models.setup.DeviceType;
import ua.dp.hammer.smarthome.models.states.AlarmsState;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.FanState;
import ua.dp.hammer.smarthome.repositories.DevicesRepository;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
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

   @Test
   public void testGetAllDevices(@Autowired TestRestTemplate restTemplate) {
      DeviceSetupInfo[] response = restTemplate.getForObject(getAllDevicesSetupUri(), DeviceSetupInfo[].class);

      assertThat(response).isNotNull();
      assertThat(response).isEmpty();
   }

   @Test
   public void testAddAndDeleteDevice(@Autowired TestRestTemplate restTemplate) {
      DeviceSetupInfo newDevice = new DeviceSetupInfo();
      newDevice.setName(null); // skip
      newDevice.setType(DeviceType.ENV_SENSOR);

      StatusResponse addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).isNotNull();
      assertThat(addDeviceSetupResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_NAME_ERROR);

      newDevice.setName("ABC");
      newDevice.setType(null);
      addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).isNotNull();
      assertThat(addDeviceSetupResponse.getErrorMessage()).isEqualTo(SetupController.EMPTY_TYPE_ERROR);

      newDevice.setType(DeviceType.PROJECTOR);
      newDevice.setIp4Address("192.168.0.55");
      addDeviceSetupResponse = restTemplate.postForObject(getAddDeviceSetupUri(), newDevice, StatusResponse.class);

      assertThat(addDeviceSetupResponse).isNotNull();
      assertThat(addDeviceSetupResponse.getStatusCode()).isEqualTo(StatusCodes.ERROR);
      assertThat(addDeviceSetupResponse.getErrorMessage()).startsWith(DevicesRepository.UNKNOWN_TYPE_ERROR);



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
      assertThat(allDevicesResponse).isNotEmpty();

      savedDevice = Arrays.stream(allDevicesResponse)
            .filter(device -> device.getName().equals(newDevice.getName()))
            .findFirst()
            .orElse(null);

      assertThat(savedDevice).isNull(); // Already deleted
   }

   //@Test
   public void testDeviceModification(@Autowired TestRestTemplate restTemplate) {
      DeviceSetupInfo newDevice = new DeviceSetupInfo();
      newDevice.setName("ABCD");
      newDevice.setType(DeviceType.ENV_SENSOR);

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
      modifiedDevice.setType(DeviceType.PROJECTOR);
      modifiedDevice.setIp4Address("192.168.0.57");

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

   //@Test
   public void testFanTurnedOff(@Autowired TestRestTemplate restTemplate) {
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

   //@Test
   public void testFanTurnedOnByItsSwitcherAndThenHumidityThresholdDetected(@Autowired TestRestTemplate restTemplate) {
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

   //@Test
   public void testFanTurnedOnByHumidityThreshold(@Autowired TestRestTemplate restTemplate) {
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

   //@Test
   public void testFanTurnedOnBySmartphoneAndThenHumidityThresholdDetected(@Autowired TestRestTemplate restTemplate) {
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

   //@Test
   public void testFanTurnedOnBySmartphoneWithTimeout(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
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

   //@Test
   public void testFanTurnedOnBySmartphoneWithTimeout2(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
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

   //@Test
   public void testFanTurnedOnByHumidityThresholdWithTimeout(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
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

   //@Test
   public void testAlarmSources(@Autowired TestRestTemplate restTemplate) {
      String deviceName = "Entrance Motion Detector";
      AlarmInfo alarmInfo1 = new AlarmInfo("ALARM_SOURCE_1", deviceName);
      AlarmInfo alarmInfo2 = new AlarmInfo("ALARM_SOURCE_2", deviceName);

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

   //@Test
   public void testAlarms(@Autowired TestRestTemplate restTemplate) throws InterruptedException, ExecutionException, TimeoutException {
      String deviceName = "Entrance Motion Detector";
      AlarmInfo alarmInfo = new AlarmInfo("ALARM_SOURCE_1", deviceName);

      StatusResponse added = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.ADD_ALARM_SOURCE_PATH, alarmInfo, StatusResponse.class);
      assertThat(added).isNotNull();
      assertThat(added.getStatusCode()).isEqualTo(StatusCodes.OK);

      AlarmsState alarmsStateResponse = restTemplate.getForObject(ManagerRestController.CONTROLLER_PATH +
                  ManagerRestController.IGNORE_ALARMS_PATH + "?timeout=1&doNotTurnOnProjectors=true", AlarmsState.class);
      assertThat(alarmsStateResponse).isNotNull();
      assertThat(alarmsStateResponse.isIgnoring()).isTrue();
      assertThat(alarmsStateResponse.getMinutesRemaining()).isEqualTo(1);

      ExecutorService executorService = Executors.newSingleThreadExecutor();
      FutureTask<StreetMotionDetectors> futureStatesResponse = new FutureTask<>(() ->
            restTemplate.getForObject(AlarmsMonitorRestController.CONTROLLER_PATH +
            AlarmsMonitorRestController.GET_CURRENT_STATES_DEFERRED, StreetMotionDetectors.class));
      executorService.execute(futureStatesResponse);

      ServerStatus alarmResponse = restTemplate.getForObject(Esp8266ExternalDevicesCommunicatorRestController.CONTROLLER_PATH +
            Esp8266ExternalDevicesCommunicatorRestController.ALARM_PATH + "?alarmSource=" + alarmInfo.getAlarmSource() +
                  "&deviceName=" + alarmInfo.getDeviceName(),
            ServerStatus.class);

      assertThat(alarmResponse).isNotNull();
      assertThat(alarmResponse.getStatusCode()).isEqualTo(StatusCodes.OK);

      StreetMotionDetectors statesResponse = futureStatesResponse.get(1, TimeUnit.SECONDS);
      assertThat(statesResponse).isNotNull();
      assertThat(statesResponse.getMotionDetectors().size()).isEqualTo(1);

      MotionDetector motionDetectorResponse = statesResponse.getMotionDetectors().iterator().next();
      assertThat(motionDetectorResponse.getName()).isEqualTo(alarmInfo.getDeviceName());
      assertThat(motionDetectorResponse.getSource()).isEqualTo(alarmInfo.getAlarmSource());

      StatusResponse deleted = restTemplate.postForObject(SetupController.CONTROLLER_PATH +
            SetupController.DELETE_ALARM_SOURCE_PATH, alarmInfo, StatusResponse.class);
      assertThat(deleted).isNotNull();
      assertThat(deleted.getStatusCode()).isEqualTo(StatusCodes.OK);
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
