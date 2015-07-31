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
import openchannel_dynamic_downloader.controls.Notifier;
import openchannel_dynamic_downloader.main.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.utils.Email;

/**
 *
 * @author tomas
 */
public class FxmlLoginViewController implements Initializable {

    @FXML
    TextField loginUsernameField;
    @FXML
    PasswordField loginPasswordField;
    @FXML
    TextField createUsernameField;
    @FXML
    TextField createEmailField;//optional
    @FXML
    TextField createPasswordField;

    @FXML
    Button loginBtn;
    @FXML
    Label lblLogin;
    @FXML
    Button createBtn;
    @FXML
    Label lblCreate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        hookListeners();

    }

    private boolean emailValidity = false;

    @SuppressWarnings("static-access")
    private void hookListeners() {

        loginBtn.setOnMouseClicked((MouseEvent event) -> {
            UserProfile logProfile = new UserProfile(loginUsernameField.getText(), loginPasswordField.getText());
            if (logProfile.validate()) {
                logProfile.login();
                OpenChannel_Dynamic_Downloader.primStage.hide();
                try {//invoke start method and lats do it again
                    OpenChannel_Dynamic_Downloader.odca.start(OpenChannel_Dynamic_Downloader.primStage);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                lblLogin.setText("incorrect credentials");
            }

        });

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
            //log in with him right away
            UserProfile newProfile = UserProfile.createOCProfile(createUsernameField.getText(), createPasswordField.getText());
            if (newProfile != null && emailValidity) {

            } else {
                lblCreate.setText("Username already in use");

            }
        });

    }

}
