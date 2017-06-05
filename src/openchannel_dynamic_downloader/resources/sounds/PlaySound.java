/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package openchannel_dynamic_downloader.resources.sounds;

/**
 *
 * @author tomas
 */
public class PlaySound {
	// system sounds
	public static final int CONNECTED = 0;
	public static final int CONNECTION_FAILED = 1;
	public static final int DISCONNECTED = 2;
	public static final int INTRO = 3;
	public static final int PURCHASE = 4;
	public static final int SECURITY = 5;
	public static final int WELCOME_BACK = 6;
	public static final int WELCOME_ECPS = 7;



	public static void play(int sound) {
		new Thread(new SystemSound(sound)).start();
	}
	
}
