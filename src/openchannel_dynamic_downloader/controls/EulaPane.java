/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;

/**
 *
 * @author tomas
 */
public class EulaPane extends BorderPane {

    private TextArea textArea;
    public Button btn;

    public EulaPane() {
        textArea=new TextArea("test test");
        textArea.setWrapText(true);
        textArea.setEditable(false);
        this.setCenter(textArea);
        btn=new Button();
   
        btn.setText("I Agree");
        btn.setTextFill(Paint.valueOf("white"));
        btn.setStyle("-fx-background-radius:10;-fx-background-color:#3498db");
        this.setBottom(btn);
     btn.setAlignment(Pos.CENTER);
    }

}
