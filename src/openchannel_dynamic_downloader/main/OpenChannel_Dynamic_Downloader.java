/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.main;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openchannel_dynamic_downloader.controllers.FxmlMainViewController;
import openchannel_dynamic_downloader.controls.Notifier;
import openchannel_dynamic_downloader.controls.Notifier.NotifierType;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.security.UserProfile;
import openchannel_dynamic_downloader.tray.Tray;
import openchannel_dynamic_downloader.utils.Info;

/**
 *
 * @author tomas
 */
public class OpenChannel_Dynamic_Downloader extends Application {

    private static ServerSocket socket;

    public static OpenChannel_Dynamic_Downloader odca;

    public static Stage primStage;
    private static int appPort;
    private static Tray tray;
    private static boolean isTray;
    private static boolean running;

    //NOT SURE IF NEEDED / MAYBE WILL BE BETTER TO SET CONTROLLER ON FXML
    public static final FxmlMainViewController fxmlMvc = new FxmlMainViewController();
    
    
    
    private static Path[] rootDirs;
    
    static{
        
    }

    /**
     * This method shoud run only once IF system tray is not supported on this machine ELSE multiple invocations might be executed
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        if (MainDataModel.loginProfile != null) {//logged in

            if (MainDataModel.loginProfile.getPassword().equals("") && MainDataModel.loginProfile.getUsername().equals("")) {
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

            primStage = stage;
            primStage.setTitle("OpenChannel " + Info.APP_VERSION);
            primStage.getIcons().add(new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI)));
            primStage.setMaximized(true);
            primStage.setScene(new Scene(loadMainPane()));

            primStage.show();

            if (SystemTray.isSupported()) {
                primStage.setOnCloseRequest((WindowEvent event) -> {
                    tray.showMessage("Application still active.", TrayIcon.MessageType.INFO);

                });

                if (!isTray) {
                    tray = new Tray(primStage, this, Info.Resource.OCPI);
                    isTray = true;
                }

            }
        } else {//not yet logged in
            showLoginWindow();
        }

        if (running) {
            executeOnStart();
            running = true;
        }
    }

    
    
    /**
     * TODO doc
     */
    private void executeOnStart() {

        if (Info.isFtr) {
            //BFXviewLoader.loadView("")//load welcome /tutorial shiz
            Platform.runLater(() -> {
                Notifier.showNotification(NotifierType.INFORMATION, "Welcome namehere",
                        "So this is first time you run OpenChannel.\nI will help you out by showing you tutorial.", Pos.BOTTOM_RIGHT, null);
            });
            // PlaySound.playSound(PlaySound.SOUND_DOWNLOAD_FINISHED);//test
        } else {

            Thread diskSpaceChecker = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            });
            diskSpaceChecker.setName("Disk space checker");
            diskSpaceChecker.setPriority(Thread.MIN_PRIORITY);

            //BFXviewLoader.loadView("/bitcompile/ecps/fxml/FxmlBrowseProfiles.fxml");//change to default page at loading time  
            //PlaySound.play(PlaySound.WELCOME_BACK);
        }

    }

    /**
     * Loads applicaiton main frame pane, and first application view into dynamic pane.
     *
     * @return Main application frame pane "ScrollPane in this case".
     * @throws IOException If path to fxml is invalid.
     */
    private ScrollPane loadMainPane() throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setController(fxmlMvc);
        ScrollPane mainPane = (ScrollPane) loader.load(
                getClass().getResourceAsStream(Info.Resource.FXML_FILE_MAIN));

        return mainPane;
    }

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        //argument processing might be reworked

        processArguments(args);
        //need to use application as object cause of login invocation of start
        odca = new OpenChannel_Dynamic_Downloader();
        odca.launch(args);
        //launch(args);

        //not sure if start is on fx or main thread
    }

    public static final void showLoginWindow() {
        primStage = new Stage();
        primStage.setTitle("OpenChannel login");
        primStage.getIcons().add(new Image(OpenChannel_Dynamic_Downloader.class.getResourceAsStream(Info.Resource.OCPI)));
        try {
            primStage.setScene(new Scene((BorderPane) FXMLLoader.load(OpenChannel_Dynamic_Downloader.class.getResource(Info.Resource.FXML_FILE_LOGIN))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        primStage.show();

    }

//maybe useless
    private UserProfile logInAs(String username, String password) {
        return null;
    }

    //might be reworked
    private static void processArguments(String[] args) {
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

    /**
     * Returns application port
     *
     * @return integer number that represent port that application occupy
     */
    public static int getApplicationPort() {
        return appPort;
    }

}
