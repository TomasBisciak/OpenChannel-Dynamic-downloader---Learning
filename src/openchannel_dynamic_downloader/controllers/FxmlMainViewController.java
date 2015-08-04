/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import openchannel_dynamic_downloader.controls.OCTableView;
import openchannel_dynamic_downloader.utils.FileUtils;
import openchannel_dynamic_downloader.utils.Info;
import org.controlsfx.control.NotificationPane;

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

    @FXML
    public RadioButton rbtnDiskSpace;
    @FXML
    public static HBox notifPaneHolder;

    private static volatile boolean isOneTimeInit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        allMenuBtn.setToggleGroup(toggleGroup);
        downloadingMenuBtn.setToggleGroup(toggleGroup);
        completedMenuBtn.setToggleGroup(toggleGroup);
        inactiveMenuBtn.setToggleGroup(toggleGroup);
        schedulerMenuBtn.setToggleGroup(toggleGroup);

        downloadsMenuBtns = new ToggleButton[]{allMenuBtn, downloadingMenuBtn,
            completedMenuBtn, inactiveMenuBtn};

        System.out.println("Calling initialize MainViewController");
        //might be in trycatch exception when window closed.
        if (!isOneTimeInit) {
            System.out.println("OneTimeInit");
            @SuppressWarnings("SleepWhileInLoop")
            Thread diskSpaceChecker = new Thread(() -> {
                while (true) {

                    try {
                        //convert to mb
                        System.out.println("RefreshLoop");
                        Platform.runLater(() -> {
                            System.out.println("updating radiotext");
                            //TODO change logic to show mb if small
                            //optimize
                            float diskSpace = FileUtils.getAllDiskSpace();
                            float usable = FileUtils.getAllUsableDiskSpace();

                            if ((usable / 1024 / 1024 / 1024) >= 0.2 * (diskSpace / 1024 / 1024 / 1024)) {
                                rbtnDiskSpace.setStyle("-fx-mark-color:green");
                                rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB ", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));

                            } else if ((usable / 1024 / 1024 / 1024) >= 0.1 * (diskSpace / 1024 / 1024 / 1024)) {
                                rbtnDiskSpace.setStyle("-fx-mark-color:orange");
                                rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB Above 10%%", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));
                            } else {
                                rbtnDiskSpace.setStyle("-fx-mark-color:orange");
                                rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB Bellow 10%%", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));
                            }

                        });
                        //TODO asi obsolete check
                        if (!rbtnDiskSpace.isVisible()) {
                            rbtnDiskSpace.setVisible(true);
                        }
                        Thread.sleep(10000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            diskSpaceChecker.setName("Disk space checker");
            diskSpaceChecker.setPriority(Thread.MIN_PRIORITY);
            diskSpaceChecker.setDaemon(true);
            diskSpaceChecker.start();
            isOneTimeInit = true;
        }

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
    //TODO create this method
    public static final void showPaneNotification(String text,int miliseconds){
        NotificationPane notifPane=new NotificationPane();
        notifPane.setText(text);
        notifPaneHolder.getChildren().add(notifPane);
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
    private void setViewDatabase(){
        //TODO fix
        //setView(getView(Info.Resource.FXML_FILE_DATABASE));
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
