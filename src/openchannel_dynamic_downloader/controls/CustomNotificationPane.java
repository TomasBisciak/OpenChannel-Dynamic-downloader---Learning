/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.NotificationPane;

/**
 *
 * @author tomas
 */
public final class CustomNotificationPane extends BorderPane {

    private NotificationPane notificationPane;
    private Pane pane;

    public CustomNotificationPane() {
        initPane();
    }

    /**
     * Shows notification pane
     * @param message Text that shoud be displayed
     * @param milisec Miliseconds of how long shoud be notification displayed
     */
    public void showNotification(String message, long milisec) {
        animateMove(true, milisec);
        if (notificationPane.isShowing()) {
            notificationPane.hide();
            notificationPane.show(message);
        } else {
            notificationPane.show(message);
        }

    }
    //TODO optimize a bit and update ui on proper thread
    private void animateMove(boolean isCollapsed, long milisec) {
        Thread anim = new Thread(() -> {
            int MAX_VAL = 38;
            int MIN_VAL = 0;
            int value = isCollapsed ? MIN_VAL : MAX_VAL;
            boolean s = true;

            while (true) {
                try {
                    if (isCollapsed) {
                        if (s) {
                            Thread.sleep(190);
                            s = false;
                        } else {
                            Thread.sleep(20);
                        }

                        if (value < MAX_VAL) {
                            value += 2;
                            int temp=value;
                            Platform.runLater(() -> {
                                 pane.setMinHeight(temp);
                            });
                           
                        } else {
                            break;
                        }
                    } else {
                        if (value > MIN_VAL) {
                            value -= 2;
                           int temp=value;
                            Platform.runLater(() -> {
                                 pane.setMinHeight(temp);
                            });
                        } else {
                            break;
                        }
                        Thread.sleep(20);
                    }

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            if (milisec > 0) {
                try {
                    Thread.sleep(milisec);
                    animateMove(false,0);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        });
        anim.setName("CustomNotificationPane Animation thread.");
        anim.setDaemon(true);
        anim.setPriority(Thread.MIN_PRIORITY);
        anim.start();

    }

    private void initPane() {
        pane = new Pane();
        pane.setPrefHeight(0);

        notificationPane = new NotificationPane(pane);
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setShowFromTop(true);

        notificationPane.setOnMouseClicked((MouseEvent event) -> {
            notificationPane.hide();
            animateMove(false,0);
        });

        this.setCenter(notificationPane);

    }

}
