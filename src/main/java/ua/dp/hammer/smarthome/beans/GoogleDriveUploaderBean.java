package ua.dp.hammer.smarthome.beans;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class GoogleDriveUploaderBean {

   /*private static final Logger LOGGER = LogManager.getLogger(GoogleDriveUploaderBean.class);

   private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
   private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
   private static HttpTransport httpTransport;
   private FileDataStoreFactory dataStoreFactory;

   private static final String READ_ERROR_MSG = " file can't be read.";
   private static final String UPLOADING_INFO_MSG = "Uploading has been completed. %s file has been uploaded at %.1f seconds. Average speed: %.1fKB/s";
   private static final String DATA_STORE_FACTORY_ERROR_MSG = "Data Store Factory wasn't created.";
   private static final String FILE_IS_READY_TO_UPLOAD = " file if ready to upload.";
   private static final String CREDENTIALS_APPLICATION_NAME = "SecurityVideoUploader";

   private static final Map<Long, UploadFile> BEING_UPLOADED_FILES = Collections.synchronizedMap(new HashMap<Long, UploadFile>());

   private String googleDriveUser;
   private String googleDriveFolderId;

   static {
      try {
         httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      } catch (GeneralSecurityException e) {
         LOGGER.error(e);
      } catch (IOException e) {
         LOGGER.error(e);
      }
   }

   private File googleCredentialsFile;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      googleDriveUser = environment.getRequiredProperty("googleDriveUser");
      googleDriveFolderId = environment.getRequiredProperty("googleDriveFolderId");
      String googleCredentialsFilePath = environment.getRequiredProperty("googleCredentialsFile");
      googleCredentialsFile = new File(googleCredentialsFilePath);
      File dataStoreDir = new File(googleCredentialsFile.getParent());

      try {
         dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
      } catch (IOException e) {
         LOGGER.error(e);
      }
   }*/

   @Async
   public void transferVideoFile(Path path) {
      /*if (!Files.isReadable(path)) {
         LOGGER.error(path + READ_ERROR_MSG);
         return;
      }

      File file = path.toFile();
      long fileLength = file.length();
      LOGGER.info(path.getFileName() + FILE_IS_READY_TO_UPLOAD + " Size: " + (fileLength > 0 ? fileLength / 1024 : 0) + "KB");
      BEING_UPLOADED_FILES.put(fileLength, new UploadFile(file.getName()));

      Drive service = getDriveService();

      if (service == null) {
         return;
      }

      BufferedInputStream bufferedInputStream = null;
      try {
         com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File()
               .setTitle(file.getName())
               .setParents(Arrays.asList(new com.google.api.services.drive.model.ParentReference().setId(googleDriveFolderId)));
         bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
         InputStreamContent mediaContent = new InputStreamContent("image/png", bufferedInputStream);
         mediaContent.setLength(fileLength);
         Drive.Files.Insert request = service.files().insert(fileMetadata, mediaContent);
         request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
         request.execute();
      } catch (GoogleJsonResponseException gjre) {
         //gjre.getStatusCode();
         LOGGER.error(gjre);
      } catch (IOException e) {
         LOGGER.error(e);
      } finally {
         if (bufferedInputStream != null) {
            try {
               bufferedInputStream.close();
            } catch (IOException e) {
               LOGGER.error(e);
            }
         }
      }*/
   }

   /**
    * @return an authorized Drive client service
    */
   /*private Drive getDriveService() {
      Credential credential = authorize();

      if (credential == null) {
         return null;
      }

      return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(CREDENTIALS_APPLICATION_NAME)
            .build();
   }*/

   /**
    * @return an authorized Credential object
    */
   /*private Credential authorize() {
      InputStreamReader in = null;
      Credential credential = null;

      try {
         if (dataStoreFactory == null) {
            throw new IOException(DATA_STORE_FACTORY_ERROR_MSG);
         }

         in = new InputStreamReader(new FileInputStream(googleCredentialsFile));
         GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, in);

         // Build flow and trigger user authorization request.
         GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
               .Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
               .setDataStoreFactory(dataStoreFactory)
               .setAccessType("offline")
               .build();

         credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(googleDriveUser);
      } catch (FileNotFoundException e) {
         LOGGER.error(e);
      } catch (IOException e) {
         LOGGER.error(e);
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               LOGGER.error(e);
            }
         }
      }
      return credential;
   }

   private static class CustomProgressListener implements MediaHttpUploaderProgressListener {
      public void progressChanged(MediaHttpUploader uploader) throws IOException {
         switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
               break;
            case INITIATION_COMPLETE:
               break;
            case MEDIA_IN_PROGRESS:
               break;
            case MEDIA_COMPLETE:
               long fileLength = uploader.getNumBytesUploaded();
               UploadFile uploadedFile = BEING_UPLOADED_FILES.get(fileLength);
               float elapsedTimeS = (float)(System.currentTimeMillis() - uploadedFile.getCreatedTime()) / 1000f;
               float speedKbs = (float) (fileLength) / elapsedTimeS / 1024f;
               BEING_UPLOADED_FILES.remove(fileLength);
               LOGGER.info(new Formatter().format(UPLOADING_INFO_MSG, uploadedFile.getFileName(), elapsedTimeS, speedKbs));
               break;
         }
      }
   }

   private static class UploadFile {
      private long createdTime;
      private String fileName;

      public UploadFile(String fileName) {
         this.fileName = fileName;
         createdTime = System.currentTimeMillis();
      }

      public long getCreatedTime() {
         return createdTime;
      }

      public String getFileName() {
         return fileName;
      }
   }*/
}
