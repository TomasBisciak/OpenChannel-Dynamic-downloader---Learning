/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.DbUtil;

/**
 * Using this mainly for OCTABLE adn visualisation of tasks that are done,cancelled,failed, once they wanna be redownlaoded they gonna spawn new downlaodtask from this PRINT ,/data / ... and this gonna be deleted Just in case there will be other implementations of downlaod unit , https secured. .etc .. no idea... INCOGNITO DOWNLOAD MAYBE/.. ?... no idea
 *
 * @author Kofola
 */
public class DownloadUnit {

    public static final String FILENAME_PARTIAL = ".oc_partial.part";

    private final Object lock = new Object();

    private SimpleLongProperty id;
    private SimpleStringProperty name;//can be set//file is upon completion
    private SimpleStringProperty source;
    private String directory;
    private SimpleLongProperty size;//STORED IN BYTES/ REPRESENTED IN STRING AT TABLE

    private SimpleStringProperty eta;//based on download speed and size
    private SimpleDoubleProperty progress = new SimpleDoubleProperty(0.0);
    ;//not sure if necessery since it can be calculated from size and downloaded
    private SimpleLongProperty downloaded;//STORED IN BYTES/ REPRESENTED IN STRING AT TABLE
    private SimpleDoubleProperty downloadSpeed = new SimpleDoubleProperty(0);
    private SimpleStringProperty added;//calculates at runtime by application //stores at start in db
    private SimpleStringProperty completedOn;////calculates at runtime by application //stores at completion to db

    public static final String[] STATE_STRINGS = {"Failed", "Paused", "Downloading",
        "Completed", "Cancelled", "Scheduled"};

    public static final int STATE_FAILED = -1;
    public static final int STATE_PAUSED = 0;
    public static final int STATE_DOWNLOADING = 1;//means downloading
    public static final int STATE_COMPLETED = 2;
    public static final int STATE_CANCELLED = 3;
    public static final int STATE_SCHEDULED = 4;

    private volatile SimpleIntegerProperty state = new SimpleIntegerProperty(STATE_DOWNLOADING);

    public static final int CONNECTIONS_DEF = 3;// DONT USE THIS FOR ACTUAL CONECTIONS, USE UserProfile.getNumOfConThreads() ...! not this!

    private String downloadDir;//not sure if used
    private SimpleIntegerProperty numOfConnections = new SimpleIntegerProperty(MainDataModel.getInstance().loginProfile.getNumOfConnectionsPerDownload());

    public DownloadUnit() {

    }

    //LOAD WITH ALL DATA / LOADING FROM DATABASE FOR INSTANCE //shoud have fuckn used factory pattern
    public DownloadUnit(long id, int state, int connections, String name, long size, String source, String added, String completedOn, String directory) {
        setIdProperty(new SimpleLongProperty(id));
        setStateProperty(new SimpleIntegerProperty(state));
        setNumberOfConnections(connections);
        setNameProperty(new SimpleStringProperty(name));
        setSizeProperty(new SimpleLongProperty(size));//WROOOOOOOONG
        setSourceProperty(new SimpleStringProperty(source));
        setAddedProperty(new SimpleStringProperty(added));
        setCompletedOnProperty(new SimpleStringProperty(completedOn));
        setDirectory(directory);
        setEtaProperty(new SimpleStringProperty());
        setDownloadSpeedProperty(new SimpleDoubleProperty());
        setDownloadedProperty(new SimpleLongProperty(readDownloadedByteSize()));
        setProgress(getPercentage() / 100);

    }

    // TODO PREROBIT CONSTRUCTORY KEDZE ZLE LOADUJU DATA KED BEREM Z DB .. V DB SU DOBRE DANE ;D
    //JUST FOR TESTING THIS IS NOT A CORRECT CONTRUCTOR IMPLEMENTATION
    public DownloadUnit(String fileName, URL source, long size, String directory, int numberOfConnections, long id) {
        /*setNameProperty(new SimpleStringProperty(fileName));
         setSourceProperty(new SimpleStringProperty(source.toString()));
         setDirectory(directory);
         setSizeProperty(new SimpleLongProperty(size));
         setEtaProperty(new SimpleStringProperty("test"));
         setNumberOfConnections(numberOfConnections);
         setDownloadedProperty(new SimpleLongProperty(0));
         setDownloadSpeedProperty(new SimpleDoubleProperty(0.0));
         setAddedProperty(new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
         setCompletedOnProperty(new SimpleStringProperty("Not completed"));
         setIdProperty(new SimpleLongProperty(id));*/

        setNameProperty(new SimpleStringProperty(fileName));
        setSourceProperty(new SimpleStringProperty(source.toString()));
        setDirectory(directory);
        setNumberOfConnections(numberOfConnections);
        //threadFinishFlags = new boolean[numberOfConnections];
        //partStartOffset = new long[numberOfConnections];
        setSizeProperty(new SimpleLongProperty());
        /*
         if (size == getSizeOf(source) || getNumberOfConnections() == 1) {
         this.size = new SimpleLongProperty(size);
         } else {
         //notify user that size of the file at the url has changed , download wont be executed
         error();
         }
         */
        setDownloadedProperty(new SimpleLongProperty(2));

        //progress.bind(downloaded.divide(size).divide(100));
        setEtaProperty(new SimpleStringProperty());

        downloadSpeedProperty().addListener((val, oldVal, newVal) -> {//might be super ineffective to compute it nonstop like this.
            /*
             //TODO check opt
             //compiler will optimize for me// or optimize later..
             averageSpeed = SMOOTHING_FACTOR * oldVal.longValue() + (1 - SMOOTHING_FACTOR) * averageSpeed;
             long totalSeconds = ((getSize() - getDownloaded()) / 1024) / (long) averageSpeed;//in kb/s
             long hours = totalSeconds / 3600;
             long minutes = (totalSeconds % 3600) / 60;
             long seconds = totalSeconds % 60;
             etaProperty().set(
             ((hours > 0) ? hours + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + ((seconds > 0) ? seconds + "s" : "")
             );*/
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

    //JUST FOR TESTING THIS IS NOT A CORRECT CONTRUCTOR IMPLEMENTATION
    public DownloadUnit(int state, String fileName, URL source, long size, String directory, int numberOfConnections, long id) {
        /*setNameProperty(new SimpleStringProperty(fileName));
         setSourceProperty(new SimpleStringProperty(source.toString()));
         setDirectory(directory);
         setSizeProperty(new SimpleLongProperty(size));
         setEtaProperty(new SimpleStringProperty("test"));
         setNumberOfConnections(numberOfConnections);
         setDownloadedProperty(new SimpleLongProperty(0));
         setDownloadSpeedProperty(new SimpleDoubleProperty(0.0));
         setAddedProperty(new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
         setCompletedOnProperty(new SimpleStringProperty("Not completed"));
         setIdProperty(new SimpleLongProperty(id));*/
        setState(state);

        System.out.println("IS FXTHREAD?" + Platform.isFxApplicationThread());
        setNameProperty(new SimpleStringProperty(fileName));
        setSourceProperty(new SimpleStringProperty(source.toString()));
        setDirectory(directory);
        setNumberOfConnections(numberOfConnections);
        //threadFinishFlags = new boolean[numberOfConnections];
        //partStartOffset = new long[numberOfConnections];
        setSizeProperty(new SimpleLongProperty());
        /*
         if (size == getSizeOf(source) || getNumberOfConnections() == 1) {
         this.size = new SimpleLongProperty(size);
         } else {
         //notify user that size of the file at the url has changed , download wont be executed
         error();
         }
         */
        setDownloadedProperty(new SimpleLongProperty(2));

        //progress.bind(downloaded.divide(size).divide(100));
        setEtaProperty(new SimpleStringProperty());

        downloadSpeedProperty().addListener((val, oldVal, newVal) -> {//might be super ineffective to compute it nonstop like this.
            /*
             //TODO check opt
             //compiler will optimize for me// or optimize later..
             averageSpeed = SMOOTHING_FACTOR * oldVal.longValue() + (1 - SMOOTHING_FACTOR) * averageSpeed;
             long totalSeconds = ((getSize() - getDownloaded()) / 1024) / (long) averageSpeed;//in kb/s
             long hours = totalSeconds / 3600;
             long minutes = (totalSeconds % 3600) / 60;
             long seconds = totalSeconds % 60;
             etaProperty().set(
             ((hours > 0) ? hours + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + ((seconds > 0) ? seconds + "s" : "")
             );*/
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

    //MAKE SURE YOU RUN THIS METHOD OFF ANY OTHER CRUCIAL THREAD ! BEST WAY IS TO CREATE SEPARATED THREAD JSUT FOR THIS//TODO  this is obsolete , i already imported creative ocmmons lib to generate checksum, might use this one for other versions
    public final String generateChecksum(String algorithmName) {//for supported names check //http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html
        if (getState() == STATE_COMPLETED) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName);
                byte[] buffer = new byte[1024];
                try (InputStream is = Files.newInputStream(Paths.get(getDirectory() + "/" + getName()));
                        DigestInputStream dis = new DigestInputStream(is, md)) {
                    int numRead;
                    do {
                        numRead = is.read(buffer);
                        if (numRead > 0) {
                            md.update(buffer, 0, numRead);
                        }
                    } while (numRead != -1);
                } catch (IOException ex) {
                    Logger.getLogger(DownloadUnit.class.getName()).log(Level.SEVERE, null, ex);
                }
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digest.length; ++i) {
                    sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
                }
                return sb.toString();

            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(DownloadUnit.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Failed to generate");
            return null;
        } else {
            return null;
        }

    }

    //go thru all the files,//MOVED FROM DOWNLAOD TASK
    public final long readDownloadedByteSize() {

        //test
        long bytes = 0;

        //condition if downlaod has been finalized already
        if (getNumberOfConnections() == 1 || getState() == DownloadUnit.STATE_COMPLETED) {

            try {
                bytes += Files.size(Paths.get(getDirectory() + "/" + getName()));
            } catch (Exception ex) {
                System.out.println("PROBLEM AT readDownloadedByteSize ID IS :" + getId());
                System.out.println("--DEBUG DIRECTORY" + getDirectory());

            }

        } else {
            for (int i = 0; i < getNumberOfConnections(); i++) {
                try {
                    bytes += Files.size(Paths.get(getDirectory() + "/" + getName() + FILENAME_PARTIAL + (i + 1)));
                } catch (Exception ex) {
                    System.out.println("PROBLEM AT readDownloadedByteSize ID IS :" + getId());
                    System.out.println("--DEBUG DIRECTORY" + getDirectory());
                }
            }
        }

        return bytes;

    }

    public final void setDownloaded(long l) {
        synchronized (lock) {
            downloaded.set(l);
        }
    }

    public final void setNumberOfConnections(int numberOfConnections) {

        if (numberOfConnections >= 1) {
            this.numOfConnections.set(numberOfConnections);
        } else {
            //todo something here
            System.out.println("Attempt to set invalid number of connections");
        }

    }

    public final int getNumberOfConnections() {
        return numOfConnections.get();
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public double getProgress() {
        return progress.get();
    }

    public final void setProgress(double progress) {

        if (progress <= 1 && progress >= 0) {
            this.progress.set(progress);
        }

    }

    public double getDownloadSpeed() {
        return downloadSpeed.get();
    }

    public String getETA() {
        return eta.get();
    }

    public String getAdded() {
        return added.get();
    }

    public String getCompletedOn() {
        return completedOn.get();
    }

    public SimpleIntegerProperty getStateProperty() {
        return state;
    }

    public int getState() {
        return state.get();
    }

    public final void setState(int state) {
        synchronized (lock) {
            if (state >= -1 && state <= 4) {
                this.state.set(state);

            } else {
                this.state.set(STATE_DOWNLOADING);
            }
            stateChanged();
        }

    }

   
    @Override
    public String toString() {
        return "id \n" + getId() + "State:" + getState() + "\n Name:" + getName() + "\n Source:" + getSource() + "\n Directory:" + getDirectory()
                + "\n Size:" + getSize() + "\n Progress:" + getProgress() + "\n ETA:" + getETA() + "\n Downloaded:" + getDownloaded()
                + "\n Download Speed:" + getDownloadSpeed() + "\n Added:" + getAdded() + "\n CompletedOn:" + getCompletedOn();

    }

    public final long getId() {
        return (id == null) ? -1 : id.get();
    }

    public final String getName() {
        return name.get();
    }

    public final long getSize() {
        return size.get();
    }

    public String getSource() {
        return source.get();
    }

    public String getDirectory() {
        return directory;
    }

    public final void setDirectory(String directory) {
        synchronized (lock) {
            this.directory = directory;
        }

    }

    public final long getDownloaded() {
        return downloaded.get();
    }

    public void stateChanged() {
        System.out.print("STATE HAS BEEN CHANGED CURRENT STATE " + getState());
        System.out.println(" STATE STRING : " + STATE_STRINGS[getState() + 1]);
        //upadte state of dt in DB
        DbUtil.updateDownload(this);
        //setChanged();
        //notifyObservers();
    }

    //for octableview
    //getters for  property
    /*
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
     */
    public SimpleLongProperty idProperty() {
        return id;
    }

    public final void setIdProperty(SimpleLongProperty idp) {
        id = idp;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public final void setNameProperty(SimpleStringProperty np) {
        name = np;
    }

    public SimpleStringProperty sourceProperty() {
        return source;
    }

    public final void setSourceProperty(SimpleStringProperty sp) {
        source = sp;
    }

    public SimpleLongProperty sizeProperty() {
        return size;
    }

    public final void setSizeProperty(SimpleLongProperty sp) {
        size = sp;
    }

    public final void setSize(long l) {
        size.set(l);
    }

    public SimpleStringProperty etaProperty() {
        return eta;
    }

    public final void setEtaProperty(SimpleStringProperty etap) {
        eta = etap;
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    public void setProgressProperty(SimpleDoubleProperty pp) {
        progress = pp;
    }

    public final SimpleLongProperty downloadedProperty() {
        return downloaded;
    }

    public final void setDownloadedProperty(SimpleLongProperty dp) {
        downloaded = dp;
    }

    public final SimpleDoubleProperty downloadSpeedProperty() {
        return downloadSpeed;
    }

    public final void setDownloadSpeedProperty(SimpleDoubleProperty dsp) {
        downloadSpeed = dsp;
    }

    public SimpleStringProperty addedProperty() {
        return added;
    }

    public final void setAddedProperty(SimpleStringProperty ap) {
        added = ap;
    }

    public SimpleStringProperty completedOnProperty() {
        return completedOn;
    }

    public final void setCompletedOnProperty(SimpleStringProperty cop) {
        completedOn = cop;
    }

    //private volatile SimpleIntegerProperty state = new SimpleIntegerProperty(STATE_DOWNLOADING);
    public SimpleIntegerProperty stateProperty() {
        return state;
    }

    public final void setStateProperty(SimpleIntegerProperty sp) {
        state = sp;
    }

    //private SimpleIntegerProperty numOfConnections = new SimpleIntegerProperty(CONNECTIONS_DEF);
    public SimpleIntegerProperty numOfConnectionsProperty() {
        return numOfConnections;
    }

    public void setNumOfConnectionsProperty(SimpleIntegerProperty nocp) {
        numOfConnections = nocp;
    }

    public final double getPercentage() {
        try {
           
            return (getDownloaded() * 100 / getSize());
        } catch (Exception e) {
            System.out.println("Problem at get percentage");
        }
        return 0;
    }

    public void cancel() {
        System.out.println("Cancelling downlaod unit from DownloadUnit Class");
        if (getState() == STATE_DOWNLOADING || getState() == STATE_PAUSED || getState() == STATE_SCHEDULED) {
            setState(STATE_CANCELLED);
        }

    }

}
