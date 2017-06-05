/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;

/**
 *
 * @author Kofola
 */
public class FxmlCloudViewController implements Initializable {

    @FXML
    private TextField txtFieldCloudSyncFolder;
    @FXML
    private Button btnBrowseDirectory;
    @FXML
    private Button btnCheckSize;
    @FXML
    private Button btnConfirmPrefix;

    public FxmlCloudViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBrowseDirectory.setOnMouseClicked((MouseEvent event) -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("CloudSync directory:");
            File defaultDirectory = new File(MainDataModel.getInstance().loginProfile.getDownloadsDir());
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(OpenChannel_Dynamic_Downloader.primStage);
            try {
                txtFieldCloudSyncFolder.setText(selectedDirectory.getPath());
            } catch (NullPointerException e) {
                System.out.println("No dir selected");
            }

        });
    }

}
