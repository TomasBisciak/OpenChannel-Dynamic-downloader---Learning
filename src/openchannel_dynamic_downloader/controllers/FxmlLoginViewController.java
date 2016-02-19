/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.utils.Email;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author tomas
 */
public class FxmlLoginViewController implements Initializable {

    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private TextField createUsernameField;
    @FXML
    private TextField createEmailField;//optional
    @FXML
    private TextField createPasswordField;

    @FXML
    private Button loginBtn;
    @FXML
    private Label lblLogin;
    @FXML
    private Button createBtn;
    @FXML
    private Label lblCreate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        hookListeners();

    }

    private boolean emailValidity = false;

    private void hookListeners() {
        loginBtn.setOnMouseClicked((MouseEvent event) -> {
            login();
        });
//        //EVERYTHING HERE ON FX APPLICATION THREAD / MIGH TBE MOVED OFF OF IT for performance gain//fixed
//        loginBtn.setOnMouseClicked((MouseEvent event) -> {
//            new Thread(() -> {
//                UserProfile logProfile;
//                if (loginUsernameField.getText().equals("") && loginPasswordField.getText().equals("")) {
//                    logProfile = new UserProfile();
//                } else {
//                    logProfile = new UserProfile(loginUsernameField.getText(), loginPasswordField.getText());
//                }
//
//                if (logProfile.validate()) {
//                    logProfile.login();
//
//                    //hide login screen
//                    Platform.runLater(() -> {
//                        OpenChannel_Dynamic_Downloader.primStage.hide();
//                    });
//
//                    try {//invoke start method and lats do it again
//                        //logged in , check preferences
//                        if (logProfile.getPreferences().getBoolean(Info.PreferenceData.PREF_USER_FIRST_TIME_RUN, true)) {
//                            OpenChannel_Dynamic_Downloader.showEulaWindow();
//                        } else {
//                            OpenChannel_Dynamic_Downloader.showMainView();
//                        }
//
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//
//                } else {
//                    Platform.runLater(() -> {
//                        lblLogin.setText("incorrect credentials");
//                    });
//                }
//            }).start();
//
//        });
        createEmailField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if (newValue.length() > 0) {
                if (Email.validateEmailAdress(newValue)) {
                    createEmailField.setStyle("-fx-border-color:red");
                    emailValidity = false;
                } else {
                    createEmailField.setStyle("-fx-border-color:green");
                    emailValidity = true;
                }
            } else {
                createEmailField.setStyle("");
                emailValidity = true;
            }
        });

        createBtn.setOnMouseClicked((MouseEvent event) -> {
            create();
        });

    }

    /**
     *
     * @param logInUser
     * @param feedback
     */
    public final void logIn(UserProfile logInUser, boolean feedback) {

        if (logInUser.validate()) {
            logInUser.login();

            //hide login screen
            Platform.runLater(() -> {
                OpenChannel_Dynamic_Downloader.primStage.hide();
            });

            try {//invoke start method and lats do it again
                //logged in , check preferences
                if (logInUser.getPreferences().getBoolean(Info.PreferenceData.PREF_USER_FIRST_TIME_RUN, true)) {
                    OpenChannel_Dynamic_Downloader.showEulaWindow();
                } else {
                    OpenChannel_Dynamic_Downloader.showMainView();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            if (feedback) {
                Platform.runLater(() -> {
                    lblLogin.setText("incorrect credentials");
                });
            }

        }
    }

    @FXML
    private void loginOnEnter() {
        login();
    }

    @FXML
    private void createOnEnter() {
        create();
    }

    private void login() {
        new Thread(() -> {
            if (loginUsernameField.getText().equals("") && loginPasswordField.getText().equals("")) {
                logIn(new UserProfile(), true);
            } else {
                logIn(new UserProfile(loginUsernameField.getText(), loginPasswordField.getText()), true);
            }
        }).start();
    }

    private void create() {
        //log in with him right away
        UserProfile newProfile = UserProfile.createOCProfile(createUsernameField.getText(), createPasswordField.getText(), createEmailField.getText());
        newProfile.createDownloadsDir();
        if (newProfile != null) {//&& email valid
            logIn(newProfile, false);
        } else {
            lblCreate.setText("Username already in use");
        }
    }
}
