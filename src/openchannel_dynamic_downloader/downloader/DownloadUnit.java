/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.downloader;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author tomas
 */
public class DownloadUnit {

    private SimpleStringProperty id;
    private SimpleStringProperty name;
    private SimpleStringProperty size;
    private SimpleStringProperty source;
    private SimpleDoubleProperty progress;
    private SimpleDoubleProperty downloadSpeed;
    private SimpleStringProperty eta;
    private SimpleStringProperty added;
    private SimpleStringProperty completedOn;

    public static final int STATE_INACTIVE = -1;
    public static final int STATE_PAUSED = 0;
    public static final int STATE_ACTIVE = 1;//means downloading
    public static final int STATE_COMPLETED = 2;

    private volatile int state = STATE_INACTIVE;

    public DownloadUnit() {

        this.id = new SimpleStringProperty("test");
        this.name = new SimpleStringProperty("test");
        this.size = new SimpleStringProperty("test");
        this.source = new SimpleStringProperty("test");
        this.progress = new SimpleDoubleProperty(0);
        this.downloadSpeed = new SimpleDoubleProperty(0);
        this.eta = new SimpleStringProperty("test");
        this.added = new SimpleStringProperty("test");
        this.completedOn = new SimpleStringProperty("test");
    }

    //just for testing everything null;
    
    public DownloadUnit(String name, String size, String source) {

    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    //getters Setters
    private void getTestUnit() {

    }

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getSize() {
        return size.get();
    }

    public String getSource() {
        return source.get();
    }

    public Double getProgress() {
        return progress.get();
    }

    public double getDownloadSpeed() {
        return downloadSpeed.get();
    }

    public String getETA() {
        return eta.get();
    }

    public String getAdded() {
        return added.get();
    }

    public String getCompletedOn() {
        return completedOn.get();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state >= -1 && state <= 2) {
            this.state = state;
        } else {
            this.state = -1;
        }
    }

}
