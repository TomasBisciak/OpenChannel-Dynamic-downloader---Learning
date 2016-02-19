/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.utils;

/**
 *
 * @author tomas
 */
public class Info {

    public static final String APP_VERSION = "0.0.5";

    public static final int DEFAULT_APP_PORT = 50585;
    public static final String OC_DIR = System.getProperty("user.home") + "\\.OpenChannel\\";
    public static final String OC_DOWNLOADS_DIR = OC_DIR + "Downloads\\";
    public static final String OC_TEMP_DIR = OC_DIR + "Temp_Dont_Delete_Dont_Use\\";
    // public static boolean isFtr=true;//Application first time run

    static {

    }

    public static class Resource {

        public static final String FXML_PREFIX = "/openchannel_dynamic_downloader/view/fxml/";

        public static final String FXML_FILE_MAIN = FXML_PREFIX + "FxmlMainView.fxml";
        public static final String FXML_FILE_DOWNLOADS = FXML_PREFIX + "FxmlDownloadsView.fxml";
        public static final String FXML_FILE_SCHEDULER = FXML_PREFIX + "FxmlSchedulerView.fxml";
        public static final String FXML_FILE_LOGIN = FXML_PREFIX + "FxmlLoginView.fxml";
        public static final String FXML_FILE_DATABASE = FXML_PREFIX + "FxmlDatabaseView.fxml";
        public static final String FXML_FILE_PREF = FXML_PREFIX + "FxmlPreferencesView.fxml";
        public static final String FXML_FILE_ABOUT = FXML_PREFIX + "FxmlAboutView.fxml";
        public static final String FXML_FILE_CHECKSUM = FXML_PREFIX + "FxmlCheckSumView.fxml";
        public static final String FXML_FILE_TUTORIAL = FXML_PREFIX + "FxmlTutorialView.fxml";
        public static final String FXML_FILE_CLOUD = FXML_PREFIX + "FxmlCloudView.fxml";
        public static final String[] FXML_FILES = {FXML_FILE_MAIN, FXML_FILE_DOWNLOADS, FXML_FILE_SCHEDULER, FXML_FILE_LOGIN, FXML_FILE_DATABASE};

        public static final String OCPI = "/openchannel_dynamic_downloader/resources/images/OpenChannel_logo16x16.png";
         public static final String OCPI16= "/openchannel_dynamic_downloader/resources/images/OpenChannel_logo16X16.png";
          public static final String OCPI32 = "/openchannel_dynamic_downloader/resources/images/OpenChannel_logo32X32.png";
           public static final String OCPI64= "/openchannel_dynamic_downloader/resources/images/OpenChannel_logo16X16.png";
        public static final String IMAGE_OC_TRANS = "/openchannel_dynamic_downloader/resources/images/openChanTrans.png";

    }

    public static class Db {

        public static final String DB_MAIN_USERNAME = "app";
        public static final String DB_MAIN_PASSWORD = "app";
    }

    public static class PreferenceData {

        public static final String PREF_APP_FIRST_TIME_RUN = "aftr";


        public static final String PREF_USER_FIRST_TIME_RUN = "uftr";
        public static final String PREF_USER_DOWNLOADS_DIR = "ddir";
        public static final String PREF_USER_NUMOFCON_THREADS="noct";

    }

    private Info() {
    }

}
