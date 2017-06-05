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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import openchannel_dynamic_downloader.downloader.Downloader;
import openchannel_dynamic_downloader.scheduler.Scheduler;
import org.controlsfx.control.action.Action;

/**
 *
 * @author tomas
 */
public class FxmlSchedulerViewController implements Initializable {

    @FXML
    private GridPane schedulerGrid;
    @FXML
    private Button applyChangesBtn;
    @FXML
    private Label acclbl;
    @FXML
    private Button btnInactiveAll;
    @FXML
    private Button btnActiveAll;
    @FXML
    private Button btnMonday;
    @FXML
    private Button btnTuesday;
    @FXML
    private Button btnWednesday;
    @FXML
    private Button btnThursday;
    @FXML
    private Button btnFriday;
    @FXML
    private Button btnSaturday;
    @FXML
    private Button btnSunday;

    private Button[] dayButtons;
    @FXML
    private ComboBox<Integer> comboBoxActiveHourStart;
    @FXML
    private ComboBox<Integer> comboBoxActiveHourEnd;
    /**
     * Color constant for active scheduler cell
     */
    private final String COLOR_ACTIVE_HEX = "3498db";
    /**
     * Color constant for inactive scheduler cell
     */
    private final String COLOR_INACTIVE_HEX = "9b59b6";

    /**
     * Matrix of scheduler flags from a scheduler table
     */
    private static boolean[][] schedulerFlags = new boolean[7][24];
    private static final ToggleButton[][] toggles = new ToggleButton[7][24];

    private boolean isActivator = false;
    private boolean isEndActivator = false;

    //TODO OPTIMIZE AS MUCH AS POSSIBLE
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //set all inactive
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                //comboboxes of active hour range//only once
                if (r == 0) {
                    comboBoxActiveHourStart.getItems().add(c + 1);
                    comboBoxActiveHourEnd.getItems().add(c + 1);
                }

                ToggleButton tb = new ToggleButton();
                toggles[r][c] = tb;
                tb.setTooltip(new Tooltip(c + "h-" + (c + 1) + "h"));
                tb.selectedProperty().addListener((v, oldValue, newValue) -> {
                    if (newValue) {
                        tb.setStyle("-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_ACTIVE_HEX);
                        schedulerFlags[schedulerGrid.getRowIndex(tb)][schedulerGrid.getColumnIndex(tb)] = true;
                    } else {
                        schedulerFlags[schedulerGrid.getRowIndex(tb)][schedulerGrid.getColumnIndex(tb)] = false;
                        tb.setStyle("-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_INACTIVE_HEX);
                    }
                });

                dayButtons = new Button[]{btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday, btnSunday};

                for (int i = 0; i < dayButtons.length; i++) {
                    final int t = i;
                    dayButtons[i].setOnMouseClicked((Event event) -> {
                        setDayActivityOnOff(t);
                    });
                }

                tb.setOnMouseReleased((MouseEvent event) -> {

                    if (!isEndActivator) {
                        if (tb.isSelected()) {
                            isActivator = true;
                        } else {
                            isActivator = true;
                        }
                        isEndActivator = true;
                    } else {
                        isActivator = false;
                        isEndActivator = false;
                    }

                });

                tb.setOnMouseEntered((MouseEvent event) -> {
                    if (isActivator) {
                        tb.setSelected(true);
                    }
                });

                schedulerGrid.setOnMousePressed((MouseEvent event) -> {
                    System.out.println("Mouse pressed");
                });
                tb.setPrefWidth(
                        30);
                schedulerGrid.add(tb, c, r);

                tb.setStyle(
                        "-fx-border-color:#34495e;-fx-border-width:1;-fx-background-radius:0;-fx-background-color:#" + COLOR_INACTIVE_HEX);
            }

        }

        comboBoxActiveHourStart.getSelectionModel().selectFirst();
        comboBoxActiveHourEnd.getSelectionModel().selectFirst();
        //FIRST LOAD FLAGS BEFORE APPLICAITON END FROM PREFERENCES IF ANY ARE EXISTENT,
        //todo create here
        // ANYTHING AFTER FIRST EXECUTION IS LOADED FROM SHECULER.!
        loadFlags();//loads flags if any are loaded ,
        //add listeners ondo day buttons
    }

    private void loadFlags(boolean flags[][]) {
        schedulerFlags = flags;
        //go thru toggles and activate deactivate based on flags

        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                toggles[r][c].setSelected(schedulerFlags[r][c]);
            }
        }

    }

    //LOADS DEFAULT ONES only form scheduler
    private void loadFlags() {
        schedulerFlags = Scheduler.getInstance().getFlags();
        //go thru toggles and activate deactivate based on flags
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                toggles[r][c].setSelected(schedulerFlags[r][c]);
            }
        }
    }

    private void setDayActivityOnOff(int day) {

        boolean onOff = false;
        for (int hour = 0; hour < 24; hour++) {
            if (schedulerFlags[day][hour]) {
                onOff = false;
                break;
            } else {
                onOff = true;
            }
        }

        for (int hour = 0; hour < 24; hour++) {
            schedulerFlags[day][hour] = onOff;
            getToggleByRowColumnIndex(day, hour).setSelected(onOff);

        }

    }

    @FXML
    private void selectRange() {
        for (int d = 0, s = comboBoxActiveHourStart.getValue(), e = comboBoxActiveHourEnd.getValue(),a; d < 7; d++) {
            a=s;
            do {
                if(a==24){
                    a=0;
                    continue;
                }
                toggles[d][a].setSelected(true);
                a++;
            } while (a != e);
        }
    }

    @FXML
    private void activeAllOnEvent() {
        ObservableList<Node> childrens = schedulerGrid.getChildren();
        for (Node node : childrens) {
            try {
                ((ToggleButton) node).setSelected(true);
            } catch (ClassCastException ex) {
                System.out.println("debug-Problem with casting togglebutton , chill");
            }
        }
        setAllSchedulerFlags(true);
    }

    @FXML
    private void inactiveAllOnEvent() {
        ObservableList<Node> childrens = schedulerGrid.getChildren();
        for (Node node : childrens) {
            try {
                ((ToggleButton) node).setSelected(false);
            } catch (ClassCastException ex) {
                System.out.println("debug-PRoblem with casting togglebutton , chill");
            }
        }
        setAllSchedulerFlags(false);
    }

    public static final boolean[][] getSchedulerFlags() {
        return schedulerFlags;
    }

    /**
     * Sets all scheduler flags to bool
     *
     * @param bool scheduler state
     */
    private void setAllSchedulerFlags(boolean bool) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 24; c++) {
                schedulerFlags[r][c] = bool;
            }
        }
    }

    public FxmlSchedulerViewController() {

    }

    //not really necessery
    public ToggleButton getToggleByRowColumnIndex(int row, int column) {
        return toggles[row][column];
    }

    /**
     * Applys changes to scheduler / changes reReadFlags on downloader and all threads executing downlaod has to change theyr state
     */
    @FXML
    @SuppressWarnings("SleepWhileInLoop")
    private void setApplyChanges() {
        // FxmlMainViewController.showPaneNotification("HELLO CHANGES",2000);//todo REMOVE FROM HERE//just test
        Scheduler.getInstance().setFlags(schedulerFlags);
        Scheduler.getInstance().reReadFlags = true;
        FxmlMainViewController.showPaneNotification("Scheduler:Change applied", 2000);
    }

}
