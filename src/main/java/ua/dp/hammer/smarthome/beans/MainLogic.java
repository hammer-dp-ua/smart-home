package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.ProjectorResponse;
import ua.dp.hammer.smarthome.models.StatusCodes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

@Component
public class MainLogic {
   private static final Logger LOGGER = LogManager.getLogger(MainLogic.class);

   private boolean turnProjectorOn;
   private Queue<DeferredResult<ProjectorResponse>> projectorsDeferredResults = new ConcurrentLinkedQueue<>();
   private ScheduledFuture<?> scheduledFutureProjectorTurningOff;
   private LocalDateTime lastSentResponsesTime;

   public void receiveAlarm() {
      if (scheduledFutureProjectorTurningOff != null && !scheduledFutureProjectorTurningOff.isDone()) {
         scheduledFutureProjectorTurningOff.cancel(false);
      }

      scheduledFutureProjectorTurningOff = new ConcurrentTaskScheduler().schedule(new Runnable() {
         @Override
         public void run() {
            switchProjectors(ProjectorState.TURN_OFF);
         }
      }, new Date(System.currentTimeMillis() + 60000));

      switchProjectors(ProjectorState.TURN_ON);
   }

   public void addProjectorsDeferredResult(DeferredResult<ProjectorResponse> projectorDeferredResult, String clientIp) {
      projectorsDeferredResults.add(projectorDeferredResult);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Deferred result from " + clientIp + " has been added. Deferred results size: " + projectorsDeferredResults.size());
      }
   }

   @Scheduled(fixedRate = 10000)
   public void setProjectorsDeferredResult() {
      if (lastSentResponsesTime == null || LocalDateTime.now().minusMinutes(5).isAfter(lastSentResponsesTime)) {
         sendKeepHeartResponse();
      }
   }

   private void sendKeepHeartResponse() {
      switchProjectors(null);
   }

   private void switchProjectors(ProjectorState newProjectorState) {
      if (newProjectorState != null) {
         turnProjectorOn = newProjectorState == ProjectorState.TURN_ON;
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

         ProjectorResponse projectorResponse = new ProjectorResponse(StatusCodes.OK);

         projectorResponse.setTurnOn(turnProjectorOn);
         if (LOGGER.isDebugEnabled()) {
            projectorResponse.setIncludeDebugInfo(true);
         }

         projectorDeferredResult.setResult(projectorResponse);
      }
      lastSentResponsesTime = LocalDateTime.now();
   }

   private enum ProjectorState {
      TURN_ON, TURN_OFF
   }
}
