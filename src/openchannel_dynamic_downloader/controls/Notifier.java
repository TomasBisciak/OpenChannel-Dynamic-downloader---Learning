/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import openchannel_dynamic_downloader.application.OpenChannel_Dynamic_Downloader;
import org.controlsfx.control.Notifications;

/**
 *
 * @author tomas
 */
public final class Notifier {

    public static enum NotifierType {

        INFORMATION, WARNING, ERROR, CUSTOM

    }

    private Notifier() {
    }

    
    /**
     * Shows notification
     * 
     * @param notifierType Type of the notification INFORMATION, WARNING, ERROR, CUSTOM
     * @param title Title of the notification
     * @param text Text to be displayed
     * @param position Position of the notification on the screen/stage
     * @param eh event handler that shoud be used/ onAction
     * @param imgUrl Graphic of the notification
     */
    public static void showNotification(NotifierType notifierType, String title, String text, Pos position, EventHandler<ActionEvent> eh, String... imgUrl) {
        Node graphic = null;
        switch (notifierType) {
            case INFORMATION:
                break;
            case WARNING:
                break;
            case ERROR:
                break;
            case CUSTOM:
                graphic = new ImageView(imgUrl.length < 1 ? null : imgUrl[0]);
        }
        //int ts=text.toCharArray().length;
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(text)
                .graphic(graphic)
                .hideAfter(Duration.seconds(8))
                .position(position)
                .onAction(eh == null ? new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        System.out.println("Notification clicked on!");
                    }
                } : eh);
        notificationBuilder.owner(OpenChannel_Dynamic_Downloader.primStage);
        notificationBuilder.hideCloseButton();
        notificationBuilder.darkStyle();
        switch (notifierType) {
            case WARNING:
                notificationBuilder.showWarning();
                break;
            case INFORMATION:
                notificationBuilder.showInformation();
                break;
            case ERROR:
                notificationBuilder.showError();
                break;
            default:
                notificationBuilder.show();
        }
    }

    /**
     * 
     * @param notifierType
     * @param title
     * @param text
     * @param position
     * @param owner
     * @param eh
     * @param imgUrl 
     */
    public static void showNotification(NotifierType notifierType, String title, String text, Pos position, Stage owner, EventHandler<ActionEvent> eh, String... imgUrl) {
        Node graphic = null;
        switch (notifierType) {
            case INFORMATION:
                break;
            case WARNING:
                break;
            case ERROR:
                break;
            case CUSTOM:
                graphic = new ImageView(imgUrl.length < 1 ? null : imgUrl[0]);
        }
        //int ts=text.toCharArray().length;
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(text)
                .graphic(graphic)
                .hideAfter(Duration.seconds(8))
                .position(position)
                .onAction(eh == null ? new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        System.out.println("Notification clicked on!");
                    }
                } : eh);
        notificationBuilder.owner(owner);
        notificationBuilder.hideCloseButton();
        notificationBuilder.darkStyle();
        switch (notifierType) {
            case WARNING:
                notificationBuilder.showWarning();
                break;
            case INFORMATION:
                notificationBuilder.showInformation();
                break;
            case ERROR:
                notificationBuilder.showError();
                break;
            default:
                notificationBuilder.show();
        }
    }

}
