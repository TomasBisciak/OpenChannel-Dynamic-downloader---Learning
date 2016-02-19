/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.preferences;

import java.util.prefs.Preferences;
import openchannel_dynamic_downloader.model.MainDataModel;

/**
 *
 * @author tomas
 */
public class ConfigPref {
    
   // private static ;
    public boolean gameMode;
    public boolean soundNotifications=true;
    public boolean fullscreenStartup=true;
    public double notificationVolume=50;
    
    
    
    
    private static ConfigPref instance=new ConfigPref();
            
    public static ConfigPref getInstance() {
        return instance;
    }
            
    public ConfigPref(){
        //pref=Preferences.userRoot().node
    }
    
    public boolean writePref(){
        
        return true;
    }
    
    public boolean loadPref(){
        return true;
    }
    
}
