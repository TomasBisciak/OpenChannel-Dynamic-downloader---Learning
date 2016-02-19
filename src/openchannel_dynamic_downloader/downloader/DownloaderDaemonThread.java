/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import openchannel_dynamic_downloader.controllers.FxmlMainViewController;
import openchannel_dynamic_downloader.scheduler.Scheduler;

/**
 *
 * @author Kofola
 */
public class DownloaderDaemonThread extends Thread {// CANNOT UPDATE DATABSE, if database is beign written to at the time of applicaiton close then its fucked

    private long refreshRate = 1000;//default value that this threat refreshes at
    private boolean downloadsScheduled = false;
    private boolean initialIteration = true;
    /* private final SimpleDoubleProperty totalDownVal = new SimpleDoubleProperty(1);
    

     public SimpleDoubleProperty totalDownValProperty() {
     return totalDownVal;
     }*/

    public DownloaderDaemonThread(long refreshRate) {// ONLY FOR PERFORMANCE TESTING DO NOT USE IN ACTUAL CODE!!!! refresh rate has to be 1k milisec
        this.refreshRate = refreshRate;
        this.setPriority(MIN_PRIORITY);
        this.setDaemon(true);

    }

    public DownloaderDaemonThread() {
        this.setPriority(MIN_PRIORITY);
        this.setDaemon(true);

    }

    int iterNum = 0;

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {//make as effective as possible
        double ds = 0;
        while (true) {

            ds = 0;

            for (DownloadTask dt : Downloader.getDtCache()) {
                if (dt.getState() == DownloadUnit.STATE_DOWNLOADING) {
                    ds += dt.getDownloadSpeed();

                }
            }
            final double tempDownSpeed = ds;
            Platform.runLater(() -> {
                Downloader.downloadSpeedProperty().set("Download " + tempDownSpeed + " kB/s");
            });

            double avgdp = 0;
            int divBy = 0;
            for (DownloadTask dt : Downloader.getDtCache()) {
                if (dt.getState() == DownloadUnit.STATE_DOWNLOADING || dt.getState() == DownloadUnit.STATE_PAUSED) {
                    avgdp += dt.getProgress();
                    divBy++;
                }
            }
            //TODO JESUS PLEASE OPTIMIZE!
            final double tempaAvgdp = avgdp;
            final int tempDivBy = divBy;
            Platform.runLater(() -> {
                System.out.println("TEST98-Avg download progress:" + tempaAvgdp / tempDivBy + " VALUES  tempaAvgdp:" + tempaAvgdp + " tempDivBy: " + tempDivBy);
                //totalDownVal.set(tempaAvgdp/tempDivBy);
                Downloader.totalDownValProperty().set(tempaAvgdp / tempDivBy);
            });

            //THIS CODE IS FOR SCHEDULER
            System.out.println("Scheduler is activated?:" + Scheduler.getInstance().getActive());
            System.out.println("downloadsScheduled??:" + downloadsScheduled);
            if (Scheduler.getInstance().getActive()) {
                System.out.println("Scheduler current flag:" + Scheduler.getInstance().getCurrentFlag());
                if (Scheduler.getInstance().getCurrentFlag()) {
                    if (!downloadsScheduled) {
                        Downloader.getDtCache().forEach((dt -> {
                            if (dt.getState() == DownloadUnit.STATE_DOWNLOADING) {//or paused as well? no fkn idea ill see
                                dt.setState(DownloadUnit.STATE_SCHEDULED);// prolly call differenteee motherfuckeee
                                // dt.schedule();
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
                            dt.setState(DownloadUnit.STATE_DOWNLOADING);//nastartuje sa? no nevim snad hej!;D
                            downloadsScheduled = false;
                        }
                    }));
                    initialIteration = false;
                }
            }

            Platform.runLater(() -> {
                //TAKE CARE OF GRAPHS and its resolutions
                FxmlMainViewController.addGraphSeriesSpeedToSeconds(tempDownSpeed);//add to seconds
                int avgMinute = 0;
                int avgHour = 0;
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

                if (iterNum % 10 == 0) {
                    int[] stateAmounts = new int[DownloadUnit.STATE_STRINGS.length];

                    for (DownloadUnit du : Downloader.getDownloads()) {
                        //remove all previous data
                        if (du.getState() == -1) {//failed
                            stateAmounts[0]++;
                        } else if (du.getState() == 0) {//paused
                            stateAmounts[1]++;
                        } else if (du.getState() == 1) {//downloading
                            stateAmounts[2]++;
                        } else if (du.getState() == 2) {//completed
                            stateAmounts[3]++;
                        } else if (du.getState() == 3) {//cancelled
                            stateAmounts[4]++;
                        } else if (du.getState() == 4) {//scheduled
                            stateAmounts[5]++;
                        }

                    }//end iterate over dunits
                    Platform.runLater(() -> {
                        //remove series here
                    
                        for (int i = 0; i < stateAmounts.length; i++) {
                            FxmlMainViewController.addToStateSeries(DownloadUnit.STATE_STRINGS[i], stateAmounts[i]);
                        }
                    });

                }

            });

            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloaderDaemonThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            iterNum++;

            //go thru all of the active downloads in dtcache and get progress, then update total downlaod progress bar
        }

    }

}
