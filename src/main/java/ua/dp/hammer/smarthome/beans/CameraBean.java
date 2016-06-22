package ua.dp.hammer.smarthome.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CameraBean {
   private static final Logger LOGGER = LogManager.getLogger(CameraBean.class);

   private static final String LOGIN_URL = "http://%s/goform/WEB_UsrLoginAjaxProc";
   private static final String LOGIN_POST_BODY = "UserName=%1$s&Password=%2$s&LanguageParam=1";
   private static final String REFERRER_PAGE = "http://%s/asppage/common/login.asp?id=1&ret=1";
   private static final String START_STOP_RECORDING = "XMLData=<MPLDCProtocol ProtocolType=\"1\">" +
         "<MPLDCPDeviceConfig ReturnValue=\"0\" OperateType=\"2\">" +
         "<DeviceInfoEx User=\"\" Password=\"\" IP=\"%1$s\" Port=\"30001\" DeviceID=\"%2$s\"/>" +
         "<DeviceConfigID ID=\"DeviceConfig_AlarmIn\"/>" +
         "<ConfigItem ID=\"staticAlarmInValidLevel\" Value=\"%3$d\"/>" +
         "</MPLDCPDeviceConfig>" +
         "</MPLDCProtocol>" +
         "&LanguageId=1&ID=%4$s&Num=2";
   private static final String CAMERA_ID = "0018A2";
   private static final String POST_METHOD = "POST";
   private static final String HOST_HEADER = "Host";
   private static final String REFERRER_HEADER = "Referer";
   private static final Pattern ID_FROM_RESPONSE_PATTERN = Pattern.compile("ID=(\\d+)");

   private String cameraIp;
   private String cameraLogin;
   private String cameraPassword;
   private String credentialId;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      cameraIp = environment.getRequiredProperty("cameraIp");
      cameraLogin = environment.getRequiredProperty("cameraLogin");
      cameraPassword = environment.getRequiredProperty("cameraPassword");

      login();
   }

   public String login() {
      HttpURLConnection httpURLConnection = null;

      try {
         URL url = new URL(String.format(LOGIN_URL, cameraIp));
         httpURLConnection = (HttpURLConnection) url.openConnection();
         httpURLConnection.setDoOutput(true);
         httpURLConnection.setRequestMethod(POST_METHOD);
         httpURLConnection.setRequestProperty(HOST_HEADER, cameraIp);
         httpURLConnection.setRequestProperty(REFERRER_HEADER, String.format(REFERRER_PAGE, cameraIp));
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (httpURLConnection == null) {
         return null;
      }

      try (BufferedOutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream())) {
         String loginPostBody = String.format(LOGIN_POST_BODY, cameraLogin, cameraPassword);
         byte[] loginPostBodyBytes = loginPostBody.getBytes();

         for (int i = 0; i < loginPostBodyBytes.length; i++) {
            outputStream.write(loginPostBodyBytes[i]);
         }
      } catch (IOException e) {
         LOGGER.error(e);
      }

      int responseCode = 0;
      try {
         responseCode = httpURLConnection.getResponseCode();
      } catch (IOException e) {
         LOGGER.error(e);
      }

      if (responseCode != HttpURLConnection.HTTP_OK) {
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

      String response = responseStringBuilder.toString();
      Matcher matcher = ID_FROM_RESPONSE_PATTERN.matcher(response);
      String id = null;

      if (matcher.find(1)) {
         id = matcher.group(1);
      }
      return id;
   }
}
