/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.h2;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.utils.Info;

/**
 * not sure if you actually need to create instance of it its kind of useless
 * Might be needed connection pool
 * @author tomas
 */
public class H2DatabaseConnector {

    private Connection connection;
    public Statement statement;
    public ResultSet resultSet;
    public ResultSetMetaData resultSetMd;
    public PreparedStatement prepStat;
    //not sure if here or on objects themself that need them
    public static final String[] PREP_STATEMENTS = new String[]{};

    public static final String DB_FILE_NAME = "dboc";
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
    @SuppressWarnings("OverridableMethodCallInConstructor")
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

    public final void closeConnection() {
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

    public final void openConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + File.separator
                + MainDataModel.getInstance().loginProfile.getUsername() + File.separator + DB_FILE_NAME + ";DATABASE_TO_UPPER=false;MULTI_THREADED=TRUE",
                MainDataModel.getInstance().loginProfile.getUsername(), MainDataModel.getInstance().loginProfile.getPassword());
        statement = connection.createStatement();
    }

    public final void openConnection(String username, String password) throws SQLException {
        if (username.equals(Info.Db.DB_MAIN_USERNAME)) {
            connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + DB_FILE_NAME + ";DATABASE_TO_UPPER=false;MULTI_THREADED=TRUE", username, password);
        } else {
            connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + File.separator
                    + MainDataModel.getInstance().loginProfile.getUsername() + File.separator + DB_FILE_NAME + ";DATABASE_TO_UPPER=false;MULTI_THREADED=TRUE",
                    username, password);
        }

        statement = connection.createStatement();
    }

    public final void openConnection(UserProfile profile) throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:file:" + DB_DIR + File.separator
                + MainDataModel.getInstance().loginProfile.getUsername() + File.separator + DB_FILE_NAME + ";DATABASE_TO_UPPER=false;MULTI_THREADED=TRUE",
                profile.getUsername(), profile.getPassword());
        statement = connection.createStatement();
    }
    //to be overriden

    public void execute() {
    }

    //to be overriden
    public void execute(String query) {
    }

    public <T> void execute(T param) {

    }
//not sure if i make it limited for iterable only
    //TODO might change to <T>
    //to be overriden

    public <T extends Iterable> T executeRetrieveIterable() {
        return null;
    }

    public <T> T executeRetrieve() {
        return null;
    }

    public <T> T executeRetrieve(T param) {
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

}
