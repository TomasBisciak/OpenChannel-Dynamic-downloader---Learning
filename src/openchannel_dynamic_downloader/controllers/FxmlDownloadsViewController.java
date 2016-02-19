/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import openchannel_dynamic_downloader.controls.CustomAutoCompleteSearchTableTextField;
import openchannel_dynamic_downloader.controls.OCTableView;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import openchannel_dynamic_downloader.downloader.Downloader;

/**
 *
 * @author tomas
 */
public class FxmlDownloadsViewController implements Initializable {

    @FXML
    private BorderPane holder;
    @FXML
    private Button removeBtn;
    @FXML
    private ToolBar toolBarLeft;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnPauseAll;
    @FXML
    private Button reDownloadBtn;
    @FXML
    private Button btnDownloadSpeed;

    private CustomAutoCompleteSearchTableTextField autoCompleteTxtField;

    String[] common = new String[]{"Failed", "Paused", "Downloading",
        "Completed", "Cancelled", "Scheduled", "-Today", "-Recent", "-Yesterday", "-This week",
        "-This month", "-Video", "-Audio", "-Data", "-Text", "-3D", "-Raster", "-Vector",
        "-Page Layout", "-Spreadsheet", "-Database", "-Executable", "-CAD", "-GIS", "-Web",
        "-Plugin", "-Font", "-System", "-Settings", "-Encoded", "-Compressed", "-Disk", "-Source", "-Torrent"};

    List<String> suggestions = new ArrayList<>();
    private static OCTableView ocTableView;

    private static ObservableList<DownloadUnit> downloadUnitViewCache;//master data

    private static DownloadsFilter viewFilter = DownloadsFilter.SHOW_EVERYTHING;

    private static FilteredList<DownloadUnit> filteredData;
    private static SortedList<DownloadUnit> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //give OCTableView into holder

        System.out.println("Calling initializer of downloadsViewController");
        ocTableView = new OCTableView();
        holder.setCenter(ocTableView);

        btnDownloadSpeed.textProperty().bind(Downloader.downloadSpeedProperty());

        autoCompleteTxtField = new CustomAutoCompleteSearchTableTextField();
        autoCompleteTxtField.setPromptText("Filter table");
        toolBarLeft.getItems().add(autoCompleteTxtField);
        System.out.println("IS THIS ON FXTHREAD?" + Platform.isFxApplicationThread());

        //TODO might require to SHWO USER THAT ITS FILTERING DATA , SOME KIND OF LOADING BAR, FOR MANY ENTRIES 
        new Thread(() -> {
            downloadUnitViewCache = Downloader.getDownloads();
            // 1. Wrap the ObservableList in a FilteredList (initially display all data).
            filteredData = new FilteredList<>(downloadUnitViewCache, p -> true);
            // 3. Wrap the FilteredList in a SortedList. 
            sortedData = new SortedList<>(filteredData);
            // 4. Bind the SortedList comparator to the TableView comparator.
            sortedData.comparatorProperty().bind(ocTableView.comparatorProperty());

            // 2. Set the filter Predicate whenever the filter changes.
            autoCompleteTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate((DownloadUnit du) -> {
                    // If filter text is empty, display all persons.
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    // Compare first name and last name of every person with filter text.
                    String lowerCaseFilter = newValue.toLowerCase();

                    // special cases
                    switch (lowerCaseFilter) {
                        case "failed": {
                            return du.getState() == DownloadUnit.STATE_FAILED;
                        }
                        case "paused": {
                            return du.getState() == DownloadUnit.STATE_PAUSED;
                        }
                        case "downloading": {
                            return du.getState() == DownloadUnit.STATE_DOWNLOADING;
                        }
                        case "completed": {
                            return du.getState() == DownloadUnit.STATE_COMPLETED;
                        }
                        case "cancelled": {
                            return du.getState() == DownloadUnit.STATE_CANCELLED;
                        }
                        case "scheduled": {
                            return du.getState() == DownloadUnit.STATE_SCHEDULED;
                        }

                        case "-today": {
                            return LocalDateTime.parse(du.getAdded()).toLocalDate().equals(LocalDate.now());
                        }
                        case "-recent": {
                            return ChronoUnit.DAYS.between(LocalDateTime.parse(du.getAdded()).toLocalDate(), LocalDate.now()) < 3;

                        }
                        case "-yesterday": {
                            return ChronoUnit.DAYS.between(LocalDateTime.parse(du.getAdded()).toLocalDate(), LocalDate.now()) < 1;

                        }
                        case "-this week": {
                            return ChronoUnit.DAYS.between(LocalDateTime.parse(du.getAdded()).toLocalDate(), LocalDate.now()) < 7;

                        }
                        case "-this month": {
                            return LocalDateTime.parse(du.getAdded()).toLocalDate().getMonth().getValue() == LocalDate.now().getMonth().getValue()
                                    && LocalDateTime.parse(du.getAdded()).toLocalDate().getYear() == LocalDate.now().getYear();
                        }
                        case "-video": {
                               try{
                            //generated from testEnviroment test27.java
                            String ext = du.getName().split(".")[du.getName().split(".").length].toUpperCase();

                            System.out.println("Extension is :" + ext);
                            return ext.equals("3G2") || ext.equals("3GP") || ext.equals("ASF") || ext.equals("AVI")
                                    || ext.equals("FLV") || ext.equals("M4V") || ext.equals("MOV") || ext.equals("MP4")
                                    || ext.equals("MPG") || ext.equals("RM") || ext.equals("SRT") || ext.equals("SWF")
                                    || ext.equals("VOB") || ext.equals("WMV");
                              }catch(Exception e){
                                // coudnt find dot.
                                return false;
                            }

                        }
                        case "-audio": {
                            try{
                            String split[] = du.getSource().split("\\.");
                            String ext=split[split.length-1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("AIF") || ext.equals("IFF") || ext.equals("M3U") || ext.equals("M4A") || ext.equals("MID")
                                    || ext.equals("MP3") || ext.equals("MPA") || ext.equals("WAV") || ext.equals("WMA");
                            }catch(Exception e){
                           
                                // coudnt find dot.
                                return false;
                            }

                        }
                        case "-data": {
                             try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("CSV") || ext.equals("DAT") || ext.equals("GED") || ext.equals("KEY") || ext.equals("KEYCHAIN")
                                    || ext.equals("PPS") || ext.equals("PPT") || ext.equals("PPTX") || ext.equals("SDF") || ext.equals("TAR")
                                    || ext.equals("VCF") || ext.equals("XML");
                             }catch(Exception e){
                                // coudnt find dot. 
                                 return false;
                            }

                        }
                        case "-text": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("DOC") || ext.equals("DOCX") || ext.equals("LOG") || ext.equals("MSG")
                                    || ext.equals("ODT") || ext.equals("PAGES") || ext.equals("RTF") || ext.equals("TEX")
                                    || ext.equals("TXT") || ext.equals("WPD") || ext.equals("WPS");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }

                        }

                        case "-3d": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("3DM") || ext.equals("3DS") || ext.equals("MAX") || ext.equals("OBJ");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }

                        }
                        case "-raster": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("BMP") || ext.equals("DDS") || ext.equals("GIF") || ext.equals("JPG")
                                    || ext.equals("PNG") || ext.equals("PSD") || ext.equals("PSPIMAGE") || ext.equals("TGA") || ext.equals("THM")
                                    || ext.equals("TIF") || ext.equals("TIFF") || ext.equals("YUV");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }

                        }
                        case "-vector": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("AI") || ext.equals("EPS") || ext.equals("PS") || ext.equals("SVG");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-page layout": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("INDD") || ext.equals("PCT") || ext.equals("PDF");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }

                        case "-spreadsheet": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("XLR") || ext.equals("XLS") || ext.equals("XLSX");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-database": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("ACCDB") || ext.equals("DB") || ext.equals("DBF") || ext.equals("MDB") || ext.equals("PDB") || ext.equals("SQL");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-executable": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("APK") || ext.equals("APP") || ext.equals("BAT") || ext.equals("CGI") || ext.equals("COM") || ext.equals("EXE")
                                    || ext.equals("GADGET") || ext.equals("JAR") || ext.equals("PIF") || ext.equals("VB") || ext.equals("WSF");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-cad": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("DWG") || ext.equals("DXF");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-gis": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("GPX") || ext.equals("KML") || ext.equals("KMZ");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-web": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("ASP") || ext.equals("ASPX") || ext.equals("CER") || ext.equals("CFM") || ext.equals("CSR") || ext.equals("CSS")
                                    || ext.equals("HTM") || ext.equals("HTML") || ext.equals("JS") || ext.equals("JSP") || ext.equals("PHP") || ext.equals("RSS") || ext.equals("XHTML");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-plugin": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("CRX") || ext.equals("PLUGIN");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-font": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("FNT") || ext.equals("FON") || ext.equals("OTF") || ext.equals("TTF");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-system": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("CAB") || ext.equals("CPL") || ext.equals("CUR") || ext.equals("DLL") || ext.equals("DMP")
                                    || ext.equals("DRV") || ext.equals("ICNS") || ext.equals("ICO") || ext.equals("LNK") || ext.equals("SYS");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-settings": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("CFG") || ext.equals("INI") || ext.equals("PRF");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-encoded": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("HQX") || ext.equals("MIM") || ext.equals("UUE");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-compressed": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("7Z") || ext.equals("CBR") || ext.equals("DEB") || ext.equals("GZ") || ext.equals("PKG") || ext.equals("RAR")
                                    || ext.equals("RPM") || ext.equals("SITX") || ext.equals("GZ") || ext.equals("ZIP") || ext.equals("ZIPX");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-disk": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("BIN") || ext.equals("CUE") || ext.equals("DMG") || ext.equals("ISO") || ext.equals("MDF") || ext.equals("TOAST") || ext.equals("VCD");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        case "-source": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("C") || ext.equals("CLASS") || ext.equals("CPP") || ext.equals("CS") || ext.equals("DTD") || ext.equals("FLA") || ext.equals("H")
                                    || ext.equals("JAVA") || ext.equals("LUA") || ext.equals("M") || ext.equals("PL") || ext.equals("PY") || ext.equals("SH") || ext.equals("SLN")
                                    || ext.equals("SWIFT") || ext.equals("JS") || ext.equals("HTML") || ext.equals("VCXPROJ") || ext.equals("XCODEPROJ");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                           
                        }
                        case "-torrent": {
                            try{
                            String ext = du.getName().split(".")[du.getName().split(".").length - 1].toUpperCase();
                            System.out.println("Extension is :" + ext);
                            return ext.equals("TORRENT");
                             }catch(Exception e){
                                // coudnt find dot.
                                   return false;
                            }
                        }
                        default: {
                            if (du.getName().toLowerCase().contains(lowerCaseFilter)) {
                                System.out.println("MATCHES");
                                return true; // Filter matches first name.  
                            } else {
                                System.out.println("Does not match");
                                return false; // Does not match.  
                            }
                        }

                    }

                });
            });

            suggestions.addAll(Arrays.asList(common));

            autoCompleteTxtField.autoCompletionLearnWords(suggestions);
            //autoCompleteTxtField.loadSuggestions(suggestions);

            Platform.runLater(() -> {
                ocTableView.setItems(sortedData);
            });

        }).start();

        // loadTable();
    }

    public enum DownloadsFilter {

        SHOW_EVERYTHING,
        SHOW_DOWNLOADING,
        SHOW_COMPLETED,
        SHOW_INACTIVE
    }

    public FxmlDownloadsViewController() {

        System.out.println("Calling Downloads CONSTRUCTOR");

    }
//this view might have not been yet initialized when addItemToOCTable called

    public void loadTableDataDB() {
    }

    public void loadTable() {
        new Thread(() -> {
            // Downloader.loadDownloads();
            downloadUnitViewCache = Downloader.getDownloads();
            ocTableView.setItems(downloadUnitViewCache);

            // Downloader.getDownloads();
            // ocTableView.setItems();
        }).start();
        loadFilter(viewFilter);
    }

    public static final void loadFilter(DownloadsFilter viewFilter) {

        switch (viewFilter) {

            case SHOW_EVERYTHING: {
                filteredData.setPredicate((DownloadUnit du) -> {
                    return true;
                });

                break;
            }
            case SHOW_DOWNLOADING: {
                filteredData.setPredicate((DownloadUnit du) -> {
                    return du.getState() == DownloadUnit.STATE_DOWNLOADING;

                });
                break;
            }
            case SHOW_COMPLETED: {

                filteredData.setPredicate((DownloadUnit du) -> {
                    return du.getState() == DownloadUnit.STATE_COMPLETED;
                });
                break;
            }
            case SHOW_INACTIVE: {
                filteredData.setPredicate((DownloadUnit du) -> {
                    return du.getState() == DownloadUnit.STATE_COMPLETED || du.getState() == DownloadUnit.STATE_CANCELLED
                            || du.getState() == DownloadUnit.STATE_FAILED || du.getState() == DownloadUnit.STATE_PAUSED || du.getState() == DownloadUnit.STATE_SCHEDULED;
                });
                break;
            }
            default: {//not gonna get here
                //use all , show everything prolly
            }
            FxmlDownloadsViewController.viewFilter = viewFilter;
        }

    }

    public static void addItemToOCTable(DownloadUnit dt) {
        if (ocTableView != null) {
            ocTableView.getItems().add(dt);
        }

    }

    @FXML
    private void redownloadItemsOnEvent() {
        ocTableView.redownloadItems();

    }

    @FXML
    private void removeAllOnEvent() {
        ocTableView.removeItemsOnEvent(ocTableView.getItems());
    }

    @FXML
    private void removeItemsOnEventBtn() {
        ocTableView.removeItemsOnEvent();
    }

    @FXML
    private void pauseItemOnEventBtn() {
        ocTableView.pauseOnEvent();
    }

    @FXML
    private void pauseItemsOnEventBtn() {
        ocTableView.pauseOnEvent();
    }

    /**
     * @return the downloadUnitCache
     */
    public ObservableList<DownloadUnit> getDownloadUnitViewCache() {
        return downloadUnitViewCache;
    }

    /**
     * @param downloadUnitCache the downloadUnitCache to set
     */
    public void setDownloadUnitViewCache(ObservableList<DownloadUnit> downloadUnitCache) {
        this.downloadUnitViewCache = downloadUnitCache;
    }

}
