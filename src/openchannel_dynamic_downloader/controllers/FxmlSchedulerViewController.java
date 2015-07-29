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
import javafx.scene.layout.GridPane;

/**
 *
 * @author tomas
 */
public class FxmlSchedulerViewController implements Initializable{
    @FXML
    private BorderPane gridPaneHolder;
    
    private GridPane gridPane=new GridPane();
   
    @Override
    public void initialize(URL location, ResourceBundle resources) {
       gridPane.setGridLinesVisible(true);
       gridPane.addColumn(24);
       gridPane.addRow(7);
    }
    
    public FxmlSchedulerViewController(){
        
    }
    
}
