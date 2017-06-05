/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package openchannel_dynamic_downloader.resources.sounds;


import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author tomas
 */
public class SystemSound implements Playable,Runnable{

	private int file;
        
        private  URL[] soundFiles = { 
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/Connected777.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/ConnectionFailed1s392.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/Disconnected987.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/Intro17s474.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/Purchase4s848.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/Security6s454.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/WelcomeBack1s040.mp3"),
			getClass().getResource("/bitcompile/ecps/resources/media/sounds/WelcomeEcps3s497.mp3"),
	};
        
        private URL selectedURL;

	public SystemSound(int selectedURL) {
		this.selectedURL=soundFiles[selectedURL];
	}
	
	@Override
	public void run() {
		play();
	}

        @Override
	public void play() {
		//String bip =file.toString();
           
                String bip = selectedURL.toString();
                Media media=new Media(bip);
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
	}

}