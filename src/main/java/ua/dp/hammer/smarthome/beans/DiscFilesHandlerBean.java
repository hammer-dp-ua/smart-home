package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Component
public class DiscFilesHandlerBean {

   private static final Logger LOGGER = LogManager.getLogger(DiscFilesHandlerBean.class);

   private static final String RELOCATION_FILE_ERROR_MSG = " file hasn't been relocated.";
   private static final String RELOCATION_FILE_SUCCEED_MSG = " file has been successfully relocated.";
   private static final String FULL_RAM_DISC_MSG = "RAM disc is full. Free space: ";
   private static final String NEW_FILES_MSG = "New files: ";
   private static final String FILES_DELIMITER = "; ";

   private final static Comparator<Path> FILES_COMPARATOR_ASC = new Comparator<Path>() {
      @Override
      public int compare(Path filePath1, Path filePath2) {
         long diff = filePath1.toFile().lastModified() - filePath2.toFile().lastModified();
         return (diff == 0) ? 0 : (diff > 0 ? 1 : -1);
      }
   };

   private final static SortedSet<Path> OLD_FILES = new TreeSet<Path>(FILES_COMPARATOR_ASC);
   private final static Set<Path> NON_RELOCATABLE_FILES = Collections.synchronizedSet(new HashSet<Path>());

   private String ramVideosDir;
   private String discVideosDir;
   private int criticalFreeSpaceMb;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      LOGGER.info("Init");

      ramVideosDir = environment.getRequiredProperty("ramVideosDir");
      discVideosDir = environment.getRequiredProperty("discVideosDir");
      criticalFreeSpaceMb = Integer.decode(environment.getRequiredProperty("criticalFreeSpaceMb"));

      getNewFiles();
      LOGGER.info("End of init");
   }

   public boolean isRamDiscFull() {
      String ramDiscDriveLetter = ramVideosDir.substring(0, 2);
      File ramDiscDrive = new File(ramDiscDriveLetter);
      int freeSpaceMb = (int) (ramDiscDrive.getFreeSpace() / 1024 / 1024);
      boolean ramDiscIsFull = freeSpaceMb < criticalFreeSpaceMb;
      if (ramDiscIsFull && LOGGER.isInfoEnabled()) {
         LOGGER.info(FULL_RAM_DISC_MSG + freeSpaceMb + "MB");
      }
      return ramDiscIsFull;
   }

   public SortedSet<Path> getNewFiles() {
      final SortedSet<Path> files = new TreeSet<Path>(FILES_COMPARATOR_ASC);

      try {
         Files.walkFileTree(FileSystems.getDefault().getPath(ramVideosDir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
               if (Files.isReadable(filePath)) {
                  files.add(filePath);
               }
               return FileVisitResult.CONTINUE;
            }
         });
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Old files size: " + OLD_FILES.size());
         for (Path filePath : files) {
            try {
               LOGGER.trace(filePath + " hash code: " + filePath.hashCode() + "; modification time: " + Files.getLastModifiedTime(filePath));
            } catch (IOException e) {
               LOGGER.error(e);
            }
         }
      }

      files.removeAll(OLD_FILES);
      OLD_FILES.addAll(files);

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Old files size: " + OLD_FILES.size());
      }

      if (files.size() > 0 && LOGGER.isInfoEnabled()) {
         StringBuilder stringBuilder = new StringBuilder(NEW_FILES_MSG);

         for (Path filePath : files) {
            stringBuilder.append(filePath.getFileName());
            stringBuilder.append(FILES_DELIMITER);
         }
         stringBuilder.delete(stringBuilder.length() - FILES_DELIMITER.length(), stringBuilder.length() - 1);
         LOGGER.info(stringBuilder);
      }
      return files;
   }

   public SortedSet<Path> getOldFiles() {
      return Collections.unmodifiableSortedSet(OLD_FILES);
   }

   public SortedSet<Path> getOldFilesCopy() {
      return new TreeSet<Path>(OLD_FILES);
   }

   public void relocateFileToDisk(Path filePath) {
      if (!Files.isReadable(filePath) || !Files.isWritable(filePath)) {
         if (NON_RELOCATABLE_FILES.contains(filePath))
         {
            return;
         }

         LOGGER.info(filePath.getFileName() + RELOCATION_FILE_ERROR_MSG + " Readable: " + Files.isReadable(filePath)
               + ". Writable: " + Files.isWritable(filePath));
         NON_RELOCATABLE_FILES.add(filePath);
         return;
      }

      NON_RELOCATABLE_FILES.remove(filePath);
      try {
         Files.move(filePath, FileSystems.getDefault().getPath(discVideosDir + "/" + filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
         OLD_FILES.remove(filePath);
         LOGGER.info(filePath.getFileName() + RELOCATION_FILE_SUCCEED_MSG);
      } catch (IOException e) {
         LOGGER.error(filePath.getFileName() + RELOCATION_FILE_ERROR_MSG, e);
      }
   }
}
