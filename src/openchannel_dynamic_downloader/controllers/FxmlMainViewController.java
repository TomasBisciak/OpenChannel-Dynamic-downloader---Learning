/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.controllers.FxmlDownloadsViewController.DownloadsFilter;
import openchannel_dynamic_downloader.controls.CustomAutoCompleteSearchDownloadsTextField;
import openchannel_dynamic_downloader.controls.CustomNotificationPane;
import openchannel_dynamic_downloader.controls.Notifier;
import openchannel_dynamic_downloader.downloader.DownloadTask;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import openchannel_dynamic_downloader.downloader.Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.scheduler.Scheduler;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.statistics.DailyStatTable;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.FileUtils;
import openchannel_dynamic_downloader.utils.Info;
import openchannel_dynamic_downloader.utils.MiscUtils;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.MaskerPane;
import org.jnativehook.keyboard.NativeKeyEvent;

/**
 *
 * @author tomas
 */
public class FxmlMainViewController implements Initializable {

    @FXML
    private ScrollPane dynamicNode;

    private ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private ToggleButton prefBtn;
    @FXML
    private ToggleButton allMenuBtn;
    @FXML
    private ToggleButton downloadingMenuBtn;
    @FXML
    private ToggleButton completedMenuBtn;
    @FXML
    private ToggleButton inactiveMenuBtn;
    @FXML
    private WebView webViewConnection;

    private ToggleButton[] downloadsMenuBtns;

    @FXML
    private BorderPane titledStatHolder;

    @FXML
    private ToggleButton schedulerMenuBtn;
    @FXML
    private ToggleButton activateMenuBtn;
    @FXML
    private ToggleButton automatedToggleButton;
    @FXML
    private ToggleButton databaseToggleButton;

    @FXML
    public RadioButton rbtnDiskSpace;//TODO not used as fxml part bud normal button no need for annotation
    @FXML
    private Label infoPanelThreadCountLabel;

    @FXML
    private SplitPane splitPaneMainView;

    @FXML
    private Menu mLog;
    @FXML
    private MenuItem midf;
    @FXML
    private MenuItem cpmi;
    @FXML
    private VBox menuHolder;//to hold learning text filed

    @FXML
    private TextField downFileTxtField;
    @FXML
    private Button downBtn;
    @FXML
    private TextField dirTextField;
    @FXML
    private Button browseBtn;
    @FXML
    private TextField nameOfTextField;
    @FXML
    private Button dirDefBtn;
    @FXML
    private Button nameDefBtn;
    @FXML
    private VBox notifPaneHolder;
    @FXML
    private TextField numOfConTxtField;
    @FXML
    private Label segmentedLbl;
    @FXML
    private Button numOfConDefBtn;
    @FXML
    private Label appPortLbl;
    @FXML
    private Menu outdatedMenu;
    @FXML
    private Button btnExpandMinimize;
    @FXML
    private ToggleButton premiumBtn;
    @FXML
    private ToggleButton tutorialBtn;
    @FXML
    private ToggleButton cloudBtn;
    @FXML
    private BorderPane graphHolder;
    @FXML
    private ProgressBar totalDownloadBar;
    @FXML
    private Label progressPercentLbl;
    @FXML
    private Label avgSpeedLbl;
    @FXML
    private Label lblAllocMem;
    @FXML
    private Label lblSessionData;
    @FXML
    private Label lblTodayDown;
    @FXML
    private Label lblTotalDown;
    @FXML
    private HBox secondMenuHolder;

    private static CustomNotificationPane notifPane;

    private static volatile boolean isOneTimeInit;

    private static CustomAutoCompleteSearchDownloadsTextField autoCompleteTxtField;

    private boolean dirFlag;
    private boolean downloadUrlFlag;
    private boolean nameFlag;
    private boolean connFlag = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        allMenuBtn.setToggleGroup(toggleGroup);
        downloadingMenuBtn.setToggleGroup(toggleGroup);
        completedMenuBtn.setToggleGroup(toggleGroup);
        inactiveMenuBtn.setToggleGroup(toggleGroup);
        schedulerMenuBtn.setToggleGroup(toggleGroup);
        databaseToggleButton.setToggleGroup(toggleGroup);
        prefBtn.setToggleGroup(toggleGroup);
        premiumBtn.setToggleGroup(toggleGroup);
        tutorialBtn.setToggleGroup(toggleGroup);
        cloudBtn.setToggleGroup(toggleGroup);
        downloadsMenuBtns = new ToggleButton[]{allMenuBtn, downloadingMenuBtn,
            completedMenuBtn, inactiveMenuBtn};

        activateMenuBtn.selectedProperty().addListener((v, oldVal, newVal) -> {

            if (activateMenuBtn.isSelected()) {
                activateMenuBtn.setStyle("-fx-background-radius:0;-fx-background-color:#1abc9c;");
                activateMenuBtn.setText("Activated");
                activateMenuBtn.setTextFill(Paint.valueOf("white"));

            } else {
                activateMenuBtn.setStyle("-fx-background-radius:0;");
                activateMenuBtn.setText("Activate");
                activateMenuBtn.setTextFill(Paint.valueOf("black"));

            }
            Scheduler.getInstance().setActive(newVal);

        });
        activateMenuBtn.selectedProperty().setValue(Scheduler.getInstance().getActiveProperty().getValue());

        premiumBtn.selectedProperty().addListener((v, oldVal, newVal) -> {

            if (premiumBtn.isSelected()) {// #8e44ad #6E2D8A
                premiumBtn.setStyle("-fx-background-radius:0;-fx-background-color:#6E2D8A;");
            } else {
                premiumBtn.setStyle("-fx-background-radius:0;-fx-background-color:#8e44ad;");
            }

        });
        tutorialBtn.selectedProperty().addListener((v, oldVal, newVal) -> {

            if (tutorialBtn.isSelected()) {
                tutorialBtn.setStyle("-fx-background-radius:0;-fx-background-color:#B36017;");
            } else {
                tutorialBtn.setStyle("-fx-background-radius:0;-fx-background-color:#e67e22;");
            }
        });
        cloudBtn.selectedProperty().addListener((v, oldVal, newVal) -> {

            if (cloudBtn.isSelected()) {
                cloudBtn.setStyle("-fx-background-radius:0;-fx-background-color:#176191;");
            } else {
                cloudBtn.setStyle("-fx-background-radius:0;-fx-background-color: #2980b9;");
            }
        });

        System.out.println("Calling initialize MainViewController");
        //might be in trycatch exception when window closed.
        if (!isOneTimeInit) {
            System.out.println("OneTimeInit");
            @SuppressWarnings("SleepWhileInLoop")
            Thread infoPanelThread = new Thread(() -> {
                while (true) {

                    try {
                        //convert to mb
                        Platform.runLater(() -> {
                            //TODO change logic to show mb if small
                            //optimize
                            float diskSpace = FileUtils.getAllDiskSpace();
                            float usable = FileUtils.getAllUsableDiskSpace();

                            //TODO MUST OPTIMIZE!.
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

                            //check number of threads
                            infoPanelThreadCountLabel.setText("Threads:" + (Thread.activeCount()));
                            lblAllocMem.setText("Allocated Memory " + (Runtime.getRuntime().totalMemory() / 1024) / 1024 + " MB");

                            //other actions
                        });

                        if (!rbtnDiskSpace.isSelected()) {
                            rbtnDiskSpace.setSelected(true);
                        }
                        //refreshrate might drop it down a bit to 1 second maybe.
                        Thread.sleep(5000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            infoPanelThread.setName("Info panel thread");
            infoPanelThread.setPriority(Thread.MIN_PRIORITY);
            infoPanelThread.setDaemon(true);
            infoPanelThread.start();
            isOneTimeInit = true;

            notifPane = new CustomNotificationPane();
            notifPaneHolder.getChildren().add(notifPane);

        }

        autoCompleteTxtField = new CustomAutoCompleteSearchDownloadsTextField();
        autoCompleteTxtField.setPromptText("Search your downloads folder");
        secondMenuHolder.getChildren().add(0, autoCompleteTxtField);
        autoCompleteTxtField.setStyle("-fx-background-radius:40;");
        mLog.setText("Logged as : " + MainDataModel.getInstance().loginProfile.getUsername());

        // updateSearchField(new String[]{"shit", "shizzle"});//TODO remove jsut test
        //down text field
        downFileTxtField.textProperty().addListener((v, oldValue, newValue) -> {//todo improve
            try {
                if (Downloader.validateStringDownloadUrl(downFileTxtField.getText())) {
                    downloadUrlFlag = true;
                    downFileTxtField.setStyle("");
                    downFileTxtField.setTooltip(null);

                } else {
                    downFileTxtField.setStyle("-fx-border-color:red;");
                    downFileTxtField.setTooltip(new Tooltip("Url is not valid"));
                    downloadUrlFlag = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                downloadUrlFlag = false;
                downFileTxtField.setTooltip(new Tooltip("Incorrect url"));
                downFileTxtField.setStyle("-fx-border-color:red;");
            }
        });

        //dir text field
        dirTextField.textProperty().addListener((v, oldValue, newValue) -> {
            try {
                if (Files.isDirectory(Paths.get(newValue))) {
                    dirFlag = true;
                    dirTextField.setStyle("");
                    dirTextField.setTooltip(null);
                } else {
                    dirFlag = false;
                    dirTextField.setStyle("-fx-border-color:red;");
                    dirTextField.setTooltip(new Tooltip("Not directory"));

                }
            } catch (Exception e) {
                e.printStackTrace();
                dirFlag = false;
                dirTextField.setTooltip(new Tooltip("Incorrect uri"));
                dirTextField.setStyle("-fx-border-color:red;");

            }

        });
        dirTextField.setText(MainDataModel.getInstance().loginProfile.getDownloadsDir());

        //file name text field 
        nameOfTextField.textProperty().addListener((v, oldValue, newValue) -> {
            try {
                Files.createDirectories(Paths.get(Info.OC_TEMP_DIR));//TODO optimize
                Files.createFile(Paths.get(Info.OC_TEMP_DIR + newValue));
                Files.delete(Paths.get(Info.OC_TEMP_DIR + newValue));
                nameFlag = true;
                nameOfTextField.setStyle("");
                System.out.println("Valid file name");

            } catch (Exception ex) {
                nameFlag = false;
                nameOfTextField.setStyle("-fx-border-color:red;");
                //ex.printStackTrace();
            }

        });

        browseBtn.setOnMouseClicked((MouseEvent event) -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("OpenChannel Downloads directory:");
            File defaultDirectory = new File(MainDataModel.getInstance().loginProfile.getDownloadsDir());
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(OpenChannel_Dynamic_Downloader.primStage);
            try {
                dirTextField.setText(selectedDirectory.getPath());
            } catch (NullPointerException e) {
                System.out.println("No dir selected");
            }

        });

        numOfConTxtField.textProperty().addListener((v, oldValue, newValue) -> {
            try {
                if (Integer.parseInt(newValue) >= 1) {
                    if (Integer.parseInt(newValue) == 1) {
                        segmentedLbl.setVisible(false);
                    } else {
                        segmentedLbl.setVisible(true);
                    }
                    connFlag = true;
                    numOfConTxtField.setStyle("");
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                connFlag = false;
                numOfConTxtField.setStyle("-fx-border-color:red;");
                segmentedLbl.setVisible(false);
                ex.printStackTrace();
            }

        });
        numOfConDefBtn.setOnMouseClicked((MouseEvent event) -> {
            numOfConTxtField.setText(String.valueOf(MainDataModel.getInstance().loginProfile.getNumOfConnectionsPerDownload()));
        });

        splitPaneMainView.getDividers().get(0).positionProperty().addListener((val, oldVal, newVal) -> {
            if ((double) newVal <= 0.63) {
                btnExpandMinimize.setText("Minimize");
            } else {
                btnExpandMinimize.setText("Expand");

            }
        });

        WebEngine webEngine = webViewConnection.getEngine();
        webEngine.load("http://testmy.net/");
        /*
         webEngine.load(
         getClass().getResource("/openchannel_dynamic_downloader/resources/html/speedTest.html").toExternalForm());
         */
        appPortLbl.setText(
                "Application port:" + OpenChannel_Dynamic_Downloader.getAppPort());

        totalDownloadBar.progressProperty().bind(MainDataModel.totalDownValProperty());
        totalDownloadBar.progressProperty().addListener((val, oldVal, newVal) -> {
            progressPercentLbl.setText(String.valueOf((double) newVal * 100) + "%");
        });

        lblSessionData.textProperty().bind(MainDataModel.downloadedBytesSessionStringProperty());
        lblTotalDown.textProperty().bind(MainDataModel.downloadedBytesTotalStringProperty());
        lblTodayDown.textProperty().bind(MainDataModel.downloadedBytesTodayStringProperty());

        initStatistics();

        // HIDDEN PANE CONTROL-------------------------------
        BorderPane top = new BorderPane();
        BorderPane bottom = new BorderPane();
        BorderPane left = new BorderPane();
        BorderPane right = new BorderPane();
        Label thisIsTestLabel = new Label();
        thisIsTestLabel.setText("HEY , SEE ME? COOL");
        thisIsTestLabel.setStyle("-fx-background-color:GRAY");
        right.setCenter(thisIsTestLabel);
        HiddenSidesPane hiddenPane = new HiddenSidesPane(dynamicNode, top, right, bottom, left);
        splitPaneMainView.getItems().set(0, hiddenPane);
        // HIDDEN PANE CONTROL-------------------------------

    }// END INITIALIZE
    
    
    
    

    public ToggleButton getActivateSchedulerMenuBtn() {
        return activateMenuBtn;
    }

    @FXML
    private ToggleButton toggleBtnSpeed;
    @FXML
    private ToggleButton toggleButtonDownFreq;
    @FXML
    private ToggleButton toggleBtnDataDownloaded;
    @FXML
    private ToggleButton toggleButtonState;
    @FXML
    private ToggleButton toggleButtonFileType;

    private ToggleGroup tgGraphButtonsGroup;

    // CHARTING DONE HERE------------------------
    //Might change access to data here etc .
    @FXML
    private HBox hBoxGraphHolderMenuRight;

    private final CategoryAxis xAxisBcs = new CategoryAxis();
    private final NumberAxis yAxisBcs = new NumberAxis();
    private final BarChart<String, Number> stateChart = new BarChart<>(xAxisBcs, yAxisBcs);
    //gonna refresh every 10 seconds
    //create series for each state so i can fuckn make them colored for fucks sake fuck this shit fuck
    private static final XYChart.Series stateSeries = new XYChart.Series();

    private final static NumberAxis xAxisDS = new NumberAxis(1, 60, 1);
    private final static NumberAxis yAxisDS = new NumberAxis(0, 1000, 100);
    //  // XYChart.Series series = new XYChart.Series(data);
    public static final AreaChart<Number, Number> speedChart = new AreaChart<>(xAxisDS, yAxisDS);

    private final static NumberAxis xAxisDD = new NumberAxis(1, 60, 1);
    private final static NumberAxis yAxisDD = new NumberAxis(0, 1000, 100);
    public static final AreaChart<Number, Number> dataDownloadedChart = new AreaChart<>(xAxisDD, yAxisDD);

    private static final XYChart.Series speedSeriesSeconds = new XYChart.Series();//hold 60 units
    private static final XYChart.Series speedSeriesMinutes = new XYChart.Series();//hold 60 units
    private static final XYChart.Series speedSeriesHours = new XYChart.Series();//hold 24 units

    public static XYChart.Series getStateSeries() {
        return stateSeries;
    }

    //  public static final String[] STATE_STRINGS = {"Failed", "Paused", "Downloading",
    //   "Completed", "Cancelled", "Scheduled"};
    public static void addToStateSeries(int index, int amount) {
        //based on state add to series
        ((XYChart.Data) stateSeries.getData().get(index)).setYValue(amount);

        // stateSeries.getData().add(new XYChart.Data(state, amount));
    }

    /**
     * @return the speedSeriesSeconds
     */
    public static XYChart.Series getSpeedSeriesSeconds() {
        return speedSeriesSeconds;
    }

    /**
     * @return the speedSeriesMinutes
     */
    public static XYChart.Series getSpeedSeriesMinutes() {
        return speedSeriesMinutes;
    }

    /**
     * @return the speedSeriesHours
     */
    public static XYChart.Series getSpeedSeriesHours() {
        return speedSeriesHours;
    }

    private static void updateUpperYBound(double speedAdded) {
        if (speedAdded > ((NumberAxis) speedChart.getYAxis()).getUpperBound()) {
            ((NumberAxis) speedChart.getYAxis()).setUpperBound((int) ((((NumberAxis) speedChart.getYAxis()).
                    getUpperBound() / 100) * 50) + ((NumberAxis) speedChart.getYAxis()).getUpperBound());

            ((NumberAxis) speedChart.getYAxis()).setTickUnit((int) (((NumberAxis) speedChart.getYAxis()).
                    getUpperBound() / 100) * 10);

        }
    }

    //create datasets for all resolutions
    public static final void addGraphSeriesSpeedToSeconds(double speed) {
        updateUpperYBound(speed);
        if (getSpeedSeriesSeconds().getData().size() != 60) {

            getSpeedSeriesSeconds().getData().add(new XYChart.Data(getSpeedSeriesSeconds().getData().size() + 1, speed));

        } else {
            getSpeedSeriesSeconds().getData().remove(0);
            getSpeedSeriesSeconds().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });
            getSpeedSeriesSeconds().getData().add(new XYChart.Data(getSpeedSeriesSeconds().getData().size() + 1, speed));

        }
    }

    public static final void addGraphSeriesSpeedToMinutes(double speed) {
        updateUpperYBound(speed);
        if (getSpeedSeriesMinutes().getData().size() != 60) {
            getSpeedSeriesMinutes().getData().add(new XYChart.Data(getSpeedSeriesMinutes().getData().size() + 1, speed));
            updateUpperYBound(speed);
        } else {
            getSpeedSeriesMinutes().getData().remove(0);
            getSpeedSeriesMinutes().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });
            getSpeedSeriesMinutes().getData().add(new XYChart.Data(getSpeedSeriesMinutes().getData().size() + 1, speed));
        }
    }

    public static final void addGraphSeriesSpeedToHours(double speed) {
        updateUpperYBound(speed);
        //make sure i have smooth transition from seocnds/minutes
        if (getSpeedSeriesHours().getData().size() != 24) {

            getSpeedSeriesHours().getData().add(new XYChart.Data(getSpeedSeriesHours().getData().size() + 1, speed));
        } else {
            getSpeedSeriesHours().getData().remove(0);
            getSpeedSeriesHours().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });
            getSpeedSeriesHours().getData().add(new XYChart.Data(getSpeedSeriesHours().getData().size() + 1, speed));
        }
    }

    public static final int RESOLUTION_SECONDS = 0;
    public static final int RESOLUTION_MINUTES = 1;
    public static final int RESOLUTION_HOURS = 2;
    private static int activeResolution = 0;

    public static void setActiveResolution(int resolution) {
        if (resolution <= 2 && resolution >= 0) {
            activeResolution = resolution;
        } else {
            activeResolution = 0;
        }
    }
    private final ChoiceBox choiceBoxResolutionDS = new ChoiceBox();
    private final ChoiceBox choiceBoxResolutionDD = new ChoiceBox();
    private final Label resolutionLabel = new Label("Resolution");

    private final DailyStatTable dailyStatTable = new DailyStatTable();

    private void initStatistics() {
        //set by defualt , speed of downlaoding and size of data downloaded at current time ,  shows daily downloaded data , 
        //has to be created table in database to store statistics about this
        //TODO create table in database to hodl statistics
        hBoxGraphHolderMenuRight.setSpacing(5);
        //hBoxGraphHolderMenuRight.getChildren().add(0, resolutionLabel);

        tgGraphButtonsGroup = new ToggleGroup();
        toggleBtnSpeed.setToggleGroup(tgGraphButtonsGroup);
        toggleButtonDownFreq.setToggleGroup(tgGraphButtonsGroup);
        toggleBtnDataDownloaded.setToggleGroup(tgGraphButtonsGroup);
        toggleButtonFileType.setToggleGroup(tgGraphButtonsGroup);

        choiceBoxResolutionDS.getItems().addAll("Seconds", "Minutes", "Hours");
        choiceBoxResolutionDS.getSelectionModel().selectFirst();
        choiceBoxResolutionDD.getItems().addAll("Seconds", "Minutes", "Hours", "Days", "Weeks", "Months", "Years", "All time");
        choiceBoxResolutionDD.getSelectionModel().selectFirst();

        ((NumberAxis) speedChart.getXAxis()).setUpperBound(60);//startin with seconds

        choiceBoxResolutionDS.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            switch ((int) newValue) {
                case RESOLUTION_SECONDS: {
                    ((NumberAxis) speedChart.getXAxis()).setUpperBound(60);
                    speedChart.getData().removeAll(getSpeedSeriesHours(), getSpeedSeriesMinutes());
                    speedChart.getData().add(getSpeedSeriesSeconds());
                    activeResolution = RESOLUTION_SECONDS;
                    break;
                }
                case RESOLUTION_MINUTES: {
                    ((NumberAxis) speedChart.getXAxis()).setUpperBound(60);
                    speedChart.getData().removeAll(getSpeedSeriesHours(), getSpeedSeriesSeconds());
                    speedChart.getData().add(getSpeedSeriesMinutes());
                    activeResolution = RESOLUTION_MINUTES;
                    break;
                }
                case RESOLUTION_HOURS: {
                    ((NumberAxis) speedChart.getXAxis()).setUpperBound(24);
                    speedChart.getData().removeAll(getSpeedSeriesMinutes(), getSpeedSeriesSeconds());
                    speedChart.getData().add(getSpeedSeriesHours());
                    activeResolution = RESOLUTION_HOURS;
                    break;
                }
            }
        });

        toggleBtnSpeed.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxGraphHolderMenuRight.getChildren().addAll(resolutionLabel, choiceBoxResolutionDS);
                graphHolder.setCenter(speedChart);
            } else {
                hBoxGraphHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionDS);
                graphHolder.setCenter(null);
            }
        });

        toggleBtnDataDownloaded.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxGraphHolderMenuRight.getChildren().addAll(resolutionLabel, choiceBoxResolutionDD);
                graphHolder.setCenter(speedChart);
            } else {
                hBoxGraphHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionDD);
                graphHolder.setCenter(null);
            }
        });

        toggleButtonState.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                graphHolder.setLeft(stateChart);
            } else {
                graphHolder.setLeft(null);
            }
        });

        toggleButtonState.setSelected(true);

        stateSeries.getData().add(new XYChart.Data("Failed", 0));
        stateSeries.getData().add(new XYChart.Data("Paused", 0));
        stateSeries.getData().add(new XYChart.Data("Downloading", 0));
        stateSeries.getData().add(new XYChart.Data("Completed", 0));
        stateSeries.getData().add(new XYChart.Data("Cancelled", 0));
        stateSeries.getData().add(new XYChart.Data("Scheduled", 0));

        stateChart.getStylesheets().add("openchannel_dynamic_downloader/css/style.css");
        stateChart.getData().add(stateSeries);
        stateChart.setAnimated(false);
        stateChart.setLegendVisible(false);
        speedChart.getData().add(getSpeedSeriesSeconds());
        speedChart.setAnimated(false);
        speedChart.setLegendVisible(false);

        //initialize with this button selected by default
        toggleBtnSpeed.setSelected(true);

        //TITLED PANE UPPER HOLDER INITIALIZATION
        //add dailyStats loaded , from mainDataModel instance
        dailyStatTable.setItems(MainDataModel.getInstance().getDailyStats());
        titledStatHolder.setCenter(dailyStatTable);

    }

    @FXML
    private Button btnDetect;
    @FXML
    private TextField txtFieldDetect;

    public static int latestKeyPressed = Integer.MIN_VALUE;
    //TODO JE TO DOJEBANE
    @FXML
    @SuppressWarnings("SleepWhileInLoop")
    private void detect() {
        new Thread(() -> {
            while (latestKeyPressed == Integer.MIN_VALUE) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FxmlMainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Unknown keyCode: 0x-80000000
                if (!NativeKeyEvent.getKeyText(latestKeyPressed).equals("Unknown keyCode: 0x-80000000")) {
                    txtFieldDetect.setText(NativeKeyEvent.getKeyText(latestKeyPressed));

                }

                //set preferences
                //
            }
            latestKeyPressed = Integer.MIN_VALUE;

        }).start();
    }

    ///-----------------------------------------------------
    @FXML
    private void confirmDownloadTestFile() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    
                    Downloader.downloadFile(new URL("http://mirror.internode.on.net/pub/test/1meg.test"), "testFile" + i + ".test", dirTextField.getText(), 3, false, true);//returns boolean btw
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FxmlMainViewController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            } catch (MalformedURLException ex) {
                Logger.getLogger(FxmlMainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

    }

    @FXML
    private void confirmDownload() {

        if (downloadUrlFlag && dirFlag && nameFlag && connFlag) {
            // ex.printStackTrace();
         //   new Thread(() -> {
                try {
                    Downloader.downloadFile(new URL(downFileTxtField.getText()), nameOfTextField.getText(), dirTextField.getText(), Integer.valueOf(numOfConTxtField.getText()), true, true);//returns boolean btw
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FxmlMainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
         //   }).start();
            downloadUrlFlag = false;
            downFileTxtField.setText("");
            nameOfTextField.setText("");
            nameFlag = false;
        } else {
            System.out.println("DEBUG:BAD INPUT FOR DOWNLAOD FLAGS FALSE");
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
    public static final void showPaneNotification(String text, long milisec) {//might be graphic set as well as parameter there
        notifPane.showNotification(text, milisec);
    }

    // DOWNLOADS view has to be loaded with parameters
    @FXML
    private void setViewDownloads() {
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
        FxmlDownloadsViewController.loadFilter(DownloadsFilter.SHOW_EVERYTHING);
    }

    @FXML
    private void setViewDownloading() {
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
        FxmlDownloadsViewController.loadFilter(DownloadsFilter.SHOW_DOWNLOADING);
    }

    @FXML
    private void setViewCompleted() {
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
        FxmlDownloadsViewController.loadFilter(DownloadsFilter.SHOW_COMPLETED);
    }

    @FXML
    private void setViewInactive() {
        setView(getView(Info.Resource.FXML_FILE_DOWNLOADS));
        FxmlDownloadsViewController.loadFilter(DownloadsFilter.SHOW_INACTIVE);

    }

    @FXML
    private void setViewScheduler() {
        setView(getView(Info.Resource.FXML_FILE_SCHEDULER));
    }

    @FXML
    private void setViewDatabase() {
        setView(getView(Info.Resource.FXML_FILE_DATABASE));
    }

    @FXML
    private void setViewAutomatedDownload() {
        setView(getView(Info.Resource.FXML_FILE_AUTOMATED_DOWNLOAD));
    }

    @FXML
    private void setViewPreferences() {
        setView(getView(Info.Resource.FXML_FILE_PREF));
    }

    @FXML
    private void setViewTutorial() {
        setView(getView(Info.Resource.FXML_FILE_TUTORIAL));
    }

    @FXML
    private void setViewCloud() {
        setView(getView(Info.Resource.FXML_FILE_CLOUD));
    }

    @FXML
    private void setViewPremium() {
        setView(getView(Info.Resource.FXML_FILE_PREMIUM));
    }

    @FXML
    private void openDownFolder() {
        try {
            Desktop.getDesktop().open(new File(MainDataModel.getInstance().loginProfile.getDownloadsDir()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void showLic() {
        createLicPopup("OpenChannel Licence", "/openchannel_dynamic_downloader/resources/license/ocLicenseFile.txt").showAndWait();
    }

    @FXML
    private void incognitoOnEvent() {

    }

    //REF : https://community.oracle.com/thread/3677813?start=0&tstart=0 //TODO delete later
    @FXML
    private void showAboutView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.Resource.FXML_FILE_ABOUT));
            BorderPane root = (BorderPane) loader.load(
                    OpenChannel_Dynamic_Downloader.class
                    .getResourceAsStream(Info.Resource.FXML_FILE_ABOUT));
            //Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);

            stage.setTitle(
                    "OpenChannel About");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(FxmlMainViewController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void btnExpandMinimizeEvent() {
        if (btnExpandMinimize.getText().equals("Minimize")) {
            splitPaneMainView.setDividerPosition(0, 1);
        } else {
            splitPaneMainView.setDividerPosition(0, 0.63);
        }

    }

    private Stage createLicPopup(String title, String res) {
        Stage dialog = new Stage();
        dialog.setTitle("OpenChannel License");
        Pane parentRoot = new Pane();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setMinWidth(500);
        textArea.setMinHeight(600);
        textArea.setWrapText(true);
        textArea.setText(FileUtils.readResource(getClass().getResourceAsStream(res)));
        parentRoot.getChildren().add(textArea);
        Scene scene = new Scene(parentRoot);

        dialog.setScene(scene);
        dialog.setWidth(505);
        dialog.setHeight(630);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    @FXML
    private void changeUserPasswordAction() {
        Stage dialog = new Stage();
        BorderPane parent = new BorderPane();

        boolean validPassword;

        PasswordField passField1 = new PasswordField();
        Label lbl1 = new Label("New password");
        passField1.textProperty().addListener((v, oldValue, newValue) -> {

        });
        Label lbl2 = new Label("Repeat password");
        PasswordField passField2 = new PasswordField();
        passField2.textProperty().addListener((v, oldValue, newValue) -> {
            if (newValue.equals(passField1.getText())) {
                passField2.setStyle("");
            } else {
                passField2.setStyle("-fx-border-color:red");
            }

        });
        Label errorLbl = new Label();

        errorLbl.setTextFill(Paint.valueOf("red"));

        Button btn = new Button("Confirm");
        btn.setStyle("-fx-background-radius:0");

        btn.setOnAction((ActionEvent event) -> {
            if (passField1.getText().equals(passField2.getText())) {
                //TODO validate password
                if (UserProfile.validatePasswordString(passField2.getText())) {
                    DbUtil.changeUserPassword(passField2.getText());
                    Notifier.showNotification(Notifier.NotifierType.INFORMATION, "Password changed",
                            "Password for this profile has been changed.", Pos.BOTTOM_RIGHT, null);
                    //TODO close application notify user before closing application
                } else {
                    errorLbl.setText("Invalid password");
                }

            } else {
                errorLbl.setText("Passwords does not match");
            }

        });

        VBox vbox = new VBox(lbl1, passField1, lbl2, passField2, btn);
        vbox.setSpacing(10);
        parent.setCenter(vbox);

        Scene scene = new Scene(parent);
        dialog.setScene(scene);
        dialog.setWidth(400);
        dialog.setHeight(200);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    public static final void showLongProcessAction() {//TODO maybe implement

    }

    //todo probably remove
    public static final void updateSearchField(String[] possibilities) {
        autoCompleteTxtField.loadSuggestions(possibilities);
    }

    @FXML
    private void showInfo() {
        System.out.println("Show window with info current version,latest version, new features");
    }

    @FXML
    private void setDirDef() {
        dirTextField.setText(MainDataModel.getInstance().loginProfile.getDownloadsDir());
    }

    @FXML
    private void setNameDef() {
        if (downFileTxtField.getStyle().equals("")) {
            nameOfTextField.setText(Downloader.generateFileName(downFileTxtField.getText()));
        }

    }

}
