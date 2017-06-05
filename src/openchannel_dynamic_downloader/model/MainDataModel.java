/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.statistics.DailyStat;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;
import openchannel_dynamic_downloader.utils.MiscUtils;

/**
 * Singleton
 *
 * @author tomas
 */
public class MainDataModel {

    /**
     * Currently used login profile "Logged as"
     */
    public UserProfile loginProfile;//profile that user is logged in as.
    //other crucial data for application

    private static final MainDataModel instance = new MainDataModel();

    public long latestId = 0;//used in postfix nameFile

    private final ObservableList<DailyStat> dailyStats = FXCollections.observableArrayList();

    //private static String averageDownloadSpeed = "";
    private static final SimpleStringProperty downloadSpeed = new SimpleStringProperty("");

    public static SimpleStringProperty downloadSpeedProperty() {
        return downloadSpeed;
    }
    //might move all this to MainDataModel since it makes more sense and makes it more comfortable to work with it
    private static final SimpleStringProperty downloadedBytesSessionString = new SimpleStringProperty("");
    private static final SimpleLongProperty downloadedBytesSession = new SimpleLongProperty(0);//not stored in any//
    private static final Object incrementBytesLock = new Object();//used for incrementDownBytesSession,
    //totalDownDataStringProperty
    private static final SimpleStringProperty downloadedBytesTotalString = new SimpleStringProperty("");
    private static final SimpleLongProperty downloadedBytesTotal = new SimpleLongProperty(0);

    private static final SimpleStringProperty downloadedBytesTodayString = new SimpleStringProperty("");
    private static final SimpleLongProperty downloadedBytesToday = new SimpleLongProperty(0);
    private static final SimpleIntegerProperty numOfDownloadsToday=new SimpleIntegerProperty(0);

    private static final SimpleDoubleProperty totalDownVal = new SimpleDoubleProperty(1);//used for total progress of downloads
    
    

    public static SimpleIntegerProperty getNumOfDownloadsTodayProperty(){
        return numOfDownloadsToday;
    }
    
    public static SimpleStringProperty downloadedBytesTodayStringProperty() {
        return downloadedBytesTodayString;
    }

    public static SimpleLongProperty downloadedBytesTodayProperty() {
        return downloadedBytesToday;
    }

    public static SimpleStringProperty downloadedBytesTotalStringProperty() {
        return downloadedBytesTotalString;
    }

    public static SimpleLongProperty downloadedBytesTotalProperty() {
        return downloadedBytesTotal;
    }

    public static SimpleDoubleProperty totalDownValProperty() {
        return totalDownVal;
    }

    public static SimpleLongProperty downloadedBytesSessionProperty() {
        return downloadedBytesSession;
    }

    public static SimpleStringProperty downloadedBytesSessionStringProperty() {
        return downloadedBytesSessionString;
    }

    private static final DailyStat dailyStat=new DailyStat(
            DailyStat.createDateId(LocalDate.now()),
                    downloadedBytesTodayProperty().getValue(),
                    getNumOfDownloadsTodayProperty().getValue());
    
    public DailyStat getDailyStat(){
        return dailyStat;
    }
    
    public static void incrementDownBytesStat(long value) {//might be needed if i want to implemnt feature of limiting downloaded data per period /session whatever
        synchronized (incrementBytesLock) {
            downloadedBytesSession.setValue(downloadedBytesSession.getValue() + value);
            downloadedBytesToday.setValue(downloadedBytesToday.getValue()+value);
            dailyStat.setBytesDownloaded(downloadedBytesToday.getValue());
        }
    }
    
//    @SuppressWarnings("FieldMayBeFinal")
//    private static long previousDaysBytes=0;
//    
//    public static void incrementPreviousDaysBytes(long incrementBy){
//        previousDaysBytes+=incrementBy;
//    }

    private static void initFXProperties() {
        
        downloadedBytesSession.addListener((ObservableValue<? extends Number> val, Number oldVal, Number newVal) -> {
            Platform.runLater(() -> {
                downloadedBytesSessionString.setValue(MiscUtils.humanReadableByteCount((long) newVal));
                downloadedBytesTotal.setValue(downloadedBytesTotal.getValue()+((long)newVal-(long)oldVal));
                //downloadedBytesToday.setValue(downloadedBytesToday.getValue()+((long)newVal-(long)oldVal));
                
            });
        });
        downloadedBytesTotal.addListener((ObservableValue<? extends Number> val, Number oldVal, Number newVal) -> {
            Platform.runLater(() -> {
                downloadedBytesTotalString.setValue(MiscUtils.humanReadableByteCount((long) newVal));
            });

        });
        downloadedBytesToday.addListener((ObservableValue<? extends Number> val, Number oldVal, Number newVal) -> {
            Platform.runLater(() -> {
                downloadedBytesTodayString.setValue(MiscUtils.humanReadableByteCount((long) newVal));
            });

        });

    }

    private MainDataModel() {
    }

    /**
     * Called on sucessfull login, data can be initialized here , runs before mainController initialization
     */
    public final void initialize() {
        
        dailyStats.addAll(DbUtil.getDailyStats());
        initFXProperties();
        initPreferences();
        //initialize ON LOGIN  preferences for instance , 
    }

    private void initPreferences() {

    
        //load total downloaded bytes from all days , put all days together to get value
        downloadedBytesTotal.set(DbUtil.getTotalDownloadedBytes());
        //add ID of the day that is currently  to preferences
        
        
        if (loginProfile.getPreferences().get(Info.PreferenceData.PREF_USER_DAILY_STAT_DATEID, "").equals(LocalDate.now().toString())) {
            //add previous data from today to our dayliTotal//
            downloadedBytesToday.set(loginProfile.getPreferences().getLong(Info.PreferenceData.PREF_USER_DAILY_STAT_DOWNBYTE, 0));//dailyData
            numOfDownloadsToday.set(loginProfile.getPreferences().getInt(Info.PreferenceData.PREF_USER_DAILY_STAT_NUMOFDOWN, 0));
        }
        //other
        //add day id after comparsion
        loginProfile.getPreferences().put(Info.PreferenceData.PREF_USER_DAILY_STAT_DATEID,LocalDate.now().toString());
        
        
    }

    public static MainDataModel getInstance() {
        return instance;
    }

    public long getLatestId() {
        return latestId;
    }

    public void setLatestId(int latestId) {
        this.latestId = latestId;
    }

    public ObservableList<DailyStat> getDailyStats() {
        return dailyStats;
    }

}
