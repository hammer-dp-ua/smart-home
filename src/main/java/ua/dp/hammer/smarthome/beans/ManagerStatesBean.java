package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.CommonSate;
import ua.dp.hammer.smarthome.models.states.ProjectorState;
import ua.dp.hammer.smarthome.models.states.ShutterState;
import ua.dp.hammer.smarthome.models.states.ShutterStateRaw;
import ua.dp.hammer.smarthome.models.states.ShutterStates;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ManagerStatesBean {
   private static final Logger LOGGER = LogManager.getLogger(ManagerStatesBean.class);

   private KeepAliveStatusesBean keepAliveStatusesBean;

   private static boolean sequentialProjectorsStatesShouldBeUpdated;
   private static int sequentialProjectorsChangesAmount;

   private Queue<DeferredResult<AllManagerStates>> allStatesDeferredResults = new ConcurrentLinkedQueue<>();
   private AllManagerStates allManagerStates = AllManagerStates.getInstance();

   public AllManagerStates getAllManagerStates() {
      return allManagerStates;
   }

   public void addStateDeferredResult(DeferredResult<AllManagerStates> defResult) {
      allStatesDeferredResults.add(defResult);

      keepAliveStatusesBean.addSubscriber(KeepAliveStatusesBean.class, (deviceName) -> {
         /*if (deviceName.equals(allManagerStates.getFanState().getName())) {
            projectorState.setNotAvailable(true);
            updateDeferred();
            return;
         }*/

         for (CommonSate projectorState : allManagerStates.getProjectorsState()) {
            if (deviceName.equals(projectorState.getName())) {
               updateKeepAliveState(projectorState);
               return;
            }
         }
         for (CommonSate shutterState : allManagerStates.getShuttersState()) {
            if (deviceName.equals(shutterState.getName())) {
               updateKeepAliveState(shutterState);
               return;
            }
         }
      });
   }

   private void updateKeepAliveState(CommonSate commonState) {
      if (commonState.isNotAvailable()) {
         return;
      }

      commonState.setNotAvailable(true);
      updateDeferred();
   }

   public void changeProjectorState(ProjectorState projectorState, int expectedSequentialInvocations) {
      sequentialProjectorsChangesAmount++;
      sequentialProjectorsStatesShouldBeUpdated |= changeProjectorStateInternal(projectorState);

      if (sequentialProjectorsChangesAmount >= expectedSequentialInvocations) {
         sequentialProjectorsChangesAmount = 0;

         if (sequentialProjectorsStatesShouldBeUpdated) {
            sequentialProjectorsStatesShouldBeUpdated = false;
            updateDeferred();
         }
      }
   }

   public void changeProjectorState(ProjectorState projectorState) {
      if (changeProjectorStateInternal(projectorState)) {
         updateDeferred();
      }
   }

   private boolean changeProjectorStateInternal(ProjectorState projectorState) {
      ProjectorState existingProjectorState = allManagerStates.getProjectorsState()
            .stream()
            .filter(p -> p.getName().equals(projectorState.getName()))
            .findFirst()
            .orElse(null);
      boolean shouldBeUpdated = false;

      if (!projectorState.equals(existingProjectorState)) {
         if (existingProjectorState == null) {
            allManagerStates.getProjectorsState().add(projectorState);
         } else {
            existingProjectorState.setNewState(projectorState);
         }
         shouldBeUpdated = true;
      }
      return shouldBeUpdated;
   }

   public void changeFunState(boolean turnedOn, int minutesRemaining) {
      allManagerStates.getFanState().setTurnedOn(turnedOn);
      allManagerStates.getFanState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   public void changeAlarmsIgnoringState(boolean ignoring, int minutesRemaining) {
      allManagerStates.getAlarmsState().setIgnoring(ignoring);
      allManagerStates.getAlarmsState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   public void receiveNewShutterState(DeviceInfo deviceInfo) {
      for (ShutterStateRaw shutterStateRaw : deviceInfo.getShutterStates()) {
         String name = deviceInfo.getDeviceName();
         int no = shutterStateRaw.getShutterNo();
         ShutterStates state = ShutterStates.getState(shutterStateRaw.getShutterState());

         setShutterState(new ShutterState(name, no, state, false));
      }
   }

   public void setShutterState(ShutterState shutterState) {
      ShutterState currentState = allManagerStates.getShuttersState().stream()
            .filter(x -> x.getName().equals(shutterState.getName()) && (x.getShutterNo() == shutterState.getShutterNo()))
            .findFirst()
            .orElse(null);

      if (shutterState.equals(currentState)) {
         return;
      } else if (currentState == null) {
         allManagerStates.getShuttersState().add(shutterState);
      } else {
         currentState.setNewState(shutterState);
      }

      updateDeferred();
   }

   public boolean isProjectorTurnedOn(String projectorName) {
      return allManagerStates.getProjectorsState()
            .stream()
            .filter(p -> projectorName.equals(p.getName()))
            .findFirst()
            .map(ProjectorState::isTurnedOn)
            .orElse(false);
   }

   private void updateDeferred() {
      keepAliveStatusesBean.deleteSubscriber(ManagerStatesBean.class);

      while (!allStatesDeferredResults.isEmpty()) {
         DeferredResult<AllManagerStates> deferredResult = allStatesDeferredResults.poll();
         deferredResult.setResult(allManagerStates);
      }
   }

   @Autowired
   public void setKeepAliveStatusesBean(KeepAliveStatusesBean keepAliveStatusesBean) {
      this.keepAliveStatusesBean = keepAliveStatusesBean;
   }
}
