package ua.dp.hammer.smarthome.beans;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class VpsUploader implements InternetUploader {

   private static final Logger LOGGER = LogManager.getLogger(VpsUploader.class);

   private static final int BUFFER_SIZE = 10 * 1024 * 1024;
   private static final String FILE_IS_READY_TO_UPLOAD = " video file is ready to upload. Size: ";
   private static final String UPLOADING_INFO_MSG = "Uploading has been completed. %s file has been uploaded at %.1f seconds. Average speed: %.1fKB/s";

   private String serverSocket;
   private int serverSocketPort;
   private String vpsServerMultipartVideoFileUrl;
   private String vpsServerMultipartImageFilesUrl;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      serverSocket = environment.getRequiredProperty("serverSocket");
      serverSocketPort = Integer.parseInt(environment.getRequiredProperty("serverSocketPort"));
      vpsServerMultipartVideoFileUrl = environment.getRequiredProperty("vpsServerMultipartVideoFileUrl");
      vpsServerMultipartImageFilesUrl = environment.getRequiredProperty("vpsServerMultipartImageFilesUrl");

      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            sendPostRequest("http://localhost:8080/vps-server/videoUpload",
                  new File("C:/Videos/0018A2_192.168.0.200_1_20160625001228.ts"));
         }
      }, 5000);
   }

   @Async
   @Override
   public void transferVideoFile(Path filePath) {
      File fileToUpload = filePath.toFile();
      long fileLength = fileToUpload.length();

      LOGGER.info(filePath.getFileName() + FILE_IS_READY_TO_UPLOAD + (fileLength / 1024 / 1024) + "MB");

      long startTransferringFileTime = System.currentTimeMillis();
      boolean errorOccurred = transferFileWithMultipart(vpsServerMultipartVideoFileUrl, fileToUpload);

      if (!errorOccurred) {
         logTransferSpeed(startTransferringFileTime, fileLength, fileToUpload.getName());
      }
   }

   public void transferImageFiles(String videoFileName, List<Path> filesPath) {
      List<File> files = toFiles(filesPath);
      long fileLength = 0;

      for (File file : files) {
         fileLength += file.length();
      }

      LOGGER.info(files.size() + " image files are ready to upload. Total size: " + (fileLength / 1024 / 1024) + "MB");


      long startTransferringFileTime = System.currentTimeMillis();
   }

   private boolean transferFileWithMultipart(String url, File file) {
      boolean errorOccurred = false;
      CloseableHttpClient httpClient = HttpClients.createDefault();
      HttpPost httppost = new HttpPost(url);
      FileBody bin = new FileBody(file);
      HttpEntity reqEntity = MultipartEntityBuilder.create()
            .addTextBody("name", file.getName())
            .addPart("file", bin)
            .build();

      httppost.setEntity(reqEntity);

      LOGGER.info(file.getName() + FILE_IS_READY_TO_UPLOAD + (file.length() / 1024 / 1024) + "MB");

      try (CloseableHttpResponse response = httpClient.execute(httppost)) {
         StatusLine statusLine = response.getStatusLine();

         LOGGER.info("Status code: " + statusLine.getStatusCode());
      } catch (IOException e) {
         errorOccurred = true;
         LOGGER.error(e);
      } finally {
         try {
            httpClient.close();
         } catch (IOException e) {
            LOGGER.error(e);
         }
      }
      return errorOccurred;
   }

   private boolean transferFileWithSocket(Path path) {
      boolean errorOccurred = false;
      File fileToUpload = path.toFile();

      fileToUpload.setWritable(false);
      try (Socket echoSocket = new Socket(serverSocket, serverSocketPort);
           BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(echoSocket.getOutputStream(), BUFFER_SIZE);
           BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path), BUFFER_SIZE)) {

         int data;
         while ((data = bufferedInputStream.read()) != -1) {
            bufferedOutputStream.write(data);
         }
      } catch (IOException e) {
         errorOccurred = true;
         LOGGER.error(e);
      } finally {
         fileToUpload.setWritable(true);
      }
      return errorOccurred;
   }

   private void logTransferSpeed(long startTime, long fileLength, String fileName) {
      long endTime = System.currentTimeMillis();
      float elapsedTimeS = (float)(endTime - startTime) / 1000f;
      float speedKbs = (float)(fileLength) / elapsedTimeS / 1024f;

      LOGGER.info(new Formatter().format(UPLOADING_INFO_MSG, fileName, elapsedTimeS, speedKbs));
   }

   private List<File> toFiles(List<Path> filesPath) {
      if (filesPath == null) {
         return Collections.EMPTY_LIST;
      }

      List<File> files = new ArrayList<>(filesPath.size());

      for (Path filePath : filesPath) {
         files.add(filePath.toFile());
      }
      return files;
   }
}
