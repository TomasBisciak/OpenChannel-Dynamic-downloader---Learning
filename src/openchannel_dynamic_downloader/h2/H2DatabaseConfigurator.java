/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.h2;

import java.io.File;

/**
 *
 * @author tomas
 */
public class H2DatabaseConfigurator {
    
    //additional functionality
    
    
    public static final String DB_FILE=File.separator+"dboc";
    
   
    
    public static final String TYPEDB_FILE = "file";
    public static final String TYPEDB_MEM = "mem";
    public static final String TYPEDB_RES = "res";
    
    private static String type="";
    
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    private static H2DatabaseConfigurator instance=new H2DatabaseConfigurator();
    
    private H2DatabaseConfigurator(){
        setPassword(password);
        setUsername(username);
        setType(type);
    }
    
    
    
    
    public static H2DatabaseConfigurator getInstance(){
        return instance;
    }

    public static String getType() {
        return type;
    }


    public static void setType(String aType) {
        type = aType;
    }
    
    
    
}
