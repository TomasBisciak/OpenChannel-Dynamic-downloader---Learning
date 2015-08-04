/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.h2;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.utils.Info;

/**
 * not sure if you actually need to create instance of it its kind of useless
 *
 * @author tomas
 */
public class H2DatabaseConnector {

    private Connection connection;
    public Statement statement;
    private ResultSet resultSet;

    //not sure if here or on objects themself that need them
    public static final String[] PREP_STATEMENTS = new String[]{};

    public static final String DB_FILE_NAME = File.separator + "dboc";
    public static final String DB_DIR = Info.OC_DIR + "oc_database\\";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public H2DatabaseConnector(String username, String password) {
        try {
            openConnection(username, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public H2DatabaseConnector(UserProfile profile) {
        try {
            openConnection(profile);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public H2DatabaseConnector() {
        try {
            openConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    //TODO not sure if needed
//make one autoexec contructor
    public H2DatabaseConnector(boolean autoExec) {
        try {
            openConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (autoExec) {
            execute();//ignore warning
            closeConnection();
        }

    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + DB_FILE_NAME+";DATABASE_TO_UPPER=false", MainDataModel.loginProfile.getUsername(), MainDataModel.loginProfile.getPassword());
        statement = connection.createStatement();
    }

    private void openConnection(String username, String password) throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + DB_FILE_NAME+";DATABASE_TO_UPPER=false", username, password);
        statement = connection.createStatement();
    }

    private void openConnection(UserProfile profile) throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + DB_FILE_NAME+";DATABASE_TO_UPPER=false", profile.getUsername(), profile.getPassword());
        statement = connection.createStatement();
    }
    //to be overriden

    public void execute() {
    }

    //to be overriden
    public void execute(String query) {
    }
//not sure if i make it limited for iterable only
    //TODO might change to <T>
    //to be overriden

    public <T extends Iterable> T executeRetrieve() {
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

}
