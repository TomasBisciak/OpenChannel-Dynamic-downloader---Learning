/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.application;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openchannel_dynamic_downloader.controllers.FxmlMainViewController;
import openchannel_dynamic_downloader.controls.EulaPane;
import openchannel_dynamic_downloader.controls.Notifier;
import openchannel_dynamic_downloader.controls.Notifier.NotifierType;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.ockeyHook.OCKeyHook;
import openchannel_dynamic_downloader.tray.Tray;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 *
 * @author tomas
 */
public class OpenChannel_Dynamic_Downloader extends Application {

    /**
     * Socket that this application uses and blocks any other instance that is requested to run/ there is a way to walk around this by specifying port for the application
     */
    private static ServerSocket socket;

    /**
     * Reusable primary stage for windows/ login screen/main view/eula
     */
    public static Stage primStage;
    /**
     * Currently used Application port
     */
    private static int appPort;
    /**
     * System tray object customized
     */
    private static Tray tray;
    /**
     * Flag if application is in tray/ main view closed
     */
    private static boolean isTray;
    /**
     * Checks if application is running Used mainly for notification after application close/tray enabled
     */
    private static boolean isRunning;

    /**
     * Application preferences/user preferences are profile specific see Info class for more information
     */
    private static final Preferences appPref = Preferences.userRoot().node("openchannel/app");

    /**
     * Main controller fxmlController/no need to expose/ probably
     */
    private static final FxmlMainViewController fxmlMvc = new FxmlMainViewController();

    public static void main(String[] args) {
        processArguments(args);
        launch(args);
    }

    /**
     *
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        showLoginWindow();
        executeOnStart();

    }
//todo how to nice up code  maybe all this shodu run on jfxat and start new ones to run off of it

    public static void showMainView() {
        if (!isRunning) {

            if (MainDataModel.getInstance().loginProfile.getPassword().equals("sa") && MainDataModel.getInstance().loginProfile.getUsername().equals("sa")) {
                Platform.runLater(() -> {
                    Notifier.showNotification(Notifier.NotifierType.INFORMATION, "Logged in",
                            "Logged in using default profile.", Pos.BOTTOM_RIGHT, null);
                });

            } else {
                Platform.runLater(() -> {
                    Notifier.showNotification(Notifier.NotifierType.INFORMATION, "Logged in",
                            "Logged as " + MainDataModel.getInstance().loginProfile.getUsername() + ".", Pos.BOTTOM_RIGHT, null);
                });
            }
            startOCGListener();//executing on standalone thread
            shutdownHook();
            isRunning = true;

        } else {

        }
        Platform.runLater(() -> {
            //primStage = stage;
            primStage.setTitle("OpenChannel " + Info.APP_VERSION + "\t Fast Lightweight Downloader");
            primStage.getIcons().addAll(new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI64)),
                    new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI32)),
                    new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI16)));

            primStage.setMaximized(true);
            try {
                //scene.getStylesheets().add("barchartsample/Chart.css");
                Scene scene = new Scene(loadMainPane());
                scene.getStylesheets().add("openchannel_dynamic_downloader/css/style.css");
                primStage.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            primStage.show();
        });
        if (SystemTray.isSupported()) {
            primStage.setOnCloseRequest((WindowEvent event) -> {
                tray.showMessage("Application still active.", TrayIcon.MessageType.INFO);

            });

            if (!isTray) {
                tray = new Tray(primStage, Info.Resource.OCPI16);
                isTray = true;
            }

        } else {
            System.out.println("System tray is not supported.");
        }
    }

    public static Tray getTray() {
        return tray;
    }

    //TODO create safe exit of application
    public static final void onApplicationClose() {

    }

    /**
     * Execute on application startup , recognizes if app FTR
     */
    private void executeOnStart() {

        if (appPref.getBoolean(Info.PreferenceData.PREF_APP_FIRST_TIME_RUN, true)) {
            firstTimeRunExecution();
            appPref.putBoolean(Info.PreferenceData.PREF_APP_FIRST_TIME_RUN, false);
        } else {
            System.out.println("Not first time run");
            DbUtil.printQueryResultSet(Info.Db.DB_MAIN_USERNAME, Info.Db.DB_MAIN_PASSWORD, "select * from users");
            //DbUtil.getUsersTableInfo();
        }

    }

    /**
     * First timeapplication execution
     */
    private void firstTimeRunExecution() {

        DbUtil.createUsersTable();
        Platform.runLater(() -> {
            Notifier.showNotification(NotifierType.INFORMATION, "Welcome to Login screen",
                    "Please create user , or use default one with no credentials.", Pos.BOTTOM_RIGHT, null);
        });

    }

    /**
     * Loads applicaiton main frame pane, and first application view into dynamic pane.
     *
     * @return Main application frame pane "ScrollPane in this case".
     * @throws IOException If path to fxml is invalid.
     */
    private static ScrollPane loadMainPane() throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setController(fxmlMvc);
        ScrollPane mainPane = (ScrollPane) loader.load(
                OpenChannel_Dynamic_Downloader.class
                .getResourceAsStream(Info.Resource.FXML_FILE_MAIN));

        return mainPane;
    }

    /**
     * Shows eula window , reuses primaryStage , user must accept elua to continue
     */
    public static void showEulaWindow() {
        Platform.runLater(() -> {
            primStage.setTitle("OpenChannel EULA");
            EulaPane eulaPane = new EulaPane();
            eulaPane.btn.setOnMouseClicked((MouseEvent event) -> {
                MainDataModel.getInstance().loginProfile.getPreferences().putBoolean(Info.PreferenceData.PREF_USER_FIRST_TIME_RUN, false);
                primStage.hide();
                showMainView();
                //after user accepted eula create tables for that user/ efectively database for them basicly//off fxap
                new Thread(() -> {
                    DbUtil.createTablesForUser();
                }).start();

            });
            primStage.setScene(new Scene(eulaPane));
            primStage.show();
        });

    }

    /**
     * Shows login window, able to login /create new profile.
     */
    private void showLoginWindow() {
        Platform.runLater(() -> {
            primStage = new Stage();
            primStage.setTitle("OpenChannel login");
            primStage
                    .getIcons().add(new Image(OpenChannel_Dynamic_Downloader.class
                                    .getResourceAsStream(Info.Resource.OCPI)));

            try {
                primStage.setScene(new Scene((BorderPane) FXMLLoader.load(OpenChannel_Dynamic_Downloader.class.getResource(Info.Resource.FXML_FILE_LOGIN))));

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            primStage.show();
        });
    }

    /**
     *
     * @param args passed into application
     */
    public static void processArguments(String[] args) {
        //holds which values were sucessfully parsed/used
        boolean[] paramFlags = new boolean[2];
        if (args.length != 0) {

            //switch case to check parameters
            for (int i = 0; i < args.length; i += 2) {

                switch (args[i]) {
                    case "-p": {
                        try {
                            if (!occupyPort(Integer.valueOf(args[i + 1]))) {
                                System.out.println("Port" + Integer.valueOf(args[i + 1]) + " cannot be occupied by application");
                                continue;
                            }
                            paramFlags[0] = true;
                        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                            System.err.println("Argument is not a valid number/argument not found");
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case "": {

                        break;
                    }
                    default: {
                        System.out.println("Invalid parameter \" " + args[i] + " \",use default execution? Y/N:");
                        if (new Scanner(System.in).nextLine().equalsIgnoreCase("y")) {

                        }
                    }

                }

            }

        } else {
            //use default values
            for (int i = 0; i < paramFlags.length; i++) {
                switch (i) {
                    case 0: {
                        if (!paramFlags[i]) {
                            occupyPort(Info.DEFAULT_APP_PORT);
                        }
                        break;
                    }
                    case 1: {

                        break;
                    }

                }

            }

        }

    }

    /**
     * Opens a ServerSocket for OpenChannel aplication making sure that only one instance is running at the time, if not specified otherwise by providing specified ports that differ from each other and fall within range.
     *
     * @param port port to be occupied by application valid range 49152-65535
     */
    private static boolean occupyPort(int port) {

        if (!(port >= 49152 && port <= 65535)) { //(IANA) suggested range
            System.err.println("Not a valid port number.");
            return false;
        }
        try {
            socket = new ServerSocket(port, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
            appPort = port;
        } catch (IOException ex) {
            System.err.println("Application already running/Port occupied by a process.");
            ex.printStackTrace();
            //TODO popup inform user
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
            System.exit(1);
        }
        return false;
    }

    private static void startOCGListener() {
        //too heavy to fight for resource//might block if not executed on this thread
        new Thread(() -> {
            try {
                GlobalScreen.registerNativeHook();
                System.out.println("Hook registered");
            } catch (NativeHookException ex) {
                System.out.println(ex.getMessage());
                //TODO notify user that globalkey listener doesnt work.
            }

            System.out.println("Hook state: " + GlobalScreen.isNativeHookRegistered());
            GlobalScreen.addNativeKeyListener(new OCKeyHook());
            //DISABLE LOGGIN FOR HOOK
            Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
        }).start();

    }

    private static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                //execute before shutdown
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                Logger.getLogger(OpenChannel_Dynamic_Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }));
    }

    public static final int getAppPort() {
        return appPort;
    }

}
