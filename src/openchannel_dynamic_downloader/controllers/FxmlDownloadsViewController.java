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
import javafx.scene.layout.BorderPane;
import openchannel_dynamic_downloader.controls.OCTableView;

/**
 *
 * @author tomas
 */
public class FxmlDownloadsViewController implements Initializable {

    
    
    @FXML
    private BorderPane holder;

    
   

    OCTableView ocTableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //give OCTableView into holder
       
        System.out.println("Calling initializer of downloadsViewController");
        ocTableView=new OCTableView();
        holder.setCenter(ocTableView);
    }
    
    private void filterTable(int FILTER){
        //how to filter out items in table
    }


    public FxmlDownloadsViewController() {
       
        System.out.println("Calling Downloads CONSTRUCTOR");
    }

   

}
 