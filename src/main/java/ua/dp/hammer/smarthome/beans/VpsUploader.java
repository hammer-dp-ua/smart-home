package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Formatter;

@Component
public class VpsUploader implements InternetUploader {

   private static final Logger LOGGER = LogManager.getLogger(VpsUploader.class);

   private static final int BUFFER_SIZE = 10 * 1024 * 1024;
   private static final String FILE_IS_READY_TO_UPLOAD = " file if ready to upload. Size: ";
   private static final String UPLOADING_INFO_MSG = "Uploading has been completed. %s file has been uploaded at %.1f seconds. Average speed: %.1fKB/s";

   private String serverSocket;
   private int serverSocketPort;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      serverSocket = environment.getRequiredProperty("serverSocket");
      serverSocketPort = Integer.parseInt(environment.getRequiredProperty("serverSocketPort"));
   }

   @Async
   @Override
   public void transferFile(Path path) {
      boolean errorOccurred = false;
      File fileToUpload = path.toFile();
      long fileLength = fileToUpload.length();
      long startTransferringFileTime = System.currentTimeMillis();

      LOGGER.info(path.getFileName() + FILE_IS_READY_TO_UPLOAD + (fileLength / 1024) + "KB");

      fileToUpload.setWritable(false);
      try (Socket echoSocket = new Socket(serverSocket, serverSocketPort);
           BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(echoSocket.getOutputStream(), BUFFER_SIZE);
           BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path), BUFFER_SIZE)) {

         int data;
         while ((data = bufferedInputStream.read()) != -1)
         {
            bufferedOutputStream.write(data);
         }
      } catch (UnknownHostException e) {
         errorOccurred = true;
         LOGGER.error(e);
      } catch (IOException e) {
         errorOccurred = true;
         LOGGER.error(e);
      }
      finally {
         fileToUpload.setWritable(true);
      }

      if (!errorOccurred) {
         logTransferSpeed(startTransferringFileTime, fileLength, fileToUpload.getName());
      }
   }

   private void logTransferSpeed(long startTime, long fileLength, String fileName) {
      long endTime = System.currentTimeMillis();
      float elapsedTimeS = (float)(endTime - startTime) / 1000f;
      float speedKbs = (float)(fileLength) / elapsedTimeS / 1024f;

      LOGGER.info(new Formatter().format(UPLOADING_INFO_MSG, fileName, elapsedTimeS, speedKbs));
   }
}
