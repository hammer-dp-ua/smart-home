package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.ProjectorResponse;
import ua.dp.hammer.smarthome.models.StatusCodes;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

@Component
public class MainLogic {
   private static final Logger LOGGER = LogManager.getLogger(MainLogic.class);

   private boolean turnProjectorOn;
   private int projectorTurnOffTimeoutSec;
   private int deferredResponseTimeoutSec;
   private Queue<DeferredResult<ProjectorResponse>> projectorsDeferredResults = new ConcurrentLinkedQueue<>();
   private ScheduledFuture<?> scheduledFutureProjectorTurningOff;
   private LocalDateTime lastSentResponsesTime;
   private LocalDateTime thresholdHumidityStartTime;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      projectorTurnOffTimeoutSec = Integer.parseInt(environment.getRequiredProperty("projectorTurnOffTimeoutSec"));
      deferredResponseTimeoutSec = Integer.parseInt(environment.getRequiredProperty("deferredResponseTimeoutSec"));
   }

   public void receiveAlarm() {
      turnProjectorsOn();
   }

   public void addProjectorsDeferredResult(DeferredResult<ProjectorResponse> projectorDeferredResult, String clientIp,
                                           boolean serverIsAvailable) {
      if (!serverIsAvailable) {
         ProjectorResponse projectorResponse = createProjectorResponse();
         projectorDeferredResult.setResult(projectorResponse);

         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Server availability hasn't been detected yet");
         }
         return;
      }

      projectorsDeferredResults.add(projectorDeferredResult);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred result from " + clientIp + " has been added. Deferred results size: " + projectorsDeferredResults.size());
      }
   }

   @Scheduled(fixedRate = 5000)
   public void setProjectorsDeferredResult() {
      if (lastSentResponsesTime == null || LocalDateTime.now().minusSeconds(deferredResponseTimeoutSec).isAfter(lastSentResponsesTime)) {
         sendKeepHeartResponse();
      }
   }

   public void turnProjectorsOn() {
      LocalDateTime localDateTime = LocalDateTime.now();

      if (localDateTime.getHour() >= 16 || localDateTime.getHour() <= 7) {
         if (scheduledFutureProjectorTurningOff != null && !scheduledFutureProjectorTurningOff.isDone()) {
            scheduledFutureProjectorTurningOff.cancel(false);

            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Projector turning off scheduled task has been canceled.");
            }
         }

         scheduledFutureProjectorTurningOff = new ConcurrentTaskScheduler().schedule(new Runnable() {
            @Override
            public void run() {
               switchProjectors(ProjectorState.TURN_OFF);
            }
         }, new Date(System.currentTimeMillis() + projectorTurnOffTimeoutSec * 1000));

         switchProjectors(ProjectorState.TURN_ON);
      }
   }

   public boolean getBathroomFanState(float humidity, float temperature) {
      if (humidity >= 90.0f) {
         thresholdHumidityStartTime = LocalDateTime.now();
      }

      LocalDateTime currentTime = LocalDateTime.now();
      return thresholdHumidityStartTime != null && currentTime.isBefore(thresholdHumidityStartTime.plusMinutes(10));
   }

   public void turnOnBathroomFan() {
      thresholdHumidityStartTime = LocalDateTime.now();
   }

   private void sendKeepHeartResponse() {
      switchProjectors(null);
   }

   private void switchProjectors(ProjectorState newProjectorState) {
      if (newProjectorState != null) {
         turnProjectorOn = newProjectorState == ProjectorState.TURN_ON;
      }

      if (turnProjectorOn) {
         LOGGER.info("Projectors are turning on");
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred results are ready to be returned. Size: " + projectorsDeferredResults.size() +
               ". New projectors state: " + newProjectorState + ". State to set: " + turnProjectorOn);
      }

      while (!projectorsDeferredResults.isEmpty()) {
         DeferredResult<ProjectorResponse> projectorDeferredResult = projectorsDeferredResults.poll();

         if (projectorDeferredResult == null) {
            return;
         }

         ProjectorResponse projectorResponse = createProjectorResponse();

         projectorDeferredResult.setResult(projectorResponse);
      }
      lastSentResponsesTime = LocalDateTime.now();
   }

   private ProjectorResponse createProjectorResponse() {
      ProjectorResponse projectorResponse = new ProjectorResponse(StatusCodes.OK);

      projectorResponse.setTurnOn(turnProjectorOn);
      if (LOGGER.isDebugEnabled()) {
         projectorResponse.setIncludeDebugInfo(true);
      }
      return projectorResponse;
   }

   private enum ProjectorState {
      TURN_ON, TURN_OFF
   }
}
