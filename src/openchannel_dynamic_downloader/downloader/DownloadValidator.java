/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;

import java.net.URL;
import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * @author tomas
 */
public class DownloadValidator {

    private static String[] schemes = {"http", "https", "ftp"};
    private static UrlValidator urlValidator = new UrlValidator(schemes);

    /*
     if (urlValidator.isValid(url)) {
     //  url.matches(url);//check ends with  "/something.something" 
     //ALSO CHECK FOR FRAGMENTS #..SOMETHING remove fragment ignore it
     }
     */
    
    public DownloadValidator(String[] schemes, boolean symbolicLinks) {

    }
    
    public final boolean validate(URL url){
        return true;
    }
    

}
