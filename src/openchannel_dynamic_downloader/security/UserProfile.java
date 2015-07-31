/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.security;

import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.preferences.ConfigPref;

/**
 * Password will be in memory at runtime.
 *
 * @author tomas
 */
public class UserProfile {

    //default
    private String password = "sa";
    private String username = "sa";//not case sensitive
    private String email = "";

    private ConfigPref preferences;

    //something to help insert itself into database
    public UserProfile() {

    }

    public UserProfile(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    //some check maybe
    public final void setPassword(String aPassword) {
        password = aPassword;
    }

    public final void setUsername(String aUsername) {
        username = aUsername;
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

}
