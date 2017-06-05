/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.tray;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import openchannel_dynamic_downloader.scheduler.Scheduler;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 *
 * @author tomas
 */
public class Tray {

    private Stage stage;
    private TrayIcon icon;
    private MenuItem schedulerEnableDisable;

    public MenuItem getSchedulerEnableDisable() {
        return schedulerEnableDisable;
    }

    public Tray(Stage stage, String imagePath) {

        this.stage = stage;
        hookTray(imagePath);
    }

    public void showMessage(String message, TrayIcon.MessageType TYPE) {
        icon.displayMessage("OpenChannel", message, TYPE);
    }

    private void hookTray(String image) {

        try {
            icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream(image)), "OpenChannel " + Info.APP_VERSION,
                    createPopupMenu());

            icon.addActionListener((ActionEvent e) -> {
                Platform.runLater(() -> {
                    stage.setIconified(false);//nevim co presne naco som t osem dal
                });

            });
            //icon.
            SystemTray.getSystemTray().add(icon);

            //icon.displayMessage("OpenChannel", "Listener enabled.",
            //       TrayIcon.MessageType.INFO);
            Platform.setImplicitExit(false);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }

    }

    public PopupMenu createPopupMenu() throws HeadlessException {
        PopupMenu menu = new PopupMenu();

        MenuItem exit = new MenuItem("Exit");
        MenuItem showWindow = new MenuItem("Show window");
        CheckboxMenuItem cbGameMode = new CheckboxMenuItem("Game Mode");
        Menu menuScheduler = new Menu("Scheduler");
        schedulerEnableDisable = new MenuItem("Enable");
        schedulerEnableDisable.addActionListener((ActionEvent e) -> {
            if (Scheduler.getInstance().getActiveProperty().getValue()) {
                Scheduler.getInstance().setActive(false);
                //TODO CHECK THIS
                try {//not sure if try catch has to be here , check later
                    Platform.runLater(() -> {
                        OpenChannel_Dynamic_Downloader.getFxmlMvc().getActivateSchedulerMenuBtn().selectedProperty().setValue(false);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
            } else {
                Scheduler.getInstance().setActive(true);
                //TODO CHECK THIS
                try {//not sure if try catch has to be here , check later
                    Platform.runLater(() -> {
                        OpenChannel_Dynamic_Downloader.getFxmlMvc().getActivateSchedulerMenuBtn().selectedProperty().setValue(true);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        menuScheduler.add(schedulerEnableDisable);
        Menu menuDownloads = new Menu("Downloads");

        showWindow.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                try {
                    OpenChannel_Dynamic_Downloader.showMainView();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        });

        exit.addActionListener((ActionEvent e) -> {
            try {
                GlobalScreen.unregisterNativeHook();
                //DbUtil.insertIntoDailyStats(1000l);
                System.exit(0);
            } catch (NativeHookException ex) {
                ex.printStackTrace();
            }
        });

        menu.add(showWindow);
        menu.add(cbGameMode);
        menu.add(menuScheduler);
        menu.add(menuDownloads);
        menu.add(exit);

        return menu;
    }

    public void setTrayImage(String imagePath) {
        try {
            icon.setImage(ImageIO.read(getClass().getResourceAsStream(imagePath)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
