package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.SortedSet;

@Component
public class EntryPointBean {

   private static final Logger LOGGER = LogManager.getLogger(EntryPointBean.class);

   @Autowired
   private VpsUploader vpsUploader;

   @Autowired
   private DiscFilesHandlerBean discFilesHandlerBean;

   @Autowired
   private ApplicationContext appContext;

   /**
    * Period will be measured from the completion time of each preceding invocation
    */
   @Scheduled(fixedDelay=10000)
   public void checkFilesAndTransfer() {
      SortedSet<Path> newFiles = discFilesHandlerBean.getNewVideoFiles();
      Iterator<Path> newFilesIterator = newFiles.iterator();
      /*while (newFilesIterator.hasNext()) {
         Path newFile = newFilesIterator.next();
         vpsUploader.transferVideoFile(newFile);
      }*/
      discFilesHandlerBean.createImageFiles(newFiles);

      Iterator<Path> oldFilesIterator = discFilesHandlerBean.getOldFilesCopy().iterator();
      while (discFilesHandlerBean.isRamDiscFull() && oldFilesIterator.hasNext()) {
         Path oldFilePath = oldFilesIterator.next();
         discFilesHandlerBean.relocateFileToDisk(oldFilePath);
      }
   }
}
