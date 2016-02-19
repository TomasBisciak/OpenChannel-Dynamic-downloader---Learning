/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import openchannel_dynamic_downloader.security.UserProfile;

/**
 * Singleton
 * @author tomas
 */
public class MainDataModel {

    /**
     * Currently used login profile "Logged as" 
     */
    public UserProfile loginProfile;//profile that user is logged in as.

    private static String downloadDir;//aktualne pouzivany//NOT USED ATM

    private static final MainDataModel instance=new MainDataModel();
    
    public long latestId=0;//used in postfix nameFile

    private MainDataModel() {
        //maybe get latest id at creation
    }

    public static MainDataModel getInstance() {
        return instance;
    }
    
    

    public static String getDownloadDir() {
        return downloadDir;
    }

    public static boolean setDownloadDir(String aDownloadDir) {//must be valid location//can be set at preferences initialization at applicaitons first stages of execution
        if (Files.isDirectory(Paths.get(aDownloadDir)) && Files.exists(Paths.get(aDownloadDir))) {
            downloadDir = aDownloadDir;
            return true;
        }//esle keep dir as it was before
        return false;
    }

    public long getLatestId() {
        return latestId;
    }

    public void setLatestId(int latestId) {
        this.latestId = latestId;
    }
    

}
