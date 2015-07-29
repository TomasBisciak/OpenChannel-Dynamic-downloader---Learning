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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import openchannel_dynamic_downloader.security.UserProfile;

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
    
    private void hookListeners(){
        
         createBtn.setOnMouseClicked((MouseEvent event) -> {
            UserProfile newProfile = UserProfile.createOCProfile(loginUsernameField.getText(), loginPasswordField.getText());
            if (newProfile != null) {

            } else {
                lblLogin.setText("Incorrect credentials");
                
            }
        });
         
         
    }
    
    
    

}
