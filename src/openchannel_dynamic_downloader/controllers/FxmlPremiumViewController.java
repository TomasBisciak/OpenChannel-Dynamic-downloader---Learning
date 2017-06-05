/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;




/**
 *
 * @author Kofola
 */
public class FxmlPremiumViewController implements Initializable{

    
    @FXML
    private WebView webViewPremium;
    
    public FxmlPremiumViewController(){
        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
         WebEngine webEngine = webViewPremium.getEngine();
        webEngine.load("https://www.spotify.com/sk/premium/");
    }
    
}
