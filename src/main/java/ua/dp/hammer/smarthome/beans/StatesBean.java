package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.states.AllStates;
import ua.dp.hammer.smarthome.models.states.ShutterState;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class StatesBean {
   private static final Logger LOGGER = LogManager.getLogger(StatesBean.class);

   private Queue<DeferredResult<AllStates>> allStatesDeferredResults = new ConcurrentLinkedQueue<>();
   private AllStates allStates = AllStates.getInstance();

   public AllStates getAllStates() {
      return allStates;
   }

   public void addStateDeferredResult(DeferredResult<AllStates> defResult) {
      allStatesDeferredResults.add(defResult);
   }

   public void changeProjectorState(boolean turnedOn) {
      allStates.getProjectorState().setTurnedOn(turnedOn);

      updateDeferred();
   }

   public void changeFunState(boolean turnedOn, int minutesRemaining) {
      allStates.getFanState().setTurnedOn(turnedOn);
      allStates.getFanState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   public void changeAlarmsIgnoringState(boolean ignoring, int minutesRemaining) {
      allStates.getAlarmsState().setIgnoring(ignoring);
      allStates.getAlarmsState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   public void changeShutterState(ShutterState shutterState) {
      ShutterState currentState = allStates.getShuttersState().stream()
            .filter(x -> x.equals(shutterState))
            .findFirst()
            .orElse(null);

      if (currentState != null && currentState.isOpened() == shutterState.isOpened()) {
         return;
      } else if (currentState == null) {
         allStates.getShuttersState().add(shutterState);
      } else {
         currentState.setOpened(shutterState.isOpened());
      }

      updateDeferred();
   }

   private void updateDeferred() {
      while (!allStatesDeferredResults.isEmpty()) {
         DeferredResult<AllStates> deferredResult = allStatesDeferredResults.poll();
         deferredResult.setResult(allStates);
      }
   }
}
