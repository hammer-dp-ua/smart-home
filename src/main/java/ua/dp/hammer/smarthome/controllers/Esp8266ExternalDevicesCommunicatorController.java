package ua.dp.hammer.smarthome.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.beans.CameraBean;
import ua.dp.hammer.smarthome.beans.ImmobilizerBean;
import ua.dp.hammer.smarthome.beans.MainLogic;
import ua.dp.hammer.smarthome.models.Esp8266Data;
import ua.dp.hammer.smarthome.models.ProjectorResponse;
import ua.dp.hammer.smarthome.models.ServerStatus;
import ua.dp.hammer.smarthome.models.StatusCodes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping(path = "/server/esp8266")
public class Esp8266ExternalDevicesCommunicatorController {
   private static final Logger LOGGER = LogManager.getLogger(Esp8266ExternalDevicesCommunicatorController.class);

   private static final String LOGGER_DEBUG_INFO = "Gain of %1$s: %2$s" +
         "\r\nErrors: %3$d" +
         "\r\nOverrun Errors: %4$d" +
         "\r\nIdle Line Detections: %5$d" +
         "\r\nNoise Detection: %6$d" +
         "\r\nFraming Errors: %7$d" +
         "\r\nLast Error Task: %8$d" +
         "\r\nUSART data: %9$s";

   private Queue<DeferredResult<ServerStatus>> deferredResults = new ConcurrentLinkedQueue<>();

   @Autowired
   private CameraBean cameraBean;

   @Autowired
   private ImmobilizerBean immobilizerBean;

   @Autowired
   private MainLogic mainLogic;

   @PostMapping(path = "/statusInfo", consumes="application/json")
   public ServerStatus receiveStatusInfo(@RequestBody Esp8266Data esp8266Data, @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      ServerStatus serverStatus = new ServerStatus(StatusCodes.OK);

      if (LOGGER.isDebugEnabled()) {
         String gain = esp8266Data.getGain() != null ? esp8266Data.getGain().trim() : null;
         LOGGER.debug(new Formatter().format(LOGGER_DEBUG_INFO, clientIp, gain, esp8266Data.getErrors(),
               esp8266Data.getUsartOverrunErrors(), esp8266Data.getUsartIdleLineDetections(), esp8266Data.getUsartNoiseDetection(),
               esp8266Data.getUsartFramingErrors(), esp8266Data.getLastErrorTask(), esp8266Data.getUsartData()));
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
            //cameraBean.startVideoRecording();
         } else {
            LOGGER.info("Video recording wasn't started because immobilizer was activated " + durationBetweenImmobilizerAndAlarm +
            " seconds ago");
         }
      } else {
         //cameraBean.startVideoRecording();
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
      return new ServerStatus(StatusCodes.OK);
   }

   @GetMapping(path = "/immobilizerDeactivated")
   public ServerStatus receiveImmobilizerDeactivation(@RequestHeader("X-FORWARDED-FOR") String clientIp) {
      LOGGER.info("Immobilizer deactivated: " + clientIp);

      immobilizerBean.setDeactivationDateTime(LocalDateTime.now());
      return new ServerStatus(StatusCodes.OK);
   }

   @PostMapping(path = "/projectorDeferred", consumes="application/json")
   public DeferredResult<ProjectorResponse> sendProjectorDeferredResult(@RequestBody Esp8266Data esp8266Data,
                                                                        @RequestHeader("X-FORWARDED-FOR") String clientIp) {
      if (LOGGER.isDebugEnabled()) {
         String gain = esp8266Data.getGain() != null ? esp8266Data.getGain().trim() : null;
         LOGGER.debug(new Formatter().format(LOGGER_DEBUG_INFO, clientIp, gain, esp8266Data.getErrors(),
               esp8266Data.getUsartOverrunErrors(), esp8266Data.getUsartIdleLineDetections(), esp8266Data.getUsartNoiseDetection(),
               esp8266Data.getUsartFramingErrors(), esp8266Data.getLastErrorTask(), esp8266Data.getUsartData()));
      }

      DeferredResult<ProjectorResponse> projectorDeferredResult = new DeferredResult<>();
      mainLogic.addProjectorsDeferredResult(projectorDeferredResult, clientIp);
      return projectorDeferredResult;
   }

   /**
    *
    * @param serverStatus
    */
   public void setDeferredResult(ServerStatus serverStatus) {
      if (!deferredResults.isEmpty()) {
         DeferredResult<ServerStatus> deferredResult = deferredResults.peek();
         deferredResult.setResult(serverStatus);
      }
   }
}
