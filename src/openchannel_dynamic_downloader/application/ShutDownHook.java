
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.application;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import openchannel_dynamic_downloader.downloader.Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import static openchannel_dynamic_downloader.model.MainDataModel.downloadedBytesTodayProperty;
import static openchannel_dynamic_downloader.model.MainDataModel.getNumOfDownloadsTodayProperty;
import openchannel_dynamic_downloader.statistics.DailyStat;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 *
 * @author Kofola
 */
public class ShutDownHook extends Thread {

    private final boolean interupt;

    public ShutDownHook(boolean interupt) {
        this.interupt = interupt;
    }

    @Override
    public void run() {
        //Short cleanup maximum access database no advanced calculations here 
        //  DbUtil.insertIntoDailyStats(555l);
        if (interupt) {
            return;
        }
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            Logger.getLogger(ShutDownHook.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("ShutdownHook Execution");

        DbUtil.insertIntoDailyStats(123l);

        MainDataModel.getInstance().loginProfile.getPreferences().putLong(
                Info.PreferenceData.PREF_USER_DAILY_STAT_DOWNBYTE, MainDataModel.downloadedBytesTodayProperty().get());
        System.out.println("WRITTEN DATA INTO PREF:" + MainDataModel.downloadedBytesTodayProperty().get());

            // DbUtil.insertIntoDailyStats(new DailyStat(20160324,
        //                   555,
        //                   0));
//       DbUtil.insertIntoDailyStats(new DailyStat( DailyStat.createDateId(LocalDate.now()),
//                    MainDataModel.downloadedBytesTodayProperty().getValue(),
//                    MainDataModel.getNumOfDownloadsTodayProperty().getValue()));
        System.out.println("ShutdownHook Execution finished");
        //ADDITIONAL  stuff.
    }

}
