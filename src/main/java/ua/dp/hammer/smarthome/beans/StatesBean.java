package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.states.AllStates;

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

   public void changeFunState(boolean turnedOn) {
      allStates.getFanState().setTurnedOn(turnedOn);

      updateDeferred();
   }

   public void changeAlarmsIgnoringState(boolean ignoring, int minutesRemaining) {
      allStates.getAlarmsState().setIgnoring(ignoring);
      allStates.getAlarmsState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   private void updateDeferred() {
      while (!allStatesDeferredResults.isEmpty()) {
         DeferredResult<AllStates> deferredResult = allStatesDeferredResults.poll();
         deferredResult.setResult(allStates);
      }
   }
}
