package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.CameraBean;
import ua.dp.hammer.smarthome.beans.ImmobilizerBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.Timer;
import java.util.TimerTask;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorController.class);

   private static final String LOGGER_DEBUG_INFO = "Gain of %1$s (%2$s): %3$sdB" +
         "\r\nErrors: %4$d" +
         "\r\nOverrun Errors: %5$d" +
         "\r\nIdle Line Detections: %6$d" +
         "\r\nNoise Detection: %7$d" +
         "\r\nFraming Errors: %8$d" +
         "\r\nLast Error Task: %9$d" +
         "\r\nUSART data: %10$s" +
         "\r\nBuild timestamp: %11$s";

   private int manuallyTurnedOnFanTimeoutMinutes;
   private String ipAddressToUpdateFirmware;

   @Autowired
   private Environment environment;

   @Autowired
   private CameraBean cameraBean;

   @Autowired
   private ImmobilizerBean immobilizerBean;

   @Autowired
   private MainLogic mainLogic;

   @PostConstruct
   public void init() {
      manuallyTurnedOnFanTimeoutMinutes = Integer.parseInt(environment.getRequiredProperty("manuallyTurnedOnFanTimeoutMinutes"));
   }

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Request esp8266Request, @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
         serverStatus.setIncludeDebugInfo(true);
      }
      return serverStatus;
   }

   @GetMapping(path = "/alarm")
   public ServerStatus receiveAlarm(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Alarm: " + clientIp);

      LocalDateTime immobilizerActivatedDateTime = immobilizerBean.getActivationDateTime();

      mainLogic.receiveAlarm();

      if (immobilizerActivatedDateTime != null) {
         LocalDateTime currentDateTime = LocalDateTime.now();
         long durationBetweenImmobilizerAndAlarm = Duration.between(currentDateTime, immobilizerActivatedDateTime).abs().getSeconds();

         if (durationBetweenImmobilizerAndAlarm >= 120) {
            cameraBean.startVideoRecording();
         } else {
            LOGGER.info("Video recording wasn't started because immobilizer was activated " + durationBetweenImmobilizerAndAlarm +
            " seconds ago");
         }
      } else {
         cameraBean.startVideoRecording();
      }
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/falseAlarm")
   public ServerStatus receiveFalseAlarm(@RequestHeader("X-FORWARDED-FOR") String clientIp,
                                         @RequestParam("alarmSource") String alarmSource) {
      LOGGER.info("False alarm from " + alarmSource + ": " + clientIp);
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerActivated")
   public ServerStatus receiveImmobilizerActivation(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Immobilizer activated: " + clientIp);

      immobilizerBean.setActivationDateTime(LocalDateTime.now());
      mainLogic.turnProjectorsOn();
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerDeactivated")
   public ServerStatus receiveImmobilizerDeactivation(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Immobilizer deactivated: " + clientIp);

      immobilizerBean.setDeactivationDateTime(LocalDateTime.now());
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/updateFirmware")
   public String updateFirmware(@RequestParam("alarmSource") String ipAddress) {
      ipAddressToUpdateFirmware = ipAddress;
      return ipAddress;
   }

   @PostMapping(path = "/projectorDeferred", consumes="application/json")
   public DeferredResult<ProjectorResponse> sendProjectorDeferredResult(@RequestBody Esp8266Request esp8266Request,
                                                                        @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
      }

      DeferredResult<ProjectorResponse> projectorDeferredResult = new DeferredResult<>();
      mainLogic.addProjectorsDeferredResult(projectorDeferredResult, clientIp, esp8266Request.isServerIsAvailable());
      return projectorDeferredResult;
   }

   @PostMapping(path = "/testDeferred", consumes="application/json")
   public DeferredResult<ProjectorResponse> sendDeferredResult(@RequestBody Esp8266Request esp8266Request,
                                                               @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
      }

      DeferredResult<ProjectorResponse> deferredResult = new DeferredResult<>();
      final ProjectorResponse response = new ProjectorResponse(StatusCodes.OK);

      response.setTurnOn(false);
      if (clientIp != null && clientIp.equals(ipAddressToUpdateFirmware)) {
         response.setUpdateFirmware(true);
         ipAddressToUpdateFirmware = null;

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Firmware of " + clientIp + "(" + esp8266Request.getDeviceName() + ") will be updated");
         }
      }

      if (esp8266Request.isServerIsAvailable()) {
         Timer timer = new Timer();

         timer.schedule(new TimerTask() {
            @Override
            public void run() {
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("testDeferred response is set");
               }
               deferredResult.setResult(response);
            }
         }, 1 * 60 * 1000);
      } else {
         deferredResult.setResult(response);
      }
      return deferredResult;
   }

   @PostMapping(path = "/bathroomFan", consumes="application/json")
   public FanResponse receiveBathroomParameters(@RequestBody Esp8266Request esp8266Request,
                                                @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      FanResponse fanResponse = new FanResponse(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         writeGeneralDebugInfo(clientIp, esp8266Request);
         LOGGER.debug("Bathroom info. Humidity: " + esp8266Request.getHumidity() + "; Temperature: " + esp8266Request.getTemperature());
         fanResponse.setIncludeDebugInfo(true);
      }

      fanResponse.setTurnOn(mainLogic.getBathroomFanState(esp8266Request.getHumidity(), esp8266Request.getTemperature()));
      fanResponse.setManuallyTurnedOnTimeout(manuallyTurnedOnFanTimeoutMinutes);
      return fanResponse;
   }

   @GetMapping(path = "/turnOnBathroomFun")
   public String turnOnBathroomFun() {
      LOGGER.info("Bathroom fan will be turned on");

      mainLogic.turnOnBathroomFan();

      return "OK";
   }

   private void writeGeneralDebugInfo(String clientIp, Esp8266Request esp8266Request) {
      String gain = esp8266Request.getGain() != null ? esp8266Request.getGain().trim() : null;
      LOGGER.debug(new Formatter().format(LOGGER_DEBUG_INFO, clientIp, esp8266Request.getDeviceName(), gain, esp8266Request.getErrors(),
            esp8266Request.getUsartOverrunErrors(), esp8266Request.getUsartIdleLineDetections(), esp8266Request.getUsartNoiseDetection(),
            esp8266Request.getUsartFramingErrors(), esp8266Request.getLastErrorTask(), esp8266Request.getUsartData(),
            esp8266Request.getBuildTimestamp()));
   }
}
