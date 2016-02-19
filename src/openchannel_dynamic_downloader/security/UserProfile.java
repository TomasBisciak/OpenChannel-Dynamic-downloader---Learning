/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import openchannel_dynamic_downloader.downloader.DownloadTask;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import openchannel_dynamic_downloader.h2.H2DatabaseConnector;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * NOT SURE IF IT IWLL BE MUTABLE
 *
 * @author tomas
 */
public class UserProfile {

    /**
     * Default value for default connection
     */
    private String password = "sa";
    private String username = "sa";//not case sensitive
    private String email = null;

    /**
     *
     */
    private SimpleStringProperty downloadsDir;
    private SimpleIntegerProperty numOfConnectionsPerDownload;
    // private SimpleStringProperty downloadsDir;

    public static final String PREP_STAT_INSERT_USER_PROF = "INSERT INTO userProfiles () VALUES (?,?,?)";

    //stored under userprofile preferences dir
    private Preferences preferences;

    //something to help insert itself into database
    public UserProfile() {//default user initialization
        preferences = Preferences.userRoot().node("openchannel/users/sa");
    }

    public UserProfile(String username, String password) {
        setUsername(username);
        //downloadsDir=new SimpleStringProperty(Info.OC_DOWNLOADS_DIR+getUsername()+"\\");  //todo remove
        setPassword(password);
        preferences = Preferences.userRoot().node("openchannel/users/" + username);//test if capslock will not mess up or '/' character

        downloadsDir = new SimpleStringProperty(preferences.get(Info.PreferenceData.PREF_USER_DOWNLOADS_DIR, Info.OC_DOWNLOADS_DIR + username + "\\"));
        numOfConnectionsPerDownload = new SimpleIntegerProperty(preferences.getInt(Info.PreferenceData.PREF_USER_NUMOFCON_THREADS,DownloadTask.CONNECTIONS_DEF));

        downloadsDir.addListener((v, oldValue, newValue) -> {
            preferences.put(Info.PreferenceData.PREF_USER_DOWNLOADS_DIR, getDownloadsDir());
        });
        numOfConnectionsPerDownload.addListener((v, oldValue, newValue) -> {
            preferences.putInt(Info.PreferenceData.PREF_USER_NUMOFCON_THREADS, getNumOfConnectionsPerDownload());
        });
        loadAllPreferences();
    }

    public UserProfile(String username, String password, String email) {
        setUsername(username);
        //downloadsDir=new SimpleStringProperty(Info.OC_DOWNLOADS_DIR+getUsername()+"\\");  //todo remove
        setPassword(password);
        preferences = Preferences.userRoot().node("openchannel/users/" + username);//test if capslock will not mess up or '/' character

        downloadsDir = new SimpleStringProperty(preferences.get(Info.PreferenceData.PREF_USER_DOWNLOADS_DIR, Info.OC_DOWNLOADS_DIR + username + "\\"));
        downloadsDir.addListener((v, oldValue, newValue) -> {
            preferences.put(Info.PreferenceData.PREF_USER_DOWNLOADS_DIR, getDownloadsDir());
            System.out.println("DEBUG:putting in preference data for downloads dir on userprofile");
        });
      //  numOfConnectionsPerDownload

        loadAllPreferences();
    }

    public final void loadAllPreferences() {
        downloadsDir.set(preferences.get(Info.PreferenceData.PREF_USER_DOWNLOADS_DIR, Info.OC_DOWNLOADS_DIR + getUsername() + "\\"));
        //and so on
    }

    public final <T> void loadPreference(String preferenceKey, ObservableValue<T> observableValue) {

    }

    public final void updateAllPreferences(String downloadsDir) {//PREFERENCES VIEW CONTROLLER invokes this method to apply changes//not
        setDownloadsDir(downloadsDir);
    }

    public String getPassword() {
        return password;
    }

    public final String getUsername() {
        return username;
    }

    public final String getEmail() {
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
        Preferences.userRoot().node("openchannel/users/" + username);//test if capslock will not mess up or '/' character
    }

    public final void setEmail(String email) {
        this.email = email;
    }

    //utility methods etc
    /**
     * Create UserProfile , insert it into database and return this ready to use profile.
     *
     * @param name
     * @param password
     * @param email
     * @return UserProfile that has been created , stored in database and ready to be used.Has default preferences
     */
    public static final UserProfile createOCProfile(String name, String password, String email) {

        return (UserProfile) new H2DatabaseConnector(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD) {

            @Override
            public <T> T executeRetrieve() {
                try {

                    prepStat = getConnection().prepareStatement("SELECT name FROM users WHERE name=?");
                    prepStat.setString(1, name);
                    resultSet = prepStat.executeQuery();
                    if (resultSet.next()) {
                        System.out.println("holla");
                        return null;
                    } else {
                        System.out.println("holla2");
                        prepStat = getConnection().prepareStatement("INSERT INTO users VALUES(?,?,?)");
                        prepStat.setString(1, name);
                        prepStat.setString(2, password);
                        prepStat.setString(3, email);
                        prepStat.executeUpdate();
                        return (T) new UserProfile(name, password, email);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

        }.executeRetrieve();

    }

    public void createDownloadsDir() {
        try {
            Files.createDirectories(Paths.get(getDownloadsDir()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public final boolean validate() {
        //check database validate existance and compare passwords
        String pswrd = (String) new H2DatabaseConnector(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD) {

            @Override
            public <T> T executeRetrieve() {

                try {
                    prepStat = getConnection().prepareStatement("SELECT password FROM users WHERE name=?");
                    prepStat.setString(1, getUsername());
                    resultSet = prepStat.executeQuery();
                    if (resultSet.next()) {
                        System.out.println("Password for username ;" + getUsername() + "; is= " + resultSet.getString(1));
                        return (T) resultSet.getString(1);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return null;
            }

        }.executeRetrieve();
        if (pswrd != null && pswrd.equals(getPassword())) {
            return true;
        }
        return false;
    }

    public static final boolean validatePasswordString(String password) {
        //todo create password validation
        System.out.println("TODO CREATE PASSWORD VALIDATION");
        return true;
    }

    public final void login() {
        MainDataModel.getInstance().loginProfile = this;
    }

    @Override
    public String toString() {
        return "UserProfile Username:" + username + " Password:" + password + " \nPreferences" + getPreferences();
    }

    public PreparedStatement getPreparedStatement() {
        return null;
    }

    //not sure if needed
    public void savePreferences() {

    }

    public Preferences getPreferences() {
        return preferences;
    }

    public SimpleStringProperty getDownloadsDirProperty() {
        return downloadsDir;
    }

    public final String getDownloadsDir() {
        return downloadsDir.get();
    }

    public void setDownloadsDir(String url) {
        downloadsDir.set(url);
    }

    public void setNumOfConnectionsPerDownload(int value){
        numOfConnectionsPerDownload.set(value);
    }
    
    public int getNumOfConnectionsPerDownload() {
        return numOfConnectionsPerDownload.get();
    }

}
