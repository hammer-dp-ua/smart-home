package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ua.dp.hammer.smarthome.interfaces.ImageFilesUploader;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

   public final static Comparator<Path> PATH_FILES_COMPARATOR_ASC = (filePath1, filePath2) -> {
      long diff = filePath1.toFile().lastModified() - filePath2.toFile().lastModified();
      return (diff == 0) ? 0 : (diff > 0 ? 1 : -1);
   };

   public final static Comparator<File> FILES_COMPARATOR_ASC = (file1, file2) -> {
      long diff = file1.lastModified() - file2.lastModified();
      return (diff == 0) ? 0 : (diff > 0 ? 1 : -1);
   };

   private final static SortedSet<Path> OLD_FILES = new TreeSet<>(PATH_FILES_COMPARATOR_ASC);
   private final static Set<Path> NON_RELOCATABLE_FILES = Collections.synchronizedSet(new HashSet<Path>());

   private String ramVideosDir;
   private String discVideosDir;
   private int criticalFreeSpaceMb;
   private String videoFileExtension;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      ramVideosDir = environment.getRequiredProperty("ramVideosDir");
      discVideosDir = environment.getRequiredProperty("discVideosDir");
      criticalFreeSpaceMb = Integer.decode(environment.getRequiredProperty("criticalFreeSpaceMb"));
      videoFileExtension = environment.getRequiredProperty("videoFileExtension");

      getNewVideoFiles();
   }

   public boolean isRamDiscFull() {
      File ramDirFile = new File(ramVideosDir);
      int freeSpaceMb = (int) (ramDirFile.getFreeSpace() / 1024 / 1024);
      boolean ramDiscIsFull = freeSpaceMb < criticalFreeSpaceMb;

      if (ramDiscIsFull) {
         LOGGER.info(FULL_RAM_DISC_MSG + freeSpaceMb + "MB");
      }
      return ramDiscIsFull;
   }

   public SortedSet<Path> getNewVideoFiles() {
      final SortedSet<Path> newFiles = new TreeSet<>(PATH_FILES_COMPARATOR_ASC);
      Path videosDirectoryPath = FileSystems.getDefault().getPath(ramVideosDir);

      try {
         Files.walkFileTree(videosDirectoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
               if (Files.isReadable(filePath) && filePath.toString().endsWith(videoFileExtension)) {
                  newFiles.add(filePath);
               }
               return FileVisitResult.CONTINUE;
            }
         });
      } catch (NoSuchFileException e) {
         LOGGER.error(e);

         try {
            Files.createDirectory(videosDirectoryPath);
            LOGGER.info(ramVideosDir + " has been created");
         } catch (IOException directoryCreationException) {
            LOGGER.error(directoryCreationException);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Old files count: " + OLD_FILES.size());

         for (Path filePath : newFiles) {
            try {
               LOGGER.debug(filePath + " hash code: " + filePath.hashCode() + "; modification time: " +
                     Files.getLastModifiedTime(filePath));
            } catch (IOException e) {
               LOGGER.error(e);
            }
         }
      }

      newFiles.removeAll(OLD_FILES);
      OLD_FILES.addAll(newFiles);

      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("Old files count: " + OLD_FILES.size());
      }

      if (newFiles.size() > 0) {
         StringBuilder stringBuilder = new StringBuilder(NEW_FILES_MSG);

         for (Path filePath : newFiles) {
            stringBuilder.append(filePath.getFileName());
            stringBuilder.append(FILES_DELIMITER);
         }
         stringBuilder.delete(stringBuilder.length() - FILES_DELIMITER.length(), stringBuilder.length() - 1);
         LOGGER.info(stringBuilder);
      }
      return newFiles;
   }

   public SortedSet<Path> getOldFiles() {
      return Collections.unmodifiableSortedSet(OLD_FILES);
   }

   public SortedSet<Path> getOldFilesCopy() {
      return new TreeSet<>(OLD_FILES);
   }

   public void relocateFileToDisk(Path filePath) {
      if (!filePath.toFile().exists()) {
         LOGGER.info(filePath.getFileName() + " doesn't exist anymore");

         NON_RELOCATABLE_FILES.remove(filePath);
         OLD_FILES.remove(filePath);
      }

      if (!Files.isReadable(filePath) || !Files.isWritable(filePath)) {
         if (NON_RELOCATABLE_FILES.contains(filePath)) {
            return;
         }

         LOGGER.info(filePath.getFileName() + RELOCATION_FILE_ERROR_MSG + " Readable: " + Files.isReadable(filePath)
               + ". Writable: " + Files.isWritable(filePath));

         NON_RELOCATABLE_FILES.add(filePath);
         return;
      }

      NON_RELOCATABLE_FILES.remove(filePath);

      try {
         Files.move(filePath, FileSystems.getDefault().getPath(discVideosDir + File.separator + filePath.getFileName()),
               StandardCopyOption.REPLACE_EXISTING);
         OLD_FILES.remove(filePath);

         LOGGER.info(filePath.getFileName() + RELOCATION_FILE_SUCCEED_MSG);
      } catch (IOException e) {
         LOGGER.error(filePath.getFileName() + RELOCATION_FILE_ERROR_MSG, e);
      }
   }

   @Async
   public void createImageFiles(Set<Path> videoFiles, ImageFilesUploader imageFilesUploader) {
      for (Path videoFilePath : videoFiles) {
         File videoFile = videoFilePath.toFile();
         int videoFileLengthMb = (int) (videoFile.length() / 1024 / 1024);

         if (videoFileLengthMb < 10) {
            continue;
         }

         String newDirectoryName = videoFilePath.getFileName().toString().replace(videoFileExtension, "");
         Path newDirectoryPath = FileSystems.getDefault().getPath(ramVideosDir, newDirectoryName);

         try {
            Files.createDirectory(newDirectoryPath);

            long startTime = System.currentTimeMillis();
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg.exe", "-i", videoFilePath.toString(), "-r", "0.2",
                  newDirectoryPath.toString() + File.separator + "%3d.jpeg");
            processBuilder.redirectErrorStream(true);

            //readInputStreamInSeparateThread(process);

            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();

            while (inputStream.read() != -1) {
            }

            int execTimeSec = (int) ((System.currentTimeMillis() - startTime) / 1000);
            LOGGER.info(videoFilePath.getFileName().toString() + " video file converted in " + execTimeSec + " seconds");

            imageFilesUploader.upload(videoFilePath.getFileName().toString(), getFiles(newDirectoryPath));
         } catch (IOException e) {
            LOGGER.error(newDirectoryName + " directory could not be created", e);
         }
      }
   }

   private void readInputStreamInSeparateThread(final Process process) {
      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            InputStream inputStream = process.getInputStream();

            try {
               while (inputStream.read() != -1) {
               }
               inputStream.close();

               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("Input stream reading stopped");
               }
            } catch (IOException e) {
               LOGGER.error(e);
            }
         }
      }, 0);
   }

   private SortedSet<Path> getFiles(Path directoryPath) {
      final SortedSet<Path> newFiles = new TreeSet<Path>(PATH_FILES_COMPARATOR_ASC);

      try {
         Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
               newFiles.add(filePath);
               return FileVisitResult.CONTINUE;
            }
         });
      } catch (IOException e) {
         LOGGER.error(e);
      }
      return newFiles;
   }
}
