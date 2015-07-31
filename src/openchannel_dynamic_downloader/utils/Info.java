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
    
    public static final String APP_VERSION = "0.0.1";
    
    public static final int DEFAULT_APP_PORT = 50585;
    
    public static boolean isFtr=true;//change not initialized here
    
    
    public static class Resource{
         public static final String FXML_PREFIX = "/openchannel_dynamic_downloader/view/fxml/";
         
         public static final String FXML_FILE_MAIN=FXML_PREFIX+"FxmlMainView.fxml";
         public static final String FXML_FILE_DOWNLOADS=FXML_PREFIX+"FxmlDownloadsView.fxml";
         public static final String FXML_FILE_SCHEDULER=FXML_PREFIX+"FxmlSchedulerView.fxml";
         public static final String FXML_FILE_LOGIN=FXML_PREFIX+"FxmlLoginView.fxml";
         public static final String[] FXML_FILES={FXML_FILE_MAIN,FXML_FILE_DOWNLOADS,FXML_FILE_SCHEDULER,FXML_FILE_LOGIN};
    
         
         public static final String OCPI="/openchannel_dynamic_downloader/resources/images/openChannelPlaceholderIcon.png";
         public static final String IMAGE_OC_TRANS="/openchannel_dynamic_downloader/resources/images/openChanTrans.png";
         
    }
   
    public static class PreferenceData{
        
    }
    
    private Info(){}
    
    
    
    

    
}
