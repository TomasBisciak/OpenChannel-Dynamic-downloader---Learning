/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.awt.TrayIcon;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.FileUtils;
import sun.plugin2.util.SystemUtil;

/**
 * Using segmented file download into multiple ra files insted of writing into filechannel When segmented downloading SINGLE FILE WILL BE BROKEN INTO MULIPLE FILES and put tugether pieces upon completion BEFORE EXTENDED Observable
 *
 * @author tomas
 */
public class DownloadTask extends DownloadUnit implements Runnable {//can return itself from executor 
//TODO must suport segmented file transfer

    /*
     public static final String FILENAME_PARTIAL = ".oc_partial.part";

     private final Object lock = new Object();

     private SimpleLongProperty id;
     private SimpleStringProperty name;//can be set//file is upon completion
     private SimpleStringProperty source;
     private String directory;
     private SimpleLongProperty size;//STORED IN BYTES/ REPRESENTED IN STRING AT TABLE

     private SimpleStringProperty eta;//based on download speed and size
     private SimpleDoubleProperty progress = new SimpleDoubleProperty(0.5);
     ;//not sure if necessery since it can be calculated from size and downloaded
     private SimpleLongProperty downloaded;//STORED IN BYTES/ REPRESENTED IN STRING AT TABLE
     private SimpleDoubleProperty downloadSpeed = new SimpleDoubleProperty(0);
     private SimpleStringProperty added;//calculates at runtime by application //stores at start in db
     private SimpleStringProperty completedOn;////calculates at runtime by application //stores at completion to db

     public static final String[] STATE_STRINGS = {"Failed", "Paused", "Downloading",
     "Completed", "Cancelled"};

     public static final int STATE_FAILED = -1;
     public static final int STATE_PAUSED = 0;
     public static final int STATE_DOWNLOADING = 1;//means downloading
     public static final int STATE_COMPLETED = 2;
     public static final int STATE_CANCELLED = 3;
     public static final int STATE_SCHEDULED = 4;

     private volatile SimpleIntegerProperty state = new SimpleIntegerProperty(STATE_DOWNLOADING);
     */
    private boolean active = false;
    private boolean shoudIKillMyself = false;//! och please no no! no!

    //public static final int CONNECTIONS_DEF = 3;
    // private String downloadDir;//not sure if used
    private static final double SMOOTHING_FACTOR = 0.002;//might lower down
    private double averageSpeed = 0;
    /*
     * based on blogpost by oracle , might be calculated later based on number of threads active and size of the download
     */
    public static final int BUFFER_SIZE = 8192;
    //private SimpleIntegerProperty numOfConnections = new SimpleIntegerProperty(CONNECTIONS_DEF);
    private boolean[] threadFinishFlags;
    private final ArrayList<DownloadThread> downloadThreadList = new ArrayList<>();

    /*//probably not needed since i can somewhow read file while its beign written to
     public static int PLAYBACK_BUFFER_SIZE=5000000;
     private byte[] playbackBuffer=new byte[PLAYBACK_BUFFER_SIZE];
     */
    private long partSize;
    private long[] partStartOffset;

    /**
     * Just helper exception to provide notification for either debug or something
     */
    public static class SizeNotDeterminedException extends Exception {

        private static final long serialVersionUID = 1L;

    }

    //executor
    public DownloadTask() {

    }

    public DownloadTask(String fileName, URL source, String directory, int numberOfConnections, boolean storeItself) throws IOException, SizeNotDeterminedException, SQLException {
        //name = new SimpleStringProperty(fileName);
        setNameProperty(new SimpleStringProperty(fileName));
        //this.source = new SimpleStringProperty(source.toString());
        setSourceProperty(new SimpleStringProperty(source.toString()));
        //this.directory=directory;
        setDirectory(directory);
        //numOfConnections.set(numberOfConnections);
        setNumberOfConnections(numberOfConnections);
        threadFinishFlags = new boolean[numberOfConnections];
        partStartOffset = new long[numberOfConnections];
        //size = new SimpleLongProperty(getSizeOf(source));

        ///TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        //setSizeProperty(new SimpleLongProperty(getSizeOf(source)));
        setSizeProperty(new SimpleLongProperty());
        ///TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT

        //downloaded = new SimpleLongProperty(readDownloadedByteSize());//how much is downlaoded? check size of the file that corresponds to this downloadUnit
        setDownloadedProperty(new SimpleLongProperty(readDownloadedByteSize()));

        //progress.bind(downloaded.divide(size).divide(100));
        //eta = new SimpleStringProperty();
        setEtaProperty(new SimpleStringProperty());

        downloadSpeedProperty().addListener((val, oldVal, newVal) -> {//might be super ineffective to compute it nonstop like this.
            //TODO check opt
            //compiler will optimize for me// or optimize later..
            averageSpeed = SMOOTHING_FACTOR * oldVal.longValue() + (1 - SMOOTHING_FACTOR) * averageSpeed;
            long totalSeconds = ((getSize() - getDownloaded()) / 1024) / (long) averageSpeed;//in kb/s
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            etaProperty().set(
                    ((hours > 0) ? hours + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + ((seconds > 0) ? seconds + "s" : "")
            );
        });
        downloadedProperty().addListener((val, oldVal, newVal) -> {
            //vypocitaj downlaod speed na zaklade kolko sa stiahlo bytov za 0.5 sekundy 
            downloadSpeedProperty().set((double) (((long) newVal - (long) oldVal) / 1024));
            //  progress.set(((double)((long)newVal/getSize()))*100);
            // System.out.println("PROGRESS VALUE SET TO :"+((double)((long)newVal/getSize()))*100);
        });

        // added = new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        setAddedProperty(new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        //completedOn = new SimpleStringProperty("Not completed");
        setCompletedOnProperty(new SimpleStringProperty("Not completed"));

        if (storeItself) {
            //todo check NOT SURE IF SAFE SINCE OBJECT IS NOT YET CREATED.
            @SuppressWarnings("LeakingThisInConstructor")
            long i = DbUtil.insertDownload(this);
            if (i < 0) {
                throw new SQLException();
            }
            //id = new SimpleLongProperty(i);//shoud be safe, im sure, almost.
            setIdProperty(new SimpleLongProperty(i));

            System.out.println("DEBUG: id for downloadTask:" + getId());
            DbUtil.printQueryResultSet(MainDataModel.getInstance().loginProfile.getUsername(),
                    MainDataModel.getInstance().loginProfile.getPassword(), "SELECT * FROM downloads WHERE id=" + getId());
        } else {
            System.out.println("T101");
            setIdProperty(new SimpleLongProperty(-1));
        }
    }

    //constructor used for loading download tasks from database with already existing id
    public DownloadTask(String fileName, URL source, long size, String directory, int numberOfConnections, long id, int state) throws IOException, SizeNotDeterminedException, SQLException {
        System.out.println("IS FXTHREAD?" + Platform.isFxApplicationThread());
        setStateProperty(new SimpleIntegerProperty(state));
        setNameProperty(new SimpleStringProperty(fileName));
        setSourceProperty(new SimpleStringProperty(source.toString()));
        setDirectory(directory);
        setNumberOfConnections(numberOfConnections);
        threadFinishFlags = new boolean[numberOfConnections];
        partStartOffset = new long[numberOfConnections];

        ///TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        //setSizeProperty(new SimpleLongProperty(getSizeOf(source)));
        setSizeProperty(new SimpleLongProperty());
        ///TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT

        /*
         if (size == getSizeOf(source) || getNumberOfConnections() == 1) {
         this.size = new SimpleLongProperty(size);
         } else {
         //notify user that size of the file at the url has changed , download wont be executed
         error();
         }
         */
        setDownloadedProperty(new SimpleLongProperty(readDownloadedByteSize()));

        //progress.bind(downloaded.divide(size).divide(100));
        setEtaProperty(new SimpleStringProperty());

        downloadSpeedProperty().addListener((val, oldVal, newVal) -> {//might be super ineffective to compute it nonstop like this.
            //TODO check opt
            //compiler will optimize for me// or optimize later..
            try {
                averageSpeed = SMOOTHING_FACTOR * oldVal.longValue() + (1 - SMOOTHING_FACTOR) * averageSpeed;
                long totalSeconds = ((getSize() - getDownloaded()) / 1024) / (long) averageSpeed;//in kb/s
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                long seconds = totalSeconds % 60;
                etaProperty().set(
                        ((hours > 0) ? hours + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + ((seconds > 0) ? seconds + "s" : "")
                );
            } catch (Exception e) {
                //division by zero
            }

        });
        downloadedProperty().addListener((val, oldVal, newVal) -> {
            //vypocitaj downlaod speed na zaklade kolko sa stiahlo bytov za 0.5 sekundy 
            downloadSpeedProperty().set((double) (((long) newVal - (long) oldVal) / 1024));
            //progress.set(((double)((long)newVal/getSize()))*100);
            // System.out.println("PROGRESS VALUE SET TO :"+((double)((long)newVal/getSize()))*100);
        });

        setAddedProperty(new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        setCompletedOnProperty(new SimpleStringProperty("Not completed"));

        //this.id = new SimpleLongProperty(id);
        setIdProperty(new SimpleLongProperty(id));

        System.out.println("DEBUG: id for downloadTask:" + getId());
        DbUtil.printQueryResultSet(MainDataModel.getInstance().loginProfile.getUsername(),
                MainDataModel.getInstance().loginProfile.getPassword(), "SELECT * FROM downloads WHERE id=" + getId());

    }

    private long getSizeOf(URL url) throws SizeNotDeterminedException, IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setInstanceFollowRedirects(true);
/*
         //TEST
         List values = conn.getHeaderFields().get("Content-Length");
         for (String header : conn.getHeaderFields().keySet()) {
         if (header != null) {
         for (String value : conn.getHeaderFields().get(header)) {
         System.out.println(header + ":" + value);
         }
         }
         }
        
         if (values != null && !values.isEmpty()) {

         // getHeaderFields() returns a Map with key=(String) header 
         // name, value = List of String values for that header field. 
         // just use the first value here.
         String sLength = (String) values.get(0);
         System.out.println("LEENGHT THRU CONTENT-LENGTH=" + sLength);
         if (sLength != null) {
         //parse the length into an integer...

         }
         } else {
         System.out.println("EMPTY wtf..");
         }
         
         //END TEST
         System.out.println("RESPONSE:" + conn.getResponseCode() + " res message:" + conn.getResponseMessage());
         // HttpURLConnection conn = Downloader.createFinalHttpURLConnection(url.toString());
         */
        long length = conn.getContentLengthLong();
        if (length <= 0) {
            error();
            System.out.println("DEBUG:Size of the file " + length);
            throw new SizeNotDeterminedException();
        } else {
            System.out.println("DEBUG:Size of the file " + length);
        }

        return length;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        try {
            //all the writing etc done here
            setSize(getSizeOf(new URL(getSource())));
        } catch (SizeNotDeterminedException | IOException ex) {
            Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (isDiskSpace(getDownloaded())) {
            createDThreads();
        } else {
            error();
            return;
        }

        //threads created or execution failed
        //TODO im not reall ysure they shoud be started right away, maybe im in paused state even before i get here?
        if (getState() == DownloadUnit.STATE_DOWNLOADING) {
            startDownloadThreads();
            System.out.println("TEST500 IS ACTIVE AT START");
            active = true;
        }

        //keep yourself alive for  further work
        //update your stats every half second
        long tempTime = 0;
        while (true) {
                //TODO CREATE CASES FOR CANCELLING TASK ETC

            ///SWITCH BASED ON STATE OF THE DOWNLAOD TASK
            if (isDownloaded()) {

                setDownloaded(readDownloadedByteSize());//get num of bytes
                downloadSpeedProperty().set(0);
                setProgress(1.0);
                completedOnProperty().set(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                //  downloadSpeed.set(((getDownloaded() - oldDownSize) * 1024) * 2);
                System.out.println("DOWNLOADED IN BYTES :" + getDownloaded());
                System.out.println("PROGRESS :" + getProgress());
                System.out.println("KB/S :" + getDownloadSpeed());
                System.out.println("ETA:" + getETA());

                System.out.println("Is downloaded returned true");
                if (getNumberOfConnections() > 1) {
                    System.out.println("FINALIZING");
                    finalizeDownload();
                }
                setState(STATE_COMPLETED);
                //TODO notify and store into db.
                if (OpenChannel_Dynamic_Downloader.getTray() != null) {//todo att another flag checks from preferences
                    OpenChannel_Dynamic_Downloader.getTray().showMessage("Download info here completed", TrayIcon.MessageType.INFO);
                }
                // DO AL LTHE FINISHING WORK HERE //////////////////////
                break;
            }

            //sleep every iteration // TODO IMPORTANT!!!!!!!!!!!!IMPORTANT!!!!!!!!!!!!IMPORTANT!!!!!!!!!!!! optimize, KILL THIS TASK IF! 60 iterations have been executed in inactive mode
            try {
                // ADD CONDITIO NTO KILL THIS THREAD FROM WITHIN HERE , BREAK THE LOOP.
                if (shoudIKillMyself) {
                    break;
                }

                //if is inactive this doesnt have to be done? no idea.
                if (getState() != DownloadUnit.STATE_PAUSED) {//TODO fix this, has to take care of multiple situations not just paused, and what if paused is saved in database and i start program in paused state?
                    setDownloaded(readDownloadedByteSize());//get num of bytes
                    setProgress(getPercentage() / 100);
                    tempTime = System.currentTimeMillis();
                    //long oldDownSize = getDownloaded();//prolly delete no poitn of having it here
                }

                Thread.sleep(1000);
                //  downloadSpeed.set(((getDownloaded() - oldDownSize) * 1024) * 2);
                //just info
                System.out.println("DOWNLOADED IN BYTES :" + getDownloaded());
                System.out.println("PROGRESS :" + getProgress());
                System.out.println("KB/S :" + getDownloadSpeed());
                System.out.println("ETA:" + getETA());
                System.out.println((System.currentTimeMillis() - tempTime) + " -THREAD ITERATION TASK");

                //TODO CREATE IMPLEMENTATION OF RESUME 
                if (!active) {
                    if (getState() == DownloadUnit.STATE_DOWNLOADING) {
                        createDThreads();
                        startDownloadThreads();
                    }
                }

                //maybe i coud do this simpleer
                /*
                 if (!active) {//in case was paused or inactive bud download task was still running and created  now iteration will have to create download threads

                 if (getState() == STATE_DOWNLOADING) {//not active and! is state set to downloading now, AND! is space on the disk  create download threads
                 if (isDiskSpace(getDownloaded())) {
                 System.out.println("--DEBUG creating download threads");
                 createdDThreads();
                 } else {
                 error();
                 return;
                 }
                 active = true;
                 }

                 } else {

                 }
                 */
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            //CAN I CONTINUE

        }
        //TODO fix ... not settings correct values for data into database/i think it does now 
        DbUtil.updateDownload(this);
        System.out.println("DOWNLOAD TASK DETECTED END OF TASK");

    }

    private boolean isDownloaded() {

        for (DownloadThread dt : getDownloadThreadList()) {
            if (!dt.isFinished()) {
                return false;
            }
        }
        return true;
    }

    private void createDThreads() {
        //calcualte optimal number of connections based on size ... maybe add as a feature later
        //partial download
        if (numOfConnectionsProperty().get() == 1) {

            partStartOffset[0] = 0;
            // downloadThreadList.add(new DownloadThread("dt-" + getId() + "-" + (1) + "-" + numOfConnections.get(), 0,
            //          getSize(), getSource(), getId(), getDirectory() + "/" + getName(), 1, getNumberOfConnections()));
            getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (1) + "-" + numOfConnectionsProperty().get(), this, 1, 0, getSize(), getDirectory() + "/" + getName()));

        } else {

            partSize = Math.round(getSize() / numOfConnectionsProperty().get());

            for (int i = 0, partStart = 0; i < numOfConnectionsProperty().get(); i++, partStart += partSize) {
                if (i == numOfConnectionsProperty().get() - 1) {//last part

                    partStartOffset[i] = partStart;
                    // downloadThreadList.add(new DownloadThread("dt-" + getId() + "-" + (i + 1) + "-" + numOfConnections.get(), partStart,
                    //          getSize(), getSource(), getId(), getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1), i + 1, getNumberOfConnections()));

                    getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (i + 1) + "-" + numOfConnectionsProperty().get(), this, i + 1, partStart, getSize(), getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)));
                    System.out.println("Part " + (i + 1) + ": startByte:" + partStart + "  endByte:" + (getSize()));
                    break;

                }

                partStartOffset[i] = partStart;
                //TODO added   -1 byte to end byte cause its inclusive? not sure if correct way to do it
                // downloadThreadList.add(new DownloadThread("dt-" + getId() + "-" + (i + 1) + "-" + numOfConnections.get(), partStart,
                //          (partStart + partSize) - 1, getSource(), getId(), getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1), i + 1, getNumberOfConnections()));
                getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (i + 1) + "-" + numOfConnectionsProperty().get(), this, i + 1, partStart, (partStart + partSize) - 1, getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)));

                System.out.println("Part " + (i + 1) + ": startByte:" + partStart + "  endByte:" + ((partStart + partSize) - 1));

            }

        }

    }

    private void restartDThreadAt(int index) throws FileNotFoundException {
        getDownloadThreadList().remove(index);
       // downloadThreadList.add(new DownloadThread("dt-" + getId() + "-" + (index + 1) + "-" + numOfConnections.get(), partStartOffset[index],
        //         partStartOffset[index] + partSize, getSource(), getId(), getDirectory() + "/" + getName() + FILENAME_PARTIAL + (index + 1), index + 1, getNumberOfConnections()));

        //CASES FOR LAST 
        //AND CASE FOR NOT SEGMENTED
        if (getNumberOfConnections() > 1) {
            //if last
            if (getNumberOfConnections() == (index + 1)) {
                getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (index + 1) + "-" + numOfConnectionsProperty().get(), this, index + 1, partStartOffset[index], getSize(), getDirectory() + "/" + getName() + FILENAME_PARTIAL + (index + 1)));
            } else {
                getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (index + 1) + "-" + numOfConnectionsProperty().get(), this, index + 1, partStartOffset[index], (partStartOffset[index] + partSize) - 1, getDirectory() + "/" + getName() + FILENAME_PARTIAL + (index + 1)));
            }
        } else {
            getDownloadThreadList().add(new DownloadThread("dt-" + getId() + "-" + (1) + "-" + numOfConnectionsProperty().get(), this, 1, 0, getSize(), getDirectory() + "/" + getName()));
        }

        getDownloadThreadList().get(index).setDaemon(true);
        getDownloadThreadList().get(index).start();
    }

    private void checkDTActivity() {
        for (DownloadThread dt : getDownloadThreadList()) {
            if (!dt.isAlive() && !dt.isFinished()) {
                try {
                    restartDThreadAt(getDownloadThreadList().indexOf(dt));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                    error();
                }
            }
        }
    }

    private boolean dThreadsStarted;

    private void startDownloadThreads() {
        if (!dThreadsStarted) {
            for (DownloadThread dt : getDownloadThreadList()) {
                System.out.println("Starting thread id:" + dt.getId());
                dt.setDaemon(true);
                dt.start();
            }
            dThreadsStarted = true;
        }

    }

    private int readNumOfParts() {
        for (int i = 0; true; i++) {
            if (!Files.exists(Paths.get(getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)))) {
                return i;
            }
        }
    }

    private long readDownloadOffset(int part) {
        try {
            return Files.size(Paths.get(getDirectory() + "/" + getName() + FILENAME_PARTIAL + part));
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;//start again 0 offset
        }
    }

    //todo test
    private boolean finalizeDownload() {//partial download

        try {
            Files.createFile(Paths.get(getDirectory() + "/" + getName()));
        } catch (IOException ex) {
            Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("DEBUG:Creation of the file failed");
            return false;
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getDirectory() + "/" + getName()))) {

            for (int i = 0; i < getNumberOfConnections(); i++) {

                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)));) {
                    byte[] buff = new byte[8192];
                    int len;
                    while ((len = bis.read(buff, 0, buff.length)) != -1) {
                        bos.write(buff, 0, len);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("DEBUG:Cant finalize download, corrupted downlaod part :" + i + 1);
                    return false;
                } finally {
                    try {
                        //delete partial file after reading
                        Files.delete(Paths.get(getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)));
                    } catch (IOException ex) {
                        Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("DEBUG:Finalization failed");
        }
        System.out.println("FINALIZATION DONE");
        return true;
    }

    public String generateMD5checksum() {//put checksupms into the folder dedicated to it , after each download/if set in preferences
        //todo implement
        return "";
    }

    /**
     *
     * @param offset
     * @return
     */
    private boolean isDiskSpace(long offsetFileByte) {//accoutns for already downlaoded portion of the file as well takes int oaccount
        try {
            return (FileUtils.getDiskSpace(Paths.get(getDirectory().substring(0, 3))) - (getSize() - offsetFileByte)) > (1024 * 1024 * 1);//leftover space 1mb
        } catch (IOException ex) {
            return false;
        }
    }

//Download task actions/ controlled from downlaoder as well
    //set state calls state changed method within
    public void resume() {
        if (getState() == STATE_PAUSED || getState() == STATE_SCHEDULED) {
            System.out.println("Resuming, starting downlaod");
            active = true;
            setState(STATE_DOWNLOADING);
            //create download threads
        }

    }

    public void pause() {//pause thread
        if (getState() == STATE_DOWNLOADING || getState() == STATE_SCHEDULED) {
            getDownloadThreadList().forEach((t) -> {
                t.killed = true;
            });
            active = false;
            //TODO not sure if shodu i kill myself or not here . :D.
            setState(STATE_PAUSED);
            //doesnt have to get  pause for some reason probably some thread updates faster then this block  executes
            downloadSpeedProperty().set(0);
            etaProperty().set("");
        }
    }

    public final void schedule() {
        if (getState() == STATE_DOWNLOADING) {
            stopDownloadThreads();
            setState(STATE_SCHEDULED);
        }
    }

    public void stopDownloadThreads() {// wihtout setting a state
        getDownloadThreadList().forEach((t) -> {
            t.killed = true;
        });
        active = false;
        downloadSpeedProperty().set(0);
        etaProperty().set("");
    }

    public void cancel() {//remove and notify, also stop thread
        if (getState() == STATE_DOWNLOADING || getState() == STATE_PAUSED || getState() == STATE_SCHEDULED) {
            getDownloadThreadList().forEach((t) -> {
                t.killed = true;
            });
        }
        shoudIKillMyself = true;
        active = false;
        setState(STATE_CANCELLED);
        downloadSpeedProperty().set(0);
        etaProperty().set("");
    }

    public final void error() {//end thread, notify user
        setState(STATE_FAILED);
    }

    /**
     * @return the downloadThreadList
     */
    public ArrayList<DownloadThread> getDownloadThreadList() {
        return downloadThreadList;
    }

}
