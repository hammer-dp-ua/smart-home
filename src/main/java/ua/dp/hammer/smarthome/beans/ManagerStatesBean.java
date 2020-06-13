package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import ua.dp.hammer.smarthome.models.DeviceInfo;
import ua.dp.hammer.smarthome.models.states.AllManagerStates;
import ua.dp.hammer.smarthome.models.states.CommonSate;
import ua.dp.hammer.smarthome.models.states.FanState;
import ua.dp.hammer.smarthome.models.states.ProjectorState;
import ua.dp.hammer.smarthome.models.states.ShutterState;
import ua.dp.hammer.smarthome.models.states.ShutterStateRaw;
import ua.dp.hammer.smarthome.models.states.ShutterStates;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class ManagerStatesBean {
   private static final Logger LOGGER = LogManager.getLogger(ManagerStatesBean.class);

   private KeepAliveStatusesBean keepAliveStatusesBean;

   private static boolean sequentialProjectorsStatesShouldBeUpdated;
   private static int sequentialProjectorsChangesAmount;

   private Queue<DeferredResult<AllManagerStates>> allStatesDeferredResults;
   private AllManagerStates allManagerStates;

   @PostConstruct
   public void init() {
      allStatesDeferredResults = new ConcurrentLinkedQueue<>();
      allManagerStates = new AllManagerStates();
   }

   public AllManagerStates getAllManagerStates() {
      return allManagerStates;
   }

   public void addStateDeferredResult(DeferredResult<AllManagerStates> defResult) {
      allStatesDeferredResults.add(defResult);

      keepAliveStatusesBean.addSubscriber(KeepAliveStatusesBean.class, (notAvailableDevices) -> {
         List<CommonSate> notAvailableSates = new LinkedList<>();

         if (notAvailableDevices.contains(allManagerStates.getFanState().getDeviceName())) {
            notAvailableSates.add(allManagerStates.getFanState());
         }

         List<CommonSate> projectorStates = allManagerStates.getProjectorsState()
               .stream()
               .filter(projectorState -> notAvailableDevices.contains(projectorState.getDeviceName()))
               .collect(Collectors.toList());
         notAvailableSates.addAll(projectorStates);

         // There are 2 for kitchen shutters
         List<CommonSate> shutterSates = allManagerStates.getShuttersState()
               .stream()
               .filter(shutterState -> notAvailableDevices.contains(shutterState.getDeviceName()))
               .collect(Collectors.toList());
         notAvailableSates.addAll(shutterSates);

         updateKeepAliveStates(notAvailableSates);
      });
   }

   private void updateKeepAliveStates(List<CommonSate> notAvailableSates) {
      boolean isAnyConsideredAvailable = notAvailableSates
            .stream()
            .anyMatch(deviceSate -> !deviceSate.isNotAvailable());

      if (isAnyConsideredAvailable) {
         notAvailableSates.forEach(deviceSate -> deviceSate.setNotAvailable(true));
         updateDeferred();
      }
   }

   public void resetExpectedSequentialProjectorCounter() {
      sequentialProjectorsChangesAmount = 0;
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
            .filter(p -> p.getDeviceName().equals(projectorState.getDeviceName()))
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

   public void changeFunState(FanState newState) {
      boolean stateChanged = false;
      FanState currentState = allManagerStates.getFanState();

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Old " + FanState.class.getSimpleName() + ": " + currentState);
      }

      currentState.setDeviceName(newState.getDeviceName());

      if (newState.getTurningOnStateProlonged() != null) {
         currentState.setTurningOnStateProlonged(newState.getTurningOnStateProlonged());
      }

      if (newState.getMinutesRemaining() != null &&
            !newState.getMinutesRemaining().equals(currentState.getMinutesRemaining())) {
         stateChanged = true;
         currentState.setMinutesRemaining(newState.getMinutesRemaining());
      }

      if (newState.isTurnedOn() != null &&
            !newState.isTurnedOn().equals(currentState.isTurnedOn())) {
         stateChanged = true;
         currentState.setTurnedOn(newState.isTurnedOn());
      }

      if (newState.isNotAvailable() != null &&
            !newState.isNotAvailable().equals(currentState.isNotAvailable())) {
         stateChanged = true;
         currentState.setNotAvailable(newState.isNotAvailable());
      }

      if (stateChanged) {
         updateDeferred();
      }

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("New " + FanState.class.getSimpleName() + ": " + currentState +
               "\nState changed: " + stateChanged);
      }
   }

   public void changeAlarmsIgnoringState(boolean ignoring, int minutesRemaining) {
      allManagerStates.getAlarmsState().setIgnoring(ignoring);
      allManagerStates.getAlarmsState().setMinutesRemaining(minutesRemaining);

      updateDeferred();
   }

   public void receiveNewShutterState(DeviceInfo deviceInfo) {
      boolean deferredShouldBeUpdated = false;

      for (ShutterStateRaw shutterStateRaw : deviceInfo.getShutterStates()) {
         String name = deviceInfo.getDeviceName();
         int no = shutterStateRaw.getShutterNo();
         ShutterStates state = ShutterStates.getState(shutterStateRaw.getShutterState());

         deferredShouldBeUpdated |= setShutterState(new ShutterState(name, no, state, false));
      }

      if (deferredShouldBeUpdated) {
         updateDeferred();
      }
   }

   public boolean setShutterState(ShutterState shutterState) {
      ShutterState currentState = allManagerStates.getShuttersState().stream()
            .filter(x -> x.getDeviceName().equals(shutterState.getDeviceName()) && (x.getShutterNo() == shutterState.getShutterNo()))
            .findFirst()
            .orElse(null);

      if (shutterState.equals(currentState)) {
         return false;
      } else if (currentState == null) {
         allManagerStates.getShuttersState().add(shutterState);
      } else {
         currentState.setNewState(shutterState);
      }
      return true;
   }

   public boolean isProjectorTurnedOn(String projectorName) {
      return allManagerStates.getProjectorsState()
            .stream()
            .filter(p -> projectorName.equals(p.getDeviceName()))
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
