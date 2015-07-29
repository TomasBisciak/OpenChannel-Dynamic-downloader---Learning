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
import javafx.util.Duration;
import openchannel_dynamic_downloader.main.OpenChannel_Dynamic_Downloader;
import org.controlsfx.control.Notifications;

/**
 *
 * @author tomas
 */
public final class Notifier {

    public static enum NotifierType {
    
    INFORMATION,WARNING,ERROR,CUSTOM
    
}
    private Notifier(){};
    
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

   
    

}
