/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.security;

import java.sql.PreparedStatement;
import java.util.prefs.Preferences;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.preferences.ConfigPref;

/**
 * 
 * NOT SURE IF IT IWLL BE MUTABLE
 * @author tomas
 */
public class UserProfile {

    //default
    private String password = "sa";
    private String username = "sa";//not case sensitive
    private String email = null;
    
    public static final String PREP_STAT_INSERT_USER_PROF="INSERT INTO userProfiles () VALUES (?,?,?)";
    
    //stored under userprofile preferences dir
    private Preferences preferences;

    //something to help insert itself into database
    public UserProfile() {//default user initialization
        preferences=Preferences.userRoot().node("openchannel/users/sa");
    }

    public UserProfile(String username, String password) {
        setUsername(username);
        setPassword(password);
        preferences=Preferences.userRoot().node("openchannel/users/"+username);//test if capslock will not mess up or '/' character
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
  public final String getEmail(){
        return email;
    }

    //some check maybe
    public final void setPassword(String aPassword) {
        password = aPassword;
        //TODO implemnt on password change 
    }

    public final void setUsername(String aUsername) {
        //TODO check if available and not alreday included in database
        username = aUsername;
        //TODO rewrite all preferences from old ones to new ones
        Preferences.userRoot().node("openchannel/users/"+username);//test if capslock will not mess up or '/' character
    }
   public final void setEmail(String email){
        this.email=email;
    }
    //utility methods etc
    /**
     * Create UserProfile , insert it into database and return this ready to use profile.
     *
     * @param name
     * @param password
     * @return UserProfile that has been created , stored in database and ready to be used.Has default preferences
     */
    public static UserProfile createOCProfile(String name, String password) {
        return null;
    }

    public final boolean validate() {
        //check database validate existance and compare passwords
        return true;
    }

    public final void login() {
        MainDataModel.loginProfile = this;
    }

    @Override
    public String toString() {
        return "UserProfile Username:"+username+" Password:"+password+" \nPreferences"+getPreferences();
    }
    
    public PreparedStatement getPreparedStatement(){
        return null;
    }
    //not sure if needed
    public void savePreferences(){
        
    }
   

    public Preferences getPreferences() {
        return preferences;
    }

}
