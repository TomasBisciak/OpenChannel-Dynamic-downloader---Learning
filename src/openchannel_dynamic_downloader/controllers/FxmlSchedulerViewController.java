/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 *
 * @author tomas
 */
public class FxmlSchedulerViewController implements Initializable {

    @FXML
    private GridPane schedulerGrid;

    private final String COLOR_ACTIVE_HEX = "3498db";
    private final String COLOR_INACTIVE_HEX = "9b59b6";

    private static boolean[][] schedulerFlags = new boolean[7][24];
    
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set all inactive
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                ToggleButton tb = new ToggleButton();
                tb.setTooltip(new Tooltip(c + "h-" + (c + 1) + "h"));
                tb.setOnMouseClicked((MouseEvent event) -> {
                    if (tb.isSelected()) {
                        tb.setStyle("-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_ACTIVE_HEX);
                        schedulerFlags[schedulerGrid.getRowIndex(tb)][schedulerGrid.getColumnIndex(tb)] = true;
                    } else {
                        schedulerFlags[schedulerGrid.getRowIndex(tb)][schedulerGrid.getColumnIndex(tb)] = false;
                        tb.setStyle("-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_INACTIVE_HEX);
                    }
                });
                tb.setPrefWidth(30);
                schedulerGrid.add(tb, c, r);
                tb.setStyle("-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_INACTIVE_HEX);
            }

        }

    }

    public static final boolean[][] getSchedulerFlags() {
        return schedulerFlags;
    }

    private void setAllSchedulerFlags(boolean bool) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                schedulerFlags[r][c]=bool;
            }
        }
    }
    
    

    public FxmlSchedulerViewController() {

    }

    private ToggleButton getToggleByRowColumnIndex(final int row, final int column) {

        ObservableList<Node> childrens = schedulerGrid.getChildren();
        for (Node node : childrens) {
            try {
                if (schedulerGrid.getRowIndex(node) == row && schedulerGrid.getColumnIndex(node) == column) {
                    return (ToggleButton) node;
                }
            } catch (Exception e) {
                System.out.println("wtf");
            }
        }
        return null;
    }

}
