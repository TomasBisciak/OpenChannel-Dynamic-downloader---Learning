/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import openchannel_dynamic_downloader.controls.OCTableView;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author tomas
 */
public class FxmlMainViewController implements Initializable {

    @FXML
    private ScrollPane dynamicNode;

    private ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private ToggleButton allMenuBtn;
    @FXML
    private ToggleButton downloadingMenuBtn;
    @FXML
    private ToggleButton completedMenuBtn;
    @FXML
    private ToggleButton inactiveMenuBtn;

    private ToggleButton[] downloadsMenuBtns;

    @FXML
    private ToggleButton schedulerMenuBtn;
    @FXML
    private ToggleButton activateMenuBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        allMenuBtn.setToggleGroup(toggleGroup);
        downloadingMenuBtn.setToggleGroup(toggleGroup);
        completedMenuBtn.setToggleGroup(toggleGroup);
        inactiveMenuBtn.setToggleGroup(toggleGroup);
        schedulerMenuBtn.setToggleGroup(toggleGroup);

        downloadsMenuBtns = new ToggleButton[]{allMenuBtn, downloadingMenuBtn,
            completedMenuBtn, inactiveMenuBtn};

    }

    public FxmlMainViewController() {

    }

    public void setView(Node node) {
        dynamicNode.setContent(node);
    }

    public Node getView(String fxmlPath) {
        try {
            return new FXMLLoader(getClass().getResource(fxmlPath)).load();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void clearView() {
        dynamicNode.setContent(new BorderPane(new Label("HELLO")));//placeholder
    }

    // DOWNLOADS view has to be loaded with parameters
    @FXML
    private void setViewDownloads() {
        OCTableView.viewFilter = OCTableView.DownloadsFilter.SHOW_EVERYTHING;
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
    }

    @FXML
    private void setViewDownloading() {
        OCTableView.viewFilter = OCTableView.DownloadsFilter.SHOW_ACTIVE;
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
    }

    @FXML
    private void setViewCompleted() {
        OCTableView.viewFilter = OCTableView.DownloadsFilter.SHOW_COMPLETED;
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
    }

    @FXML
    private void setViewInactive() {
        OCTableView.viewFilter = OCTableView.DownloadsFilter.SHOW_INACTIVE;
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
    }

    @FXML
    private void setViewScheduler() {
        setView(getView(Info.Resource.FXML_FILE_SCHEDULER));
    }

    @FXML
    private void activateScheduler() {
        if (activateMenuBtn.isSelected()) {
            activateMenuBtn.setStyle("-fx-background-radius:0;-fx-background-color:#1abc9c;");
            activateMenuBtn.setText("Activated");
            activateMenuBtn.setTextFill(Paint.valueOf("white"));
        } else {
            activateMenuBtn.setStyle("-fx-background-radius:0;");
            activateMenuBtn.setText("Activate");
            activateMenuBtn.setTextFill(Paint.valueOf("black"));

        }
    }

}
