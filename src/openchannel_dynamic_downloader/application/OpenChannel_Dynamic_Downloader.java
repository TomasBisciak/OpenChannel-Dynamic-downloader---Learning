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
import openchannel_dynamic_downloader.tray.Tray;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author tomas
 */
public class OpenChannel_Dynamic_Downloader extends Application {

    private static ServerSocket socket;

    public static Stage primStage;
    private static int appPort;
    private static Tray tray;
    private static boolean isTray;
    private static boolean isRunning;

    private static Preferences appPref = Preferences.userRoot().node("openchannel/app");

    //NOT SURE IF NEEDED / MAYBE WILL BE BETTER TO SET CONTROLLER ON FXML
    private static FxmlMainViewController fxmlMvc = new FxmlMainViewController();

    public static void main(String[] args) {
        processArguments(args);
        launch(args);
    }

    /**
     * This method shoud run only once
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        showLoginWindow();
        executeOnStart();

    }

    public static void showMainView() {
        if (!isRunning) {

            if (MainDataModel.loginProfile.getPassword().equals("sa") && MainDataModel.loginProfile.getUsername().equals("sa")) {
                Platform.runLater(() -> {
                    Notifier.showNotification(Notifier.NotifierType.INFORMATION, "Logged in",
                            "Logged in using default profile.", Pos.BOTTOM_RIGHT, null);
                });

            } else {
                Platform.runLater(() -> {
                    Notifier.showNotification(Notifier.NotifierType.INFORMATION, "Logged in",
                            "Logged as " + MainDataModel.loginProfile.getUsername() + ".", Pos.BOTTOM_RIGHT, null);
                });
            }

            isRunning = true;

        } else {

        }

        //primStage = stage;
        primStage.setTitle("OpenChannel " + Info.APP_VERSION);
        primStage.getIcons().add(new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI)));
        primStage.setMaximized(true);
        try {
            primStage.setScene(new Scene(loadMainPane()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        primStage.show();

        if (SystemTray.isSupported()) {
            primStage.setOnCloseRequest((WindowEvent event) -> {
                tray.showMessage("Application still active.", TrayIcon.MessageType.INFO);

            });

            if (!isTray) {
                tray = new Tray(primStage, Info.Resource.OCPI);
                isTray = true;
            }

        }
    }

    /**
     * TODO doc
     */
    private void executeOnStart() {

        if (appPref.getBoolean(Info.PreferenceData.PREF_APP_FIRST_TIME_RUN, true)) {
            firstTimeRunExecution();
            appPref.putBoolean(Info.PreferenceData.PREF_APP_FIRST_TIME_RUN, false);
        } else {
            System.out.println("Not first time run");
            DbUtil.getUsersTableInfo();
        }

    }

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

    public static void showEulaWindow() {
        Platform.runLater(() -> {
            primStage.setTitle("OpenChannel EULA");
            EulaPane eulaPane = new EulaPane();
            System.out.println("passed here");
            eulaPane.btn.setOnMouseClicked((MouseEvent event) -> {
                MainDataModel.loginProfile.getPreferences().putBoolean(Info.PreferenceData.PREF_USER_FIRST_TIME_RUN, false);
                primStage.hide();
                showMainView();

            });
            primStage.setScene(new Scene(eulaPane));
            primStage.show();
        });

    }

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

//might be reworked
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
     * Opens a ServerSocket for OpenChannel aplication making sure that only one instance is running at the time, if not specified otherwise by providing specified ports that differ from each other
     * and fall within range.
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

}
