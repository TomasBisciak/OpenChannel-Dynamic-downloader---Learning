/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tomas
 */
public class DownloadThread extends Thread {

    private boolean isFinished;

    private long startByte;
    private long endByte;
    private int partNum;
    private String filePath;

    private DownloadTask dtask;

    //load from preferences
    public static int readTimeout = 10000;
    public static int connectionTimeout=10000;

   // public volatile boolean active;
    public volatile boolean killed;

    private int errorCode = 0x0;
    int ERR_IO = 0x1;    //0001
    int TWO = 0x2;    //0010
    int THREE = 0x4;  //0100
    int FOUR = 0x8;   //1000

    public DownloadThread() {

    }

    //partial download
    //START BYTE SPECIFIED FOR HTTP PARTIAL OFFSET!!!!!!! NOT FOR THE FILE ITSELF
    //number of parts added cause of extra bytes
    //change to reference
     /*public DownloadThread(String threadName, long startByte, long endByte, String source, long taskId, String filePath, int partNum, int numberOfParts) {

     this.setName(threadName);
     this.startByte = startByte;
     this.endByte = endByte;
     this.taskId = taskId;
     this.partNum = partNum;
     this.source = source;
     this.filePath = filePath;
     this.numberOfParts = numberOfParts;

     }
     */
    public DownloadThread(String threadName, DownloadTask dtask, int partNum, long startByte, long endByte, String filePath) {

        this.setName(threadName);
        this.dtask = dtask;
        this.filePath = filePath;
        this.partNum = partNum;
        this.startByte = startByte;
        this.endByte = endByte;
        this.filePath = filePath;
    }

    @Override
    public void run() {

      try{

            if (isDownloaded()) {
                killed = true;
                return;
            }

            HttpURLConnection conn = (HttpURLConnection)new URL(dtask.getSource()).openConnection();
            //will be set in downloader.handleRedirect
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectionTimeout);
           // conn.setInstanceFollowRedirects(true);
           // HttpURLConnection conn = Downloader.createFinalHttpURLConnection(dtask.getSource());
         

            //continue in downlaod process//or go form where yo usupposed to downlaod from anyway
            conn.setRequestProperty("Range",
                    "bytes=" + (startByte + getSizeOfFileSafe()) + "-" + endByte);//size of already downloaded

            System.out.println("Content lenght validity:" + conn.getContentLengthLong());
            //conn.getContentLengthLong()///...

            System.out.println("Range on http:" + (startByte + getSizeOfFileSafe()) + "-" + endByte);

            //ok
            System.out.println("Gonna download");
            //active = true;
            try (
                    BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd")) {

                int readed = -2;
                byte[] buffer = new byte[DownloadTask.BUFFER_SIZE];//might be calculated form download
                //offset
                //removed seek
                raf.seek(getSizeOfFileSafe());
                //System.out.println("seeking:"+startByte + getSizeOfFileSafe()+" byte");
                //pokial thread ma byt aktivny
               // System.out.println("BEFORE WHILE");
                while (true) {

                    //citaj z url
                    while (/*active &&*/ !killed && ((readed = in.read(buffer, 0, DownloadTask.BUFFER_SIZE)) != -1)) {
                      // System.out.println("WRITING TO FILE");
                        //zapisuj do suboru
                        raf.write(buffer, 0, readed);
                        /*
                         while ((count = in.read(buffer, 0, buffer.length)) != -1) {
                         file.write(buffer, 0, count);
                         readed += count;
                         du.setDownloaded(readed);
                         }
                         */
                        //removed startbyte+size at downloaded
                        //System.out.println("Thread:" + this.getName() + "File " + filePath + " Downloaded:" + (Files.size(Paths.get(filePath))) + " bytes ,From:" + (endByte - startByte) + " bytes");
                        //   if ((startByte + Files.size(Paths.get(filePath))) == endByte) {
                        if (partNum == dtask.getNumberOfConnections()) {//check added cause of extra bytes , last part doesnt have extra bytes

                            if (Files.size(Paths.get(filePath)) == endByte - startByte) {
                                System.out.println("Thread:" + this.getName() + " DEBUG: download of part " + partNum + " completed");
                                isFinished = true;
                                killed = true;
                                return;
                            }
                        } else {  //KEDZE VSETKY OSTATNE PARTY OKREM POSLEDNEHO MAJU EXTRA BYT ...
                            if (Files.size(Paths.get(filePath)) == (endByte - startByte) + 1) {
                                System.out.println("Thread:" + this.getName() + " DEBUG: download of part " + partNum + " completed");
                                isFinished = true;
                                killed = true;
                                return;
                            }
                        }
                    }

                    if (killed) {
                        System.out.println("Download thread for id:"+dtask.getId()+" with id:"+getId()+" HAS BEEN KILLED");
                        break;
                    }

                    try {
                        System.out.println("download partnum:" + partNum + " thread Im sleeping");
                        this.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("INTERUPTED EXCEPTION REACHED");
                    }
                }

            } catch (IOException ex) {
                System.out.println("DEBUG:downlaod failed at reading and writing to file");
                //error();
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Cant connect to url");
        }

        isDownloaded();
        

    }

    private long getSizeOfFileSafe() {

        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException ex) {
            //Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    private boolean isDownloaded() {
        try {
            if (startByte + Files.size(Paths.get(filePath)) == endByte) {
                System.out.println("Thread:" + this.getName() + " DEBUG: download of part " + partNum + " completed");
                isFinished = true;
                killed = true;
                return true;
            }
        } catch (Exception ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);

        }
        isFinished = false;
        return false;
    }

    public boolean isFinished() {
        return isFinished;
    }

}
