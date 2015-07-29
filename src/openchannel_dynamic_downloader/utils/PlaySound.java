/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.utils;

import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author tomas
 */
public class PlaySound {

    public static final int SOUND_DOWNLOAD_FINISHED = 0;

    private static URL[] soundFiles = {
        PlaySound.class.getResource("openchannel_dynamic_downloader/resources/sounds/DownloadFinished.mp3")

    };

    private static Media media;
    private static MediaPlayer mediaPlayer;

    private PlaySound() {
    }

    public static void playSound(int sound) {
        Thread t = new Thread(new Runnable() {//f*ck lambda >;) im a rebel
            public void run() {
                media = new Media(soundFiles[sound].toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

}
