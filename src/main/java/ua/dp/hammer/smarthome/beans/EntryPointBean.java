package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.interfaces.ImageFilesUploader;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

@Component
public class EntryPointBean {

   private static final Logger LOGGER = LogManager.getLogger(EntryPointBean.class);

   @Autowired
   private VpsUploader vpsUploader;

   @Autowired
   private DiscFilesHandlerBean discFilesHandlerBean;

   @Autowired
   private ApplicationContext appContext;

   @Autowired
   private CameraBean cameraBean;

   @Autowired
   private ImmobilizerBean immobilizerBean;

   /**
    * Period will be measured from the completion time of each preceding invocation
    */
   @Scheduled(fixedDelay=10000)
   public void checkFilesAndTransfer() {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Scheduled method is executed from thread " + Thread.currentThread().getId());
      }

      SortedSet<Path> newFiles = discFilesHandlerBean.getNewVideoFiles();
      Iterator<Path> newFilesIterator = newFiles.iterator();

      while (newFilesIterator.hasNext()) {
         Path newFile = newFilesIterator.next();
         vpsUploader.transferVideoFile(newFile);
      }

      discFilesHandlerBean.createImageFiles(newFiles, new ImageFilesUploader() {
         @Override
         public void upload(String videoFileName, SortedSet<Path> imageFilesPath) {
            vpsUploader.transferImageFiles(videoFileName, imageFilesPath);
         }
      });

      Iterator<Path> oldFilesIterator = discFilesHandlerBean.getOldFilesCopy().iterator();

      while (discFilesHandlerBean.isRamDiscFull() && oldFilesIterator.hasNext()) {
         Path oldFilePath = oldFilesIterator.next();
         discFilesHandlerBean.relocateFileToDisk(oldFilePath);
      }

      LocalDateTime immobilizerActivatedDateTime = immobilizerBean.getActivationDateTime();
      if (cameraBean.isVideoRecordingInProcess() && immobilizerActivatedDateTime != null) {
         long immobilizerActivatedDuration = Duration.between(LocalDateTime.now(), immobilizerActivatedDateTime).abs().getSeconds();

         if (immobilizerActivatedDuration <= 60) {
            LOGGER.info("Video recording is stopping because immobilizer was activated " + immobilizerActivatedDuration +
            " seconds ago");

            cameraBean.scheduleStopVideoRecording(5, TimeUnit.SECONDS);
         }
      }
   }
}
