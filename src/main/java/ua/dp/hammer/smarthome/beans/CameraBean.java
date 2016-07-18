package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CameraBean {
   private static final Logger LOGGER = LogManager.getLogger(CameraBean.class);

   private static final String LOGIN_URL = "http://%s/goform/WEB_UsrLoginProc";
   private static final String LOGIN_REFERRER_URL = "http://%s/asppage/common/login.asp";
   private static final String LOGIN_POST_BODY = "UserName=%1$s&Password=%2$s&B1=Login&LanguageParam=1&languageType=";
   private static final String SET_VALUE_BY_AJAX_URL = "http://%s/goform/WEB_SetValueByAjax";
   private static final String ALARM_IO_PARAM_URL = "http://%s/goform/WEB_setAlarmIOParam";
   private static final String REFERRER_IO_PARAM_URL = "http://%1$s/asppage/common/alarmIOparam.asp";
   private static final String START_STOP_RECORDING = "AlarmInId=1" +
         "&AlarmInName=1" +
         "&AlarmInValidLevel=%1$s" +
         "&AlarmOutId=1" +
         "&AlarmOutName=" +
         "&AlarmOutValidSignal=2" +
         "&AlarmMode=1" +
         "&Frequency=0.01" +
         "&AlarmTime=12000" +
         "&B1=OK" +
         "&ID=%2$s" +
         "&AlarmOutDeviceType=1" +
         "&AlarmOutFlag=0" +
         "&languageType=1";
   private static final String KEEP_HEART_URL = "http://%s/asppage/common/keepHeart.asp";
   private static final String KEEP_HEART_REFERRER = "http://%s/asppage/common/top.asp";
   private static final String KEEP_HEART_POST_BODY = "ID=%1$s&sand=%2$s";
   private static final String KEEP_HEART_COOKIES = "coobjMenuTree=Alarm; csobjMenuTree=DeviceInfo";
   private static final int[] KEEP_HEART_SUCCESSFULLY_RESPONSE = {239, 187, 191, 48};
   private static final String MANUAL_RECORDING_URL = "http://%s/asppage/common/Ajax_getAlarm_Request.asp";
   private static final String MANUAL_RECORDING_REFERRER = "http://%s/asppage/common/alarmIOparam.asp?alarminid=1&alarmoutid=1&ret=0";
   private static final String MANUAL_RECORDING_POST_BODY = "AlarmOutFlag=%1$s&AlarmOutId=1&AlarmOutDeviceType=1&ID=%2$s";
   private static final String CAMERA_ID = "0018A2";
   private static final String POST_METHOD = "POST";
   private static final String HOST_HEADER = "Host";
   private static final String REFERRER_HEADER = "Referer";
   private static final String COOKIE_HEADER = "Cookie";
   private static final Pattern ID_FROM_RESPONSE_PATTERN = Pattern.compile("ID=(\\d+)");
   private static final String CONNECTION_TIMEOUT = "Connection timed out";

   private static final ScheduledThreadPoolExecutor STOP_VIDEO_RECORDING_EXECUTOR = new ScheduledThreadPoolExecutor(1);
   private static final AtomicReference<String> CREDENTIAL_ID = new AtomicReference<>();

   private String cameraIp;
   private String cameraLogin;
   private String cameraPassword;
   private long cameraRecordingTimeSec;

   static {
      STOP_VIDEO_RECORDING_EXECUTOR.setRemoveOnCancelPolicy(true);
   }

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      cameraIp = environment.getRequiredProperty("cameraIp");
      cameraLogin = environment.getRequiredProperty("cameraLogin");
      cameraPassword = environment.getRequiredProperty("cameraPassword");
      cameraRecordingTimeSec = Long.parseLong(environment.getRequiredProperty("cameraRecordingTimeSec"));
   }

   public String login() {
      HttpURLConnection httpURLConnection = createPostRequest(String.format(LOGIN_URL, cameraIp),
            String.format(LOGIN_REFERRER_URL, cameraIp), String.format(LOGIN_POST_BODY, cameraLogin, cameraPassword),
            null);

      if (httpURLConnection == null) {
         return null;
      }

      int responseCode = 0;
      try {
         responseCode = httpURLConnection.getResponseCode();
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
         return null;
      }

      String response = readResponseBody(httpURLConnection);
      Matcher matcher = ID_FROM_RESPONSE_PATTERN.matcher(response);
      String id = null;

      if (matcher.find(1)) {
         id = matcher.group(1);
      }

      if (id != null && LOGGER.isDebugEnabled()) {
         LOGGER.debug("Logged in successfully. ID: " + id);
      }
      return id;
   }

   @Async
   public void startVideoRecording() {
      if (startStopVideoRecording(true)) {
         scheduleStopVideoRecording(cameraRecordingTimeSec, TimeUnit.SECONDS);
      } else {
         try {
            Thread.sleep(10000);
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Retrying to start video recording");
            }
         } catch (InterruptedException e) {
            LOGGER.error(e);
         }
         startVideoRecording();
      }
   }

   public void scheduleStopVideoRecording(long time, TimeUnit timeUnit) {
      BlockingQueue<Runnable> queue = STOP_VIDEO_RECORDING_EXECUTOR.getQueue();
      if (queue.size() > 0) {
         queue.remove(queue.element());
      }

      STOP_VIDEO_RECORDING_EXECUTOR.schedule(new Runnable() {
         @Override
         public void run() {
            if (!startStopVideoRecording(false)) {
               scheduleStopVideoRecording(5, TimeUnit.SECONDS);
            }
         }
      }, time, timeUnit);
   }

   /**
    * @param start true to start, false to stop
    * @return true if started successfully
    */
   public boolean startStopVideoRecording(boolean start) {
      if (CREDENTIAL_ID.get() == null) {
         loginAndKeepHeart();
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Video recording is " + (start ? "starting" : "stopping"));
      }

      HttpURLConnection httpURLConnection = createPostRequest(String.format(MANUAL_RECORDING_URL, cameraIp),
            String.format(MANUAL_RECORDING_REFERRER, cameraIp),
            String.format(MANUAL_RECORDING_POST_BODY, start ? 1 : 0, CREDENTIAL_ID.get()),
            null);
      int responseCode = getResponseCode(httpURLConnection);

      disconnectConnection(httpURLConnection);

      if (responseCode == HttpURLConnection.HTTP_OK) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Video recording " + (start ? "started" : "stopped"));
         }
         return true;
      } else {
         LOGGER.error("Video recording wasn't " + (start ? "started" : "stopped") + ". Response code: " + responseCode);
         return false;
      }
   }

   @Scheduled(fixedDelay=30000)
   public void loginAndKeepHeart() {
      if (CREDENTIAL_ID.get() == null) {
         LOGGER.debug("Logging in starting...");
         CREDENTIAL_ID.set(login());
      } else {
         keepHeart("ID=" + CREDENTIAL_ID.get());
         //keepHeart(String.format(KEEP_HEART_POST_BODY, CREDENTIAL_ID.get(), Math.random()));
      }
   }

   private void keepHeart(String requestBody) {
      HttpURLConnection httpURLConnection = createPostRequest(String.format(KEEP_HEART_URL, cameraIp),
            String.format(KEEP_HEART_REFERRER, cameraIp), requestBody, KEEP_HEART_COOKIES);

      int responseCode = getResponseCode(httpURLConnection);
      int[] responseContent = null;

      if (responseCode != HttpURLConnection.HTTP_OK) {
         LOGGER.error("Invalid response code on keep heart: " + responseCode);
      } else {
         responseContent = readRawResponseBody(httpURLConnection);
      }

      disconnectConnection(httpURLConnection);

      if (!Arrays.equals(KEEP_HEART_SUCCESSFULLY_RESPONSE, responseContent)) {
         CREDENTIAL_ID.set(null);
         LOGGER.error("Invalid response body on keep heart: " + Arrays.toString(responseContent));
         loginAndKeepHeart();
      }
   }

   private HttpURLConnection createPostRequest(String urlParam, String referrerHeaderValue, String postBody,
                                               String cookies) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL url = new URL (urlParam);
         httpURLConnection = (HttpURLConnection) url.openConnection();
         httpURLConnection.setDoOutput(true);
         httpURLConnection.setConnectTimeout(5000);
         httpURLConnection.setReadTimeout(5000);
         httpURLConnection.setInstanceFollowRedirects(false);
         httpURLConnection.setRequestMethod(POST_METHOD);
         httpURLConnection.setRequestProperty(HOST_HEADER, cameraIp);
         httpURLConnection.setRequestProperty(REFERRER_HEADER, referrerHeaderValue);

         if (cookies != null) {
            httpURLConnection.setRequestProperty(COOKIE_HEADER, cookies);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (httpURLConnection == null) {
         return null;
      }

      try (BufferedOutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream())) {
         byte[] loginPostBodyBytes = postBody.getBytes();

         for (int i = 0; i < loginPostBodyBytes.length; i++) {
            outputStream.write(loginPostBodyBytes[i]);
         }
      } catch (IOException e) {
         httpURLConnection = null;
         LOGGER.error(e);
      }
      return httpURLConnection;
   }

   private String readResponseBody(HttpURLConnection httpURLConnection) {
      if (httpURLConnection == null) {
         return null;
      }

      StringBuilder responseStringBuilder = new StringBuilder();

      try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
         String inputLine;

         while ((inputLine = in.readLine()) != null) {
            responseStringBuilder.append(inputLine);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }
      return responseStringBuilder.length() > 0 ? responseStringBuilder.toString() : null;
   }

   private int[] readRawResponseBody(HttpURLConnection httpURLConnection) {
      if (httpURLConnection == null) {
         return null;
      }

      List<Integer> bytes = new LinkedList<>();
      int inputByte;

      try (BufferedInputStream in = new BufferedInputStream(httpURLConnection.getInputStream())) {
         while ((inputByte = in.read()) != -1) {
            bytes.add(inputByte);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (bytes.size() > 0) {
         int[] result = new int[bytes.size()];

         for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
         }
         return result;
      } else {
         return new int[0];
      }
   }

   private int getResponseCode(HttpURLConnection httpURLConnection) {
      int responseCode = 0;

      if (httpURLConnection != null) {
         try {
            responseCode = httpURLConnection.getResponseCode();
         } catch (IOException e) {
            LOGGER.error(e);
         }
      }
      return responseCode;
   }

   private void disconnectConnection(HttpURLConnection httpURLConnection) {
      if (httpURLConnection != null) {
         httpURLConnection.disconnect();
      }
   }
}
