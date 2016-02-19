/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author tomas
 */
public class FxmlPreferencesViewController implements Initializable {

    @FXML
    private Button resetDefaultBtn;
    @FXML
    private Button confirm;
    @FXML
    private TextField txtFieldDownloadsFolder;
    @FXML
    private Button btnBrowseDownloads;
    @FXML
    private TextField txtFieldNumOfCon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //load all the preferences on this view show
        txtFieldDownloadsFolder.setText(MainDataModel.getInstance().loginProfile.getDownloadsDir());
        txtFieldDownloadsFolder.textProperty().addListener((v, oldValue, newValue) -> {
            try {
                if (Files.isDirectory(Paths.get(newValue))) {
                    txtFieldDownloadsFolder.setStyle("");
                    txtFieldDownloadsFolder.setTooltip(null);
                } else {
                    txtFieldDownloadsFolder.setStyle("-fx-border-color:red;");
                    txtFieldDownloadsFolder.setTooltip(new Tooltip("Not directory"));

                }
            } catch (Exception e) {
                e.printStackTrace();
                txtFieldDownloadsFolder.setTooltip(new Tooltip("Incorrect uri"));
                txtFieldDownloadsFolder.setStyle("-fx-border-color:red;");
            }

        });
        
        txtFieldNumOfCon.setText(String.valueOf(MainDataModel.getInstance().loginProfile.getNumOfConnectionsPerDownload()));
        txtFieldNumOfCon.textProperty().addListener((val,oldVal,newVal)->{
            try{
                if( Integer.valueOf(txtFieldNumOfCon.getText())>0){
                       txtFieldDownloadsFolder.setStyle("");
                    if(Integer.valueOf(txtFieldNumOfCon.getText())>20){
                        //SHOW USER THAT THIS VALUE IS KIND OF HIGH. And not recommended to be used.Bud allowed
                    }
                }
               
            }catch(Exception e){
                txtFieldNumOfCon.setStyle("-fx-border-color:red;");
                e.printStackTrace();
            }
        });
        

        btnBrowseDownloads.setOnMouseClicked((MouseEvent event) -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("OpenChannel Downloads directory:");
            File defaultDirectory = new File(MainDataModel.getInstance().loginProfile.getDownloadsDir());
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(OpenChannel_Dynamic_Downloader.primStage);
            try{
                 txtFieldDownloadsFolder.setText(selectedDirectory.getPath());
            }catch(NullPointerException e){
                System.out.println("No dir selected");
            }
           

        });
    }

    @FXML
    private void confirmChanges() {
        //if textField has no style set , value is supposed to be valid.//probably not best way of handling bud not bad either.
        FxmlMainViewController.showPaneNotification("Preferences updated.", 2000);
        if(txtFieldDownloadsFolder.getStyle().equals("")&&(txtFieldDownloadsFolder.getText().length()>0)){
              MainDataModel.getInstance().loginProfile.setDownloadsDir(txtFieldDownloadsFolder.getText());
              //confirm preferences
        }
        if(txtFieldNumOfCon.getStyle().equals("")){
             MainDataModel.getInstance().loginProfile.setNumOfConnectionsPerDownload(Integer.valueOf(txtFieldNumOfCon.getText()));
        }
      

    }
    //if view is going to be changed and data are changed and not saved , prompt user if he wants to save his data or discard any changes
    private boolean beforeViewChange(){
        return true;
    }
    
    @FXML
    private void resetDefaultSettings(){
         FxmlMainViewController.showPaneNotification("Preferences: Set to default values",2000);
    }
    

    
    
}
