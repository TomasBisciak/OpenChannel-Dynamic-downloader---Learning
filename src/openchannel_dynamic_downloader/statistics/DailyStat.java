/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.statistics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 *
 * @author Kofola
 */
public class DailyStat {

    //TODO create properties for work with tableView.
    private final SimpleLongProperty idDate;
    private final SimpleLongProperty bytesDownloaded;
    private final SimpleIntegerProperty numberOfDownloads;

    public DailyStat(long idDate, long bytesDownloaded, int numberOfDownloads) {
        this.idDate = new SimpleLongProperty(idDate);
        this.bytesDownloaded = new SimpleLongProperty(bytesDownloaded);
        this.numberOfDownloads = new SimpleIntegerProperty(numberOfDownloads);
    }

    public final void setIdDate(long id) {
        idDate.set(id);
    }
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static long createDateId(LocalDate localDate) {
        return Long.valueOf(localDate.format(formatter));

    }

    public final String getIdDateFormattedString() {
        String year = String.valueOf(idDate.getValue()).substring(0, 4);
        String month = String.valueOf(idDate.getValue()).substring(4, 6);
        String day = String.valueOf(idDate.getValue()).substring(6, 8);
        return year + "-" + month + "-" + day;
    }

    public final LocalDate getidDateFormattedDate() {
        String year = String.valueOf(idDate.getValue()).substring(0, 4);
        String month = String.valueOf(idDate.getValue()).substring(4, 6);
        String day = String.valueOf(idDate.getValue()).substring(6, 8);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(year + "-" + month + "-" + day, dtf);

    }

    public final long getIdDate() {
        return idDate.get();
    }

    public final SimpleLongProperty idDateProperty() {
        return idDate;
    }

    public final void setBytesDownloaded(long bytes) {
        bytesDownloaded.set(bytes);
    }

    public final long getBytesDownloaded() {
        return bytesDownloaded.get();
    }

    public final SimpleLongProperty bytesDownloadedProperty() {
        return bytesDownloaded;
    }

    public final void setNumberOfDownloads(int numOfDown) {
        numberOfDownloads.set(numOfDown);
    }

    public final long getNumberOfDownloads() {
        return numberOfDownloads.get();
    }

    public final SimpleIntegerProperty numberOfDownloadsProperty() {
        return numberOfDownloads;
    }

}
