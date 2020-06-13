package ua.dp.hammer.smarthome.boot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.dp.hammer.smarthome.models.FanRequestInfo;
import ua.dp.hammer.smarthome.models.FanResponse;
import ua.dp.hammer.smarthome.models.FanSetupInfo;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.FanState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      args = {"--logging.config=log4j2_tests.xml", "--secondsInMinute=1"})
@ActiveProfiles("integration")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class SpringBootTests {

   private final String FAN_NAME = "Bathroom fan";

   @Test
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

   @Test
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

   @Test
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

   @Test
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

   @Test
   public void testFanTurnedOnBySmartphoneWithTimeout(@Autowired TestRestTemplate restTemplate) throws InterruptedException {
      FanSetupInfo fanSetupRequest = new FanSetupInfo();
      fanSetupRequest.setManuallyTurnedOnTimeoutMinutes(1);
      fanSetupRequest.setAfterFallingThresholdWorkTimeoutMinutes(30);
      fanSetupRequest.setTurnOnHumidityThreshold(85.0F);
      fanSetupRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingUri(), fanSetupRequest, Void.class);

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
      int timeout = 2;
      FanSetupInfo fanSetupRequest = new FanSetupInfo();
      fanSetupRequest.setManuallyTurnedOnTimeoutMinutes(timeout);
      fanSetupRequest.setAfterFallingThresholdWorkTimeoutMinutes(30);
      fanSetupRequest.setTurnOnHumidityThreshold(85.0F);
      fanSetupRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingUri(), fanSetupRequest, Void.class);

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
      int timeout = 2;
      FanSetupInfo fanSetupRequest = new FanSetupInfo();
      fanSetupRequest.setManuallyTurnedOnTimeoutMinutes(10);
      fanSetupRequest.setAfterFallingThresholdWorkTimeoutMinutes(timeout);
      fanSetupRequest.setTurnOnHumidityThreshold(85.0F);
      fanSetupRequest.setName(FAN_NAME);

      restTemplate.postForObject(getSaveFanSettingUri(), fanSetupRequest, Void.class);

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

   private String getFanUri() {
      return "/server/esp8266/bathroomFan";
   }

   private String getFanTurnOnUri() {
      return "/server/manager/turnOnBathroomFan";
   }

   private String getCurrentStatesUri() {
      return "/server/manager/getCurrentStates";
   }

   private String getSaveFanSettingUri() {
      return "/server/devicesSetup/saveFanSetting";
   }
}
