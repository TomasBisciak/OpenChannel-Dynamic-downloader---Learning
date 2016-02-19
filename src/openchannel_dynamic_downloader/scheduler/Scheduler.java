/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Singleton
 *
 * @author tomas
 */
public class Scheduler {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);
    public  boolean reReadFlags;

    private static final Scheduler instance = new Scheduler();
    private boolean[][] flags = new boolean[7][24];

    private Scheduler() {

    }
    public static Scheduler getInstance() {
        return instance;
    }
    
    public void setFlags(boolean[][] flags) {
        this.flags=flags;
    }
    public boolean[][] getFlags() {
        return flags;
    }

    public boolean getCurrentFlag() {
        return flags[LocalDate.now().getDayOfWeek().getValue()][LocalDateTime.now().getHour()];
    }

    public SimpleBooleanProperty getActiveProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public boolean getActive() {
        return active.get();
    }

}
