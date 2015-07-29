/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * not sure if you actually need to create instance of it its kind of useless
 * @author tomas
 */
public class H2DatabaseConnector {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public H2DatabaseConnector() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            //throw some error noticicatio n maybe
        }

    }

    private void closeConnection() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
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

    private void openConnection() {

    }

}
