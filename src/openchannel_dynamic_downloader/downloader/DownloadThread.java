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
 *Thread safe class
 * @author tomas bisciak
 */
public class DownloadThread extends Thread {

    private boolean isFinished;

    private long bytesReadWhileActive;
    private long startByte;
    private long endByte;
    private int partNum;
    private String filePath;

    private DownloadTask dtask;

    public static int readTimeout = 60000;
    public static int connectionTimeout = 60000;

    private volatile boolean killed;
    private final Object lock = new Object();

    public void setKilled(boolean killed) {
        synchronized (lock) {
            this.killed = killed;
        }
    }

 
    public DownloadThread() {

    }


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
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {

        try {

            if (isDownloaded()) {
                killed = true;
                return;
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(dtask.getSource()).openConnection();

            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectionTimeout);
     
           
            conn.setRequestProperty("Range",
                    "bytes=" + (startByte + getSizeOfFileSafe()) + "-" + endByte);//size of already downloaded

            System.out.println("Content lenght validity:" + conn.getContentLengthLong());
            //conn.getContentLengthLong()///...

            System.out.println("Range on http:" + (startByte + getSizeOfFileSafe()) + "-" + endByte);

            try (
                    BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd")) {

                int readed = -2;
                byte[] buffer = new byte[DownloadTask.BUFFER_SIZE];//might be calculated form download
                raf.seek(getSizeOfFileSafe());
                while (true) {

                    while (!killed && ((readed = in.read(buffer, 0, DownloadTask.BUFFER_SIZE)) != -1)) {
                        raf.write(buffer, 0, readed);
                        bytesReadWhileActive += readed;
                        if (partNum == dtask.getNumberOfConnections()) {//check added cause of extra bytes , last part doesnt have extra bytes

                            if (Files.size(Paths.get(filePath)) == endByte - startByte) {
                                System.out.println("Thread:" + this.getName() + " DEBUG: download of part " + partNum + " completed");
                                isFinished = true;
                                killed = true;
                                return;
                            }
                        } else { //extra byte 
                            if (Files.size(Paths.get(filePath)) == (endByte - startByte) + 1) {
                                System.out.println("Thread:" + this.getName() + " DEBUG: download of part " + partNum + " completed");
                                isFinished = true;
                                killed = true;
                                return;
                            }
                        }
                    }

                    if (killed) {
                        System.out.println("Download thread for id:" + dtask.getId() + " with id:" + getId() + " HAS BEEN KILLED");
                        // raf.close();
                        break;
                    }

                    try {
                        
                        sleep(1000);
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
           // Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);

        }
        isFinished = false;
        return false;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public long getBytesReadWhileActive() {
        return bytesReadWhileActive;
    }

}
