import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DriveHelper {
    /** Application name. */
    static final String APPLICATION_NAME =
            "Drive API Java Quickstart";

    /** Directory to store user credentials for this application. */
    static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    static HttpTransport HTTP_TRANSPORT;

    /** Инстанс для работы с Google Drive. */
    static Drive service;

    /** ID папки, в которую загружаются файлы */
    static String folderID;

    /** Название папки, куда загружаются файлы */
    static String FOLDER_NAME = "FoodManagerContent";

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);

            service = getDriveService();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                DriveHelper.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Чтобы не было проблем с загрузкой файлов, выставлены большие таймауты.
     */
    private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                requestInitializer.initialize(httpRequest);
                httpRequest.setConnectTimeout(300 * 60000);  // 300 minutes connect timeout
                httpRequest.setReadTimeout(300 * 60000);  // 300 minutes read timeout
            }
        };
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }

    /** Создание папки в Google Drive */
    public static void createFolder() throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(FOLDER_NAME);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = service.files().create(fileMetadata).setFields("id").execute();
        folderID = file.getId();
    }

    /** Удаление файла по его ID(в том числе и папки) */
    public static void deleteFile(String fileID) {
        try {
            service.files().delete(fileID).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }

    /** инициализация перед загрузкой */
    public static void setUp() throws Exception {
        String pageToken = null;
        FileList result = service.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + FOLDER_NAME + "'")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .execute();
        if (result.getFiles().size() != 0) {
            deleteFile(result.getFiles().get(0).getId());
        }

        createFolder();
    }

    /**
     * Загрузка файла на Google Drive
     * @param filename имя файла
     * @param path путь к файлу
     * @return ID файла
     */
    public static String uploadFile(String filename, String path) throws IOException {
        // Build a new authorized API client service.

        File uploadFile = new File();
        uploadFile.setName(filename);
        uploadFile.setParents(Collections.singletonList(folderID));
        java.io.File filePath = new java.io.File(path);
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        File file = service.files().create(uploadFile, mediaContent).setFields("id, parents").execute();

        return file.getId();
    }
}