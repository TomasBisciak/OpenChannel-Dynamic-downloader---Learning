/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import openchannel_dynamic_downloader.controllers.FxmlMainViewController;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.scheduler.Scheduler;
import openchannel_dynamic_downloader.statistics.DailyStat;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author Kofola
 */
public class DownloaderDaemonThread extends Thread {// CANNOT UPDATE DATABSE, if database is beign written to at the time of applicaiton close then its fucked

    private long refreshRate = 1000;//default value that this threat refreshes at

    private boolean downloadsScheduled = false;
    private boolean initialIteration = true;

    private double ds = 0;
    private double avgdp = 0;
    private int divBy = 0;
    private int avgMinute = 0;
    private int avgHour = 0;

    /* private final SimpleDoubleProperty totalDownVal = new SimpleDoubleProperty(1);
    

     public SimpleDoubleProperty totalDownValProperty() {
     return totalDownVal;
     }*/
    public DownloaderDaemonThread(long refreshRate) {// ONLY FOR PERFORMANCE TESTING DO NOT USE IN ACTUAL CODE!!!! refresh rate has to be 1k milisec
        this.refreshRate = refreshRate;
        this.setDaemon(true);

    }

    public DownloaderDaemonThread() {
        this.setPriority(MAX_PRIORITY);
        this.setDaemon(true);

    }

    int iterNum = 0;

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {//make as effective as possible
        while (true) {

            long tStart = System.nanoTime();

            ds = 0;

            for (DownloadTask dt : Downloader.getDtCache()) {
                if (dt.getState() == DownloadUnit.STATE_DOWNLOADING) {
                    ds += dt.getDownloadSpeed();
                }
                if (dt.getState() == DownloadUnit.STATE_DOWNLOADING || dt.getState() == DownloadUnit.STATE_PAUSED) {
                    avgdp += dt.getProgress();
                    divBy++;
                }
            }

            Platform.runLater(() -> {
                // System.out.println("TEST98-Avg download progress:" + avgdp / divBy + " VALUES  tempaAvgdp:" + avgdp + " tempDivBy: " + divBy);
                //totalDownVal.set(tempaAvgdp/tempDivBy);
                MainDataModel.downloadSpeedProperty().set("Download " + ds + " kB/s");
                MainDataModel.totalDownValProperty().set(avgdp / divBy);
                FxmlMainViewController.addGraphSeriesSpeedToSeconds(ds);//add to seconds
            });

            //THIS CODE IS FOR SCHEDULER
            if (Scheduler.getInstance().getActive()) {
                if (!Scheduler.getInstance().getCurrentFlag()) {

                    if (!downloadsScheduled) {
                        Downloader.getDtCache().forEach((dt -> {
                            if (dt.getState() == DownloadUnit.STATE_DOWNLOADING) {//or paused as well? no fkn idea ill see
                                //dt.setState(DownloadUnit.STATE_SCHEDULED);// prolly call differenteee 
                                dt.schedule();
                                downloadsScheduled = true;
                            }
                        }));
                    }
                    initialIteration = false;
                }
            } else {
                //gonna be false on start no matter what.
                if (downloadsScheduled || initialIteration) {//if they were sheduled  unschedule them.PROBLEM IS MOTHERFUCKN restart
                    Downloader.getDtCache().forEach((dt -> {
                        if (dt.getState() == DownloadUnit.STATE_SCHEDULED) {//or paused as well? no fkn idea ill see
                            dt.resume();
                            downloadsScheduled = false;
                        }
                    }));
                    initialIteration = false;
                }
            }
            
            

            Platform.runLater(() -> {
                //TAKE CARE OF GRAPHS and its resolutions

                if (iterNum % 60 == 0) {//minuteees
                    //ADD AVERAGE SPEED PER 1 MINUTE, average speed  60 seconds previous
                    for (XYChart.Data xydata : (ObservableList<XYChart.Data>) FxmlMainViewController.getSpeedSeriesSeconds().getData()) {
                        avgMinute += (double) xydata.getYValue();
                    }
                    avgMinute /= FxmlMainViewController.getSpeedSeriesSeconds().getData().size();
                    FxmlMainViewController.addGraphSeriesSpeedToMinutes(avgMinute);//minutes
                }
                if (iterNum % 3600 == 0) {//hours
                    //add average speed of 60 minutes
                    for (XYChart.Data xydata : (ObservableList<XYChart.Data>) FxmlMainViewController.getSpeedSeriesMinutes().getData()) {
                        avgHour += (double) xydata.getYValue();
                    }
                    avgHour /= FxmlMainViewController.getSpeedSeriesSeconds().getData().size();
                    FxmlMainViewController.addGraphSeriesSpeedToHours(avgHour);

                }

                if (iterNum % 5 == 0) {//maybe faster refresh. bud not sure//and parallelize maybe// shoud be over 10 seconds , thats not really cool
                    int[] stateAmounts = new int[DownloadUnit.STATE_STRINGS.length];

                    for (DownloadUnit du : Downloader.getDownloads()) {
                        //remove all previous data
                        switch (du.getState()) {
                            case -1: {
                                stateAmounts[0]++;
                                break;
                            }
                            case 0: {
                                stateAmounts[1]++;
                                break;
                            }
                            case 1: {
                                stateAmounts[2]++;
                                break;
                            }
                            case 2: {
                                stateAmounts[3]++;
                                break;
                            }
                            case 3: {
                                stateAmounts[4]++;
                                break;
                            }
                            case 4: {
                                stateAmounts[5]++;
                                break;
                            }
                        }

                    }//end iterate over dunits

                    //remove series here
                    for (int i = 0; i < stateAmounts.length; i++) {
                        FxmlMainViewController.addToStateSeries(i, stateAmounts[i]);
                    }

                }

            });

            //EVERY MINUTE CHECK WHAT DAY IS IT.// FOR DAILY STATS tracking
            //THIS MAY CAUSE inaccurate counting of bytes ,if you rely on totally acurate daily byteCount , refresh of currentDate has to be less then or equal to a second.
            if (iterNum % 60 == 0) {

                //PROBLEM IS IF USER CHANGES timezone on pc everything is fucked , between today session end and today session start.
                //use unified formatting thru whole app prolly
                if (!MainDataModel.getInstance().loginProfile.getPreferences().get(Info.PreferenceData.PREF_USER_DAILY_STAT_DATEID, "").equals(LocalDate.now().toString())) {//new day is different then preferences one , 
                    //get downloaded bytes for today  and WRITE DATA INTO DB.
                    //new day insert previous day int odailyStats
                    DbUtil.insertIntoDailyStats(new DailyStat(DailyStat.createDateId(LocalDate.now()), MainDataModel.downloadedBytesTodayProperty().
                            getValue(), MainDataModel.getNumOfDownloadsTodayProperty().getValue()));
                    //new day downloaded today 0
                    MainDataModel.downloadedBytesTodayProperty().set(0);

                    /*
                     long dataPrevDay=MainDataModel.downloadedBytesTodayProperty().getValue();
                     DbUtil.insertIntoDailyStats(new DailyStat(DailyStat.createDateId(LocalDate.now()),MainDataModel.downloadedBytesTodayProperty().getValue(),0));
                     //write that shit into db
                    
                     //make sure to sutracat PREVIOSU DAY/days AMOUNT FROM  session data that updates totalDay bytes
                     MainDataModel.incrementPreviousDaysBytes(dataPrevDay);
                     //zapis nulu do predchadzajuceho session z tohto dna
                     MainDataModel.getInstance().loginProfile.getPreferences().putLong(Info.PreferenceData.PREF_USER_DAILY_STAT_DOWNBYTE,0);
                     //ZAPIS NOVY DEN. do preferencii
                     MainDataModel.getInstance().loginProfile.getPreferences().put(Info.PreferenceData.PREF_USER_DAILY_STAT_DATEID,LocalDate.now().toString());
                 
                     */
                }

            }
            System.out.println("Nano time start:"+System.nanoTime());
             System.out.println("Nano time start:"+tStart);
             System.out.println("Nano time difference:"+tStart);
            long tElapsed = (System.nanoTime() - tStart)/1000000;//milisec
             System.out.println("Nano time elapsed:"+tElapsed+" ms");
            if ((refreshRate-tElapsed)>=0) {
                try {
                    System.out.println("Time elapsed:"+tElapsed+" ms");
                    System.out.println("Sleep time "+(refreshRate-tElapsed)+" ms");
                    Thread.sleep(refreshRate-tElapsed);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            iterNum++;
            //go thru all of the active downloads in dtcache and get progress, then update total downlaod progress bar
        }

    }

}
