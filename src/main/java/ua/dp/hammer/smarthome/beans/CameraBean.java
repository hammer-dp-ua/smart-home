package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
   private static final String START_STOP_RECORDING = "AlarmInId=1&AlarmInName=1&AlarmInValidLevel=%1$s&AlarmOutId=1" +
         "&AlarmOutName=&AlarmOutValidSignal=2&AlarmMode=1&Frequency=0&AlarmTime=0&B1=OK" +
         "&ID=%2$s&AlarmOutDeviceType=1&AlarmOutFlag=1&languageType=1";
   private static final String KEEP_HEART_URL = "http://%s/asppage/common/keepHeart.asp";
   private static final String KEEP_HEART_REFERRER = "http://%s/asppage/common/top.asp";
   private static final String CAMERA_ID = "0018A2";
   private static final String POST_METHOD = "POST";
   private static final String HOST_HEADER = "Host";
   private static final String REFERRER_HEADER = "Referer";
   private static final Pattern ID_FROM_RESPONSE_PATTERN = Pattern.compile("ID=(\\d+)");

   private static final ScheduledThreadPoolExecutor STOP_VIDEO_RECORDING_EXECUTOR = new ScheduledThreadPoolExecutor(1);

   private String cameraIp;
   private String cameraLogin;
   private String cameraPassword;
   private long cameraRecordingTimeSec;
   private String credentialId;

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

      final long startTime = System.currentTimeMillis();
      LOGGER.info("~~~ 0. Thread: " + Thread.currentThread().getId() + "; Time: " + 0);
      /*startVideoRecording();

      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            LOGGER.info("~~~ 1. Thread: " + Thread.currentThread().getId() + "; Time: " + (System.currentTimeMillis() - startTime));
            startVideoRecording();
         }
      }, 10000L);

      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            LOGGER.info("~~~ 2. Thread: " + Thread.currentThread().getId() + "; Time: " + (System.currentTimeMillis() - startTime));
            startVideoRecording();
         }
      }, 15000L);

      new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
            LOGGER.info("~~~ 3. Thread: " + Thread.currentThread().getId() + "; Time: " + (System.currentTimeMillis() - startTime));
            startVideoRecording();
         }
      }, 20000L);*/
   }

   public String login() {
      HttpURLConnection httpURLConnection = createPostRequest(String.format(LOGIN_URL, cameraIp),
            String.format(LOGIN_REFERRER_URL, cameraIp), String.format(LOGIN_POST_BODY, cameraLogin, cameraPassword));

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
      return id;
   }

   @Async
   public void startVideoRecording() {
      startStopVideoRecording(true);

      BlockingQueue<Runnable> queue = STOP_VIDEO_RECORDING_EXECUTOR.getQueue();
      if (queue.size() > 0) {
         queue.remove(queue.element());
      }

      STOP_VIDEO_RECORDING_EXECUTOR.schedule(new Runnable() {
         @Override
         public void run() {
            startStopVideoRecording(false);
         }
      }, cameraRecordingTimeSec, TimeUnit.SECONDS);
   }

   /**
    * @param start true to start, false to stop
    * @return true if started successfully
    */
   public boolean startStopVideoRecording(boolean start) {
      String id = login();
      int responseCode = 0;

      if (id == null) {
         return false;
      }

      HttpURLConnection httpURLConnection = createPostRequest(String.format(ALARM_IO_PARAM_URL, cameraIp),
            String.format(REFERRER_IO_PARAM_URL, cameraIp),
            String.format(START_STOP_RECORDING, start ? 2 : 1, id));

      if (httpURLConnection != null) {
         try {
            responseCode = httpURLConnection.getResponseCode();
         } catch (IOException e) {
            LOGGER.error(e);
         }
      }
      return responseCode == HttpURLConnection.HTTP_MOVED_TEMP;
   }

   @Scheduled(fixedDelay=60000)
   public void loginAndKeepHeart() {
      if (credentialId == null) {
         credentialId = login();
      } else {
         createPostRequest(String.format(KEEP_HEART_URL, cameraIp), String.format(KEEP_HEART_REFERRER, cameraIp),
               "ID=" + credentialId);
      }
   }

   private HttpURLConnection createPostRequest(String urlParam, String referrerHeaderValue, String postBody) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL url = new URL(urlParam);
         httpURLConnection = (HttpURLConnection) url.openConnection();
         httpURLConnection.setDoOutput(true);
         httpURLConnection.setInstanceFollowRedirects(false);
         httpURLConnection.setReadTimeout(60000);
         httpURLConnection.setRequestMethod(POST_METHOD);
         httpURLConnection.setRequestProperty(HOST_HEADER, cameraIp);
         httpURLConnection.setRequestProperty(REFERRER_HEADER, referrerHeaderValue);
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
      StringBuilder responseStringBuilder = new StringBuilder();

      try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
         String inputLine;

         while ((inputLine = in.readLine()) != null) {
            responseStringBuilder.append(inputLine);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }
      return responseStringBuilder.toString();
   }
}
