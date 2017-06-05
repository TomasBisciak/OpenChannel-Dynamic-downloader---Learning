/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.MiscUtils;

/**
 * Aint no way to use proxy downloads running in parallel at this moment , no way to set multiple parallel authentificators , authentificated download woud have to be queued , proxy downloads without authentificator are possible.
 *
 * @author tomas
 */
public class Downloader {
    
    private static ExecutorService executor = Executors.newCachedThreadPool();//multithreaded all downlaod active s
    private static LinkedList<DownloadTask> queuedExec = new LinkedList<>();
    
    private static DownloadValidator downValid = new DownloadValidator(new String[]{"http", "https", "ftp"}, true);

    // private static HashMap<DownloadUnit, Long> dSizeMap = new HashMap<>();//
    /**
     * Executor type: Parallel multiple downloads at the same time, all equal priority Queued one at the time Queued Parralel prioritized parallel donload with size limit or automaticly set
     */
    public static enum EXEC_TYPE {
        
        EXEC_PARALLEL,
        EXEC_QUEUED,
        EXEC_PARALLEL_QUEUED
    }
    private static EXEC_TYPE executorType = EXEC_TYPE.EXEC_PARALLEL;
    
    
    
    
    
    
    
    
    public static DownloaderDaemonThread ddt = new DownloaderDaemonThread();
    
    public static void init() {
        /*
         downloadedProperty().addListener((val, oldVal, newVal) -> {
         //vypocitaj downlaod speed na zaklade kolko sa stiahlo bytov za 0.5 sekundy 
         downloadSpeedProperty().set((double) (((long) newVal - (long) oldVal) / 1024));
         //  progress.set(((double)((long)newVal/getSize()))*100);
         // System.out.println("PROGRESS VALUE SET TO :"+((double)((long)newVal/getSize()))*100);
         });
         */
        
        loadDownloads();
        ddt.start();
        
    }

    //private static int dataLocSpaceLock[FileUtils.getNumOfDisk()];
    public static volatile boolean queued;//not sure if used

    //TODO   IMPORTANT!!
    private static final List<DownloadTask> dtCache = Collections.synchronizedList(new ArrayList<DownloadTask>());//cached DownloadTasks objects//active/paused etc actual tasks // //must be thread safe
    private static ObservableList<DownloadUnit> downloads = FXCollections.observableArrayList();//cached DownloadUnits objects//ones that i watch in ocTableView//this is thread safe
    //downloads moze vo view ukazovat aj dtasky aj dunity takze by som mal oboje situacie nad operaciami vo view osetrit.

    public static ObservableList<DownloadUnit> getDownloads() {
        return downloads;
    }

    // public static enum SupportedLinks{
    //}
    static {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection.setFollowRedirects(true);
        long maxBytes = Runtime.getRuntime().maxMemory();
        System.out.println("DEBUG downloader - java : Max memory: " + maxBytes / 1024 / 1024 + "M");
        init();
        
    }
    
    private Downloader() {
        
    }
    
    public static List<DownloadTask> getDtCache() {
        return dtCache;
    }
    /*
     public static List<DownloadTask> getDownloads() {
     return downloads;
     }
     */
    
    public static final void initCache() {//

    }
    
    public static void setExecutorType(EXEC_TYPE execution_type) {
        //in case already downlaoding take care of switch

        switch (execution_type) {
            case EXEC_PARALLEL: {
                
                break;
            }
            case EXEC_QUEUED: {
                
                break;
            }
            case EXEC_PARALLEL_QUEUED: {
                
                break;
            }
            default: {
                setExecutorType(EXEC_TYPE.EXEC_PARALLEL);
            }
        }
        
        executorType = execution_type;
        
    }
    
    private static final StringBuilder localStringBuilder = new StringBuilder();
    
    
    //MAYBE IMPLEMENT LATER BUDN OW , IDS are changing too often.
    private static final HashSet<Long> uniqueDUIDs = new HashSet<>();
    private static void addUnigueDUID(long uid) {
        synchronized (uniqueDUIDs) {
            uniqueDUIDs.add(uid);
        }
    }

    //used for addign new downlaods from ui
    public static boolean downloadFile(URL url, String fileName, String downloadDir, int numberOfConnections, boolean notification, boolean storeItself) {
       
        System.out.println("Downloading file :" + fileName + " --- URL :" + url);
        localStringBuilder.append(fileName);
        if (localStringBuilder.toString() == null || localStringBuilder.toString().equals("")) {
            localStringBuilder.delete(0, localStringBuilder.length());
            localStringBuilder.append(generateFullName(url.toString()));
            // fileName=generateFullName(url.toString());
        }
        try {
            
            if (!(Files.list(Paths.get(downloadDir)).filter((f) -> f.getFileName().toString().equals(localStringBuilder.toString())).count() > 0)) {//disallow same names like already contained in current downlaod folder
                System.out.println("ok you can do it.");
                //TEST
                DownloadTask dt = new DownloadTask(localStringBuilder.toString(), url, downloadDir, numberOfConnections, storeItself);
                Platform.runLater(() -> {//in case its invoked from other then jfxat
                    downloads.add(dt);
                });
                dtCache.add(dt);
                // FxmlDownloadsViewController.addItemToOCTable(dt);

                new Thread(dt).start();
                
                if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
                    OpenChannel_Dynamic_Downloader.getTray().showMessage("Download info here started", TrayIcon.MessageType.INFO);
                }
                localStringBuilder.delete(0, localStringBuilder.length());
                
                return true;
            } else {
                System.out.println("they are same");
                if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
                    OpenChannel_Dynamic_Downloader.getTray().showMessage("Folder already contains file with same name,please choose different name for your file.", TrayIcon.MessageType.WARNING);
                }
            }
        } catch (IOException | DownloadTask.SizeNotDeterminedException | SQLException ex) {
            ex.printStackTrace();
            if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
                OpenChannel_Dynamic_Downloader.getTray().showMessage("Downlaod info here failed", TrayIcon.MessageType.ERROR);
            }
        }
        // executor.submit(new Download());
        localStringBuilder.delete(0, localStringBuilder.length());
        return false;
    }
    /*
     //!!!!!!!!!!!!!!!!!!!!!!!!STOREITSELF NOT USED , make sure you repair this method before use
     public static boolean downloadFile(DownloadUnit du, boolean notification, boolean storeItself) {
     System.out.println("Downloading file :" + du.getName() + " --- URL :" + du.getSource());
     try {

     if (!(Files.list(Paths.get(du.getDirectory())).filter((f) -> f.getFileName().toString().equals(du.getName())).count() > 0)) {//disallow same names like already contained in current downlaod folder
     System.out.println("ok you can do it.");
     //TODO i might have fucked up something here
     DownloadTask dt = new DownloadTask(du.getName(), new URL(du.getSource()),du.getSize() ,du.getDirectory(), du.getNumberOfConnections(), du.getId());
     downloads.add(dt);
     dtCache.add(dt);
     // FxmlDownloadsViewController.addItemToOCTable(dt);

     new Thread(dt).start();

     if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
     OpenChannel_Dynamic_Downloader.getTray().showMessage("Download info here started", TrayIcon.MessageType.INFO);
     }
     } else {
     System.out.println("they are same");
     if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
     OpenChannel_Dynamic_Downloader.getTray().showMessage("Folder already contains file with same name,please choose different name for your file.", TrayIcon.MessageType.WARNING);
     }
     }
     } catch (IOException | DownloadTask.SizeNotDeterminedException | SQLException ex) {
     ex.printStackTrace();
     if (notification && OpenChannel_Dynamic_Downloader.getTray() != null) {
     OpenChannel_Dynamic_Downloader.getTray().showMessage("Downlaod info here failed", TrayIcon.MessageType.ERROR);
     }
     }
     // executor.submit(new Download());
     return false;
     }
     */

    //might use preferences variable cause this poses security risk for user  when going from http to https for instance
    //TODO fix secur pref here
    public static HttpURLConnection createFinalHttpURLConnection(String url) {
        URL base, next;
        HttpURLConnection conn;
        String location = url;
        
        while (true) {
            try {
                //resourceUrl = new URL(url);
                conn = (HttpURLConnection) new URL(location).openConnection();
                
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                //conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

                switch (conn.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        location = conn.getHeaderField("Location");
                        base = new URL(url);
                        next = new URL(base, location);  // Deal with relative URLs
                        url = next.toExternalForm();
                        System.out.println("REDIRECT:" + url);
                        continue;
                }
                break;
            } catch (MalformedURLException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("PROBLEM AT createFinalHttpURLConnection URL MalformedURLException");
            } catch (IOException ex) {
                System.out.println("PROBLEM AT createFinalHttpURLConnection URL ioexception");
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Final url:" + url);
        return conn;
    }
    
    public static final void removeDownload(DownloadUnit du) {
        //remove from database
        //stop the threads
        //FxmlDownloadsViewController.removeItemFromOCTable(dt);
        DbUtil.removeDownload(du);
        downloads.remove(du);
        if (du instanceof DownloadTask) {//in case its active stop threads 
            ((DownloadTask) du).cancel();
        }
        
    }
    
    public static void registerDownload() {
        
    }
    
    public static final void removeDownloads(List<DownloadUnit> list) {// problem is that its still active.
        //remove from database
        //stop the threads
        DbUtil.removeDownloads(list);
        list.forEach((d) -> {
            System.out.println("debug remove list contains:" + d.getId());
            if (d instanceof DownloadTask) {//in case its active stop threads 
                ((DownloadTask) d).cancel();
            }
        });
        
        Platform.runLater(() -> {
            downloads.removeAll(list);
        });
        
        dtCache.removeAll(list);
        //FxmlDownloadsViewController.removeItemsFromOCTable(list);

    }
    
    public static final void removeDownload(Long id) {
        //remove from database
        //stop the threads
        DbUtil.removeDownload(id);
        downloads.removeIf((d) -> d.getId() == id);
        dtCache.removeIf((d) -> d.getId() == id);
        //get downlaod first ffs
        dtCache.stream().filter((dt) -> dt.getId() == id).forEach((DownloadTask dt) -> {
            ((DownloadTask) dt).cancel();
        });

        //FxmlDownloadsViewController.removeItemFromOCTable(id);
    }

    /**
     * Loads data (downlaod units) from database into downloads observable list.
     *
     * @return number of stored items in database items
     */
    public static final int loadDownloads() {
        downloads = FXCollections.observableArrayList(DbUtil.loadDownloads());
        System.out.println("Loaded items:" + downloads.size());
        
        List<DownloadUnit> activeDownloads = new ArrayList<>();
        //does not take INTO ACCOUNT SCHEDULED DOWNLOADS!!!!!!!!!!!!! no idea how they gonna work bud its gonna fix 
        //// IMPORTANT
        //// IMPORTANT!!!!!!
        //            DOWNLOADING WORKS ONLY FOR DOWNLOAD TASKS , I CAN CREATE DOWNLAOD TASKS FROM UNITS BUD WHEN I HAVE UNITS IN TABLE I CANT DOWNLAOD THEM DIRECTLY. 
        //// IMPORTANT 
        //// IMPORTANT 
        // downloads.stream().filter((du) -> (du.getState() == DownloadUnit.STATE_DOWNLOADING // woud be probably best to have here.
        //this has been taken care of in resume.
        downloads.stream().filter((du) -> (du.getState() == DownloadUnit.STATE_DOWNLOADING)).forEach((du) -> {//TODO review since this might be littlebit unoptimized.And what about sheduled downloads?
            activeDownloads.add(du);
        });
        //downloads.removeAll(activeDownloads);
        for (DownloadUnit du : activeDownloads) {
            try {
                //TODO using wrong constructor here has to be fixed, must be the one with STATE included so we know if download hsoud start itself or wait.or dont add PAUSED TO DTCACHE as active and create DU from it.
                //ak je downloadtask/
                DownloadTask dt = new DownloadTask(du.getName(), new URL(du.getSource()), du.getSize(), du.getDirectory(), du.getNumberOfConnections(), du.getId(), du.getState());
                downloads.remove(du);
                downloads.add(dt);
                dtCache.add(dt);
                new Thread(dt).start();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DownloadTask.SizeNotDeterminedException | SQLException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*
         downloads.stream().forEach((DownloadUnit du) -> {
         if (du.getState() == DownloadUnit.STATE_DOWNLOADING) {
         try {
         DownloadTask dt = new DownloadTask(du.getName(), new URL(du.getSource()), du.getDirectory(), du.getNumberOfConnections(), false);
         downloads.remove(du);
         dtCache.add(dt);
         downloads.add(du);
         System.out.println("test99");
         new Thread(dt).start();
         } catch (MalformedURLException ex) {
         Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
         Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DownloadTask.SizeNotDeterminedException | SQLException ex) {
         Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
         }
         }

         });
         */
        System.out.println("HOW MANY active downloading:" + dtCache.size());
        return downloads.size();
    }
    
    private void download(DownloadTask download) {
        
    }
    
    public static final void createDownload() {//uses validator

    }
    
    public static final boolean validateStringDownloadUrl(String url) {//has to be regex ..
        
        return true;
    }
    
    public static final String generateFullName(String url) {
        //only if valid url
        //regex check
        //different cases for supported websites
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")) + "_" + MainDataModel.getInstance().getLatestId()
                + "_oc" + url.substring(url.lastIndexOf("."));//"_oc_"+MainDataModel.getInstance().getLatestId();

    }
    
    public static final String getPostFix(String url, long id) {
        try {
            return "_" + id
                    + "_oc" + url.substring(url.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static final String generateFileName(String url) {
        //only if valid url
        //regex check
        //different cases for supported websites
        //  return URLConnection.guessContentTypeFromName(url);
        return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")) + "_" + MainDataModel.getInstance().getLatestId()
                + "_oc";//+url.substring(url.lastIndexOf("."));

    }
    
}
