/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import openchannel_dynamic_downloader.h2.H2DatabaseConnector;
import openchannel_dynamic_downloader.model.MainDataModel;

/**
 *
 * @author tomas
 */
public class DbUtil {

    
    
    public static final void getUsersTableInfo(){
        new H2DatabaseConnector(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD){

            @Override
            public void execute() {
                try (PreparedStatement stmt = getConnection().prepareStatement("select * from users")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                String name = rs.getString(1);
                                String password = rs.getString(2);
                                String email = rs.getString(3);
                                System.out.println(name + "; " + password + "; " + email);
                            }
                        }
                    } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            
        }.execute();
    }
    
    public static final void createUsersTable() {
        new H2DatabaseConnector(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD) {

            @Override
            public void execute() {
                try {
                    if (getStatement().execute("CREATE TABLE users(name VARCHAR(255) NOT NULL,password VARCHAR(255) NOT NULL,email VARCHAR(255))")) {
                        System.out.println("Table created");
                    } else {
                        System.out.println("Table is not created");
                    }
                    /* //stack overflow madProgrammer
                    getConnection().commit();
                    System.out.println("Table Created users");

                    try (PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO users (name, password, email) VALUES (?,?,?)")) {
                        stmt.setString(1, "sa");
                        stmt.setString(2, "sa");
                        stmt.setString(3, null);
                        int rows = stmt.executeUpdate();
                        System.out.println(rows + " where inserted");
                        getConnection().commit();
                    }

                    try (PreparedStatement stmt = getConnection().prepareStatement("select * from users")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                String name = rs.getString(1);
                                String password = rs.getString(2);
                                String email = rs.getString(3);
                                System.out.println(name + "; " + password + "; " + email);
                            }
                        }
                    }
                    */
                    getStatement().executeUpdate("INSERT INTO users VALUES ('sa','sa',NULL)");
                    
                    closeConnection();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        }.execute();
    }
    
//not used method
    public static final void insertDataTest() {

        new H2DatabaseConnector(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD) {

            @Override
            public void execute() {
                try {
                    getStatement().executeUpdate("INSERT INTO users VALUES ('sa','sa',NULL)");
                    getConnection().commit();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        }.execute();

    }

    public static final void createTables() {
        new H2DatabaseConnector(MainDataModel.loginProfile) {

            @Override
            public void execute() {
                try {
                    /*
                     CREATE TABLE legs(legid INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                     playerid1 INT NOT NULL REFERENCES players(playerid),
                     playerid2 INT NOT NULL REFERENCES players(playerid),
                     added TIMESTAMP AS CURRENT_TIMESTAMP NOT NULL,
                     CHECK (playerid1 <> playerid2));
                     */
                    getStatement().execute("CREATE TABLE downloads (duId bigint auto_increment)");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        }.execute();
    }

}
