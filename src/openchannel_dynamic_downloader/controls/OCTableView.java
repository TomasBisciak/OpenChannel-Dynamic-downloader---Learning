/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import openchannel_dynamic_downloader.controllers.FxmlChecksumViewController;
import openchannel_dynamic_downloader.downloader.DownloadTask;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import openchannel_dynamic_downloader.downloader.Downloader;
import openchannel_dynamic_downloader.utils.DbUtil;
import openchannel_dynamic_downloader.utils.Info;
import openchannel_dynamic_downloader.utils.MiscUtils;

/**
 *
 * @author tomas
 */
public class OCTableView extends TableView {

    private List<DownloadUnit> multipleSelection = new ArrayList<>();
    private DownloadUnit currentSelect;

    TableColumn idColumn = new TableColumn("ID");
    TableColumn<DownloadUnit, Integer> stateColumn = new TableColumn<>("State");
    TableColumn<DownloadUnit, Long> playbackColumn = new TableColumn<>("Playback");

    TableColumn nameColumn = new TableColumn("Name");
    TableColumn<DownloadUnit, Long> sizeColumn = new TableColumn<>("Size");
    TableColumn<DownloadUnit, Long> downloadedColumn = new TableColumn<>("Downloaded");
    TableColumn sourceColumn = new TableColumn("Source");

    TableColumn<DownloadUnit, Double> progressColumn = new TableColumn<>("Progress");
    TableColumn<DownloadUnit, Double> downloadSpeedColumn = new TableColumn<>("Download Speed");

    TableColumn numOfConnectionsColumn = new TableColumn("Connections");
    TableColumn etaColumn = new TableColumn("ETA");
    TableColumn addedColumn = new TableColumn("Added");
    TableColumn completedOnColumn = new TableColumn("Completed on");

    TableColumn[] tableColumns = {idColumn, stateColumn, nameColumn, playbackColumn, sizeColumn, downloadedColumn, sourceColumn, progressColumn, downloadSpeedColumn, numOfConnectionsColumn, etaColumn, addedColumn, completedOnColumn};

    public OCTableView() {
        this.setVisible(false);

        this.setRowFactory(
                new Callback<TableView<DownloadUnit>, TableRow<DownloadUnit>>() {
                    @Override
                    public TableRow<DownloadUnit> call(TableView<DownloadUnit> tableView) {
                        final TableRow<DownloadUnit> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();

                        MenuItem removeItem = new MenuItem("Remove");
                        MenuItem removeAndDeleteData = new MenuItem("Remove & delete data");
                        SeparatorMenuItem separator = new SeparatorMenuItem();
                        MenuItem openFile = new MenuItem("Open");
                        MenuItem openDir = new MenuItem("Open containing folder");
                        MenuItem copyURL = new MenuItem("Copy URL");
                        MenuItem start = new MenuItem("Start");
                        MenuItem forceStart = new MenuItem("Force Start");
                        MenuItem pause = new MenuItem("Pause");
                        MenuItem cancel = new MenuItem("Cancel Download");
                        MenuItem redownload = new MenuItem("Re-download");
                        MenuItem generateMD5 = new MenuItem("Generate & Compare MD5");//TODO only allow on completed downloads
                        MenuItem copyIntoClipboard = new MenuItem("Copy info into clipboard");

                        copyIntoClipboard.setOnAction((ActionEvent event) -> {
                            infoCopyToClipboard();
                        });

                        cancel.setOnAction((ActionEvent event) -> {
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            cancelDownload();
                        });
                        start.setOnAction((ActionEvent event) -> {
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            resumeDownloadOnEvent();
                        });
                        forceStart.setOnAction((ActionEvent event) -> {
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            resumeForcedDownloadOnEvent();
                        });

                        redownload.setOnAction((ActionEvent event) -> {
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            redownloadItems();
                        });

                        generateMD5.setOnAction((ActionEvent event) -> {
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            md5OnEvent();
                        });

                        removeItem.setOnAction((ActionEvent event) -> {
                            System.out.println("Gonna remove num:" + multipleSelection.size() + "items");
                            //getItems().removeAll(multipleSelection);//TODO CAHNGE ON SELECTED ROWS
                            // getItems().remove(row.getItem());
                            // multipleSelection.removeAll(multipleSelection);
                            removeItemsOnEvent();
                        });

                        removeAndDeleteData.setOnAction((ActionEvent event) -> {
                            removeAndDeleteItemsOnEvent();
                        });

                        copyURL.setOnAction((ActionEvent event) -> {
                            copyURLOnEvent();
                        });

                        openDir.setOnAction((ActionEvent event) -> {
                            openDirOnEvent();
                        });
                        openFile.setOnAction((ActionEvent event) -> {
                            openFileOnEvent();
                        });
                        pause.setOnAction((ActionEvent event) -> {
                            pauseOnEvent();
                        });
                        rowMenu.getItems()
                        .addAll(removeItem, removeAndDeleteData, new SeparatorMenuItem(), openDir, openFile, new SeparatorMenuItem(), copyURL,copyIntoClipboard, generateMD5, new SeparatorMenuItem(), start, forceStart, pause, cancel, redownload);

                        // only display context menu for non-null items:
                        row.contextMenuProperty()
                        .bind(
                                Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));
                        return row;
                    }
                }
        );

        //set pref widths
        idColumn.setCellValueFactory(
                new PropertyValueFactory("id"));
        sourceColumn.setPrefWidth(
                500);
        playbackColumn.setCellValueFactory(
                new PropertyValueFactory("id"));
        playbackColumn.setResizable(
                false);
        playbackColumn.setCellFactory(
                new Callback<TableColumn<DownloadUnit, Long>, TableCell<DownloadUnit, Long>>() {

                    @Override
                    public TableCell<DownloadUnit, Long> call(TableColumn<DownloadUnit, Long> param
                    ) {
                        return new TableCell<DownloadUnit, Long>() {
                            private Button btn = new Button("");

                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                    setGraphic(null);
                                } else {
                                    setStyle("-fx-background-position:center center;-fx-font-size:9");
                                    setAlignment(Pos.CENTER);
                                    setGraphic(btn);
                                    btn.setMinWidth(40);
                                    btn.setStyle("-fx-background-image:url(http://www.iconsdb.com/icons/preview/caribbean-blue/play-xxl.png);-fx-background-position: "
                                            + "center center;-fx-background-size: 20 20;-fx-background-repeat: no-repeat;  -fx-background-radius:0;  -fx-background-width:0;");
                                    btn.setOnMouseReleased((MouseEvent event) -> {
                                        //user custom player to play file.
                                    });
                                }
                            }
                        };
                    }
                }
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory("name"));
        nameColumn.setPrefWidth(
                150);
        stateColumn.setCellValueFactory(
                new PropertyValueFactory<>("state"));
        stateColumn.setPrefWidth(
                150);
        stateColumn.setCellFactory(
                new Callback<TableColumn<DownloadUnit, Integer>, TableCell<DownloadUnit, Integer>>() {

                    @Override
                    public TableCell<DownloadUnit, Integer> call(TableColumn<DownloadUnit, Integer> param
                    ) {
                        return new TableCell<DownloadUnit, Integer>() {
                            //private Label lbl = new Label("State");

                            @Override
                            protected void updateItem(Integer item, boolean empty) {
                                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    setStyle("-fx-alignment:CENTER;");
                                    switch (item) {
                                        case DownloadUnit.STATE_FAILED: {
                                            setText("Failed");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#e74c3c;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case DownloadUnit.STATE_PAUSED: {
                                            setText("Paused");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#7f8c8d;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case DownloadUnit.STATE_SCHEDULED: {
                                            setText("Scheduled");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#7f8c8d;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case DownloadUnit.STATE_DOWNLOADING: {
                                            setText("Downloading");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#3498db;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case DownloadUnit.STATE_COMPLETED: {
                                            setText("Completed");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#2ecc71;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case DownloadUnit.STATE_CANCELLED: {
                                            setText("Cancelled");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#34495e;-fx-text-fill:WHITE");
                                            break;
                                        }

                                        default: {
                                            setText(null);
                                            setStyle("");
                                        }
                                    }
                                    //setGraphic(lbl);
                                }
                            }

                        };
                    }
                }
        );

        sizeColumn.setCellValueFactory(
                new PropertyValueFactory<>("size"));
        sizeColumn.setPrefWidth(
                75);
        sizeColumn.setCellFactory(
                new Callback<TableColumn<DownloadUnit, Long>, TableCell<DownloadUnit, Long>>() {

                    @Override
                    public TableCell<DownloadUnit, Long> call(TableColumn<DownloadUnit, Long> param
                    ) {
                        return new TableCell<DownloadUnit, Long>() {

                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    //optimized
                                    setText(MiscUtils.humanReadableByteCount(item));
                                   

                                }
                            }

                        };
                    }
                }
        );

        downloadedColumn.setCellValueFactory(
                new PropertyValueFactory<>("downloaded"));
        downloadedColumn.setPrefWidth(
                75);
        downloadedColumn.setCellFactory(
                new Callback<TableColumn<DownloadUnit, Long>, TableCell<DownloadUnit, Long>>() {

                    @Override
                    public TableCell<DownloadUnit, Long> call(TableColumn<DownloadUnit, Long> param
                    ) {
                        return new TableCell<DownloadUnit, Long>() {

                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    //optimized
                                    setText(MiscUtils.humanReadableByteCount(item));

                                }
                            }

                        };
                    }

                }
        );

        sourceColumn.setCellValueFactory(
                new PropertyValueFactory("source"));
        sourceColumn.setPrefWidth(
                100);

        progressColumn.setCellValueFactory(
                new PropertyValueFactory<>("progress"));
        progressColumn.setPrefWidth(
                200);
        progressColumn.setCellFactory(ProgressBarTableCell.<DownloadUnit>forTableColumn());

        downloadSpeedColumn.setCellValueFactory(
                new PropertyValueFactory("downloadSpeed"));
        downloadSpeedColumn.setPrefWidth(
                150);
        downloadSpeedColumn.setCellFactory(column
                -> {
                    return new TableCell<DownloadUnit, Double>() {

                        @Override
                        protected void updateItem(Double item, boolean empty) {
                            super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                            if (item == null || empty) {
                                setText(null);
                                setStyle("");
                            } else {
                                if (item == 0) {//kB/s
                                    setText(null);
                                    setStyle("");
                                } else if (item < 1000) {
                                    setText(item + " kB/s");
                                } else {
                                    setText((item / 1000) + " MB/s");
                                }

                            }

                        }

                    };
                }
        );

        numOfConnectionsColumn.setCellValueFactory(
                new PropertyValueFactory("numOfConnections"));
        etaColumn.setCellValueFactory(
                new PropertyValueFactory("eta"));
        etaColumn.setPrefWidth(
                75);
        addedColumn.setCellValueFactory(
                new PropertyValueFactory("added"));
        addedColumn.setPrefWidth(
                170);
        completedOnColumn.setCellValueFactory(
                new PropertyValueFactory("completedOn"));
        completedOnColumn.setPrefWidth(
                170);

        this.getColumns()
                .addAll(Arrays.asList(tableColumns));
        // this.getColumns().add(downloadedColumn);
        //set up selection mdoel and functionality

        this.setEditable(
                false);

        this.setTableMenuButtonVisible(
                true);

        setSelectionModel();

        this.setVisible(
                true);
    }

    private final StringBuilder localSb = new StringBuilder();

    public void infoCopyToClipboard() {
        for (DownloadUnit du : multipleSelection) {
            localSb.append(du.getId()).append(",").append(du.getState()).append(",").append(du.getName()).append(",").append(du.getSize())
                    .append(",").append(du.getDownloaded()).append(",").append(du.getSource()).append(",").append(du.getNumberOfConnections())
                    .append(",").append(du.getAdded()).append(",").append(du.getCompletedOn()).append("\n");
        }
        StringSelection selection = new StringSelection(localSb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

    }

    //THAT TAKES CARE OF DOWNLAODS THAT ARE FINISHED , PARTIALLY  DOWNLAODED NOT . SINGLE CONNECITON OK SEGMENTED NOT
    public final void removeAndDeleteItemsOnEvent() {
        new Thread(() -> {
            //FIRST STOP /REMOVE FROM DB THE FUCKN DOWNLOAD THEN REMOVE
            //Downloader.removeDownloads(multipleSelection);
            DbUtil.removeDownloads(multipleSelection);
            multipleSelection.forEach((d) -> {
                d.cancel();
                if (d.getState() == DownloadUnit.STATE_COMPLETED || d.getNumberOfConnections() == 1) {
                    try {
                        Files.deleteIfExists(Paths.get(d.getDirectory() + d.getName()));
                    } catch (IOException ex) {
                        Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        //Files.deleteIfExists(Paths.get(d.getDirectory() + d.getName()));
                        for (int i = 0; i < d.getNumberOfConnections(); i++) {
                            Files.deleteIfExists(Paths.get(d.getDirectory() + d.getName() + DownloadUnit.FILENAME_PARTIAL + (i + 1)));
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Platform.runLater(() -> {
                    Downloader.getDownloads().removeAll(multipleSelection);
                });
                Downloader.getDtCache().removeAll(multipleSelection);

            });
        }).start();

    }

    public final void resumeForcedDownloadOnEvent() {
        // MAYBE CREATE FORCE START THAT WILL RESUME EVEN SCHEDULED  A DOWNLOADS
        //now takes care of situation where resume woud be called on DU instead of DT and creates DT from DU
        Platform.runLater(() -> {
            //TODO  TIME THIS EXECUTION WITH PARALLEL AND NORMAL STREAM WHILE UNDER DOWNLOAD LOAD OF MULTIPLE THREADS
            //WORKS NOT BUD NOT SURE IF THIS IS THE MOST OPTIMIZED WAY OF DOINGTHINGS, BUT DOES  STOPS US FROM CREATING TASKS THAT ARE RUNNING IN THE BACKGROUND , instead they are created when needed.
            ArrayList<DownloadUnit> temp = new ArrayList<>();//to remove old
            ArrayList<DownloadTask> tempTask = new ArrayList<>();//to remove old
            Downloader.getDownloads().parallelStream().filter((DownloadUnit du) -> multipleSelection.stream().
                    filter((DownloadUnit d) -> d.getId() == du.getId()).count() > 0).forEach((DownloadUnit du) -> {
                        try {
                            // IN CASE OF STATE BEIGN SOMETHING ELSE THEN PAUSED !  dont execute !
                            if (du.getState() == DownloadUnit.STATE_PAUSED || du.getState() == DownloadUnit.STATE_SCHEDULED) {// i dont care here for scheduler since  i cant resume scheduled download
                                tempTask.add(new DownloadTask(du.getName(), new URL(du.getSource()), du.getSize(), du.getDirectory(), du.getNumberOfConnections(), du.getId(), du.getState()));
                                temp.add(du);//old pointer remove from downloads
                            } else {
                                //TODO create notifications
                                System.out.println("notify user that this cannot be resumed state:" + du.getState() + " , id :" + du.getId());
                            }

                        } catch (MalformedURLException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (DownloadTask.SizeNotDeterminedException | SQLException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
            System.out.println("Size of temp: " + temp.size());
            //remove old
            temp.forEach((du) -> {
                System.out.println("REMOVING FROM OCTABLEVIEW");
                Downloader.getDownloads().remove(du);

            });
            //add new
            tempTask.forEach((dt) -> {
                System.out.println("gonna resume dt:" + dt.getId() + " state:" + dt.getState());
                Downloader.getDtCache().add(dt);
                Downloader.getDownloads().add(dt);
                Thread t = new Thread(dt);
                t.start();
                dt.resume();
            });

        });

        /*
         //slight revork above maybe
         Downloader.getDtCache().parallelStream().filter((DownloadTask dt) -> multipleSelection.stream().
         filter((DownloadUnit d) -> d.getId() == dt.getId()).count() > 0).forEach((DownloadTask dt) -> {
         dt.resume();
         System.out.println("gonna resume dt:" + dt.getId());
         });
         /*
         multipleSelection.stream().forEach((DownloadUnit du) -> {
         du.setState(DownloadUnit.STATE_DOWNLOADING);
         });
         */
    }

    private void resumeDownloadOnEvent() {
        // MAYBE CREATE FORCE START THAT WILL RESUME EVEN SCHEDULED  A DOWNLOADS
        //now takes care of situation where resume woud be called on DU instead of DT and creates DT from DU
        Platform.runLater(() -> {
            //TODO  TIME THIS EXECUTION WITH PARALLEL AND NORMAL STREAM WHILE UNDER DOWNLOAD LOAD OF MULTIPLE THREADS
            //WORKS NOT BUD NOT SURE IF THIS IS THE MOST OPTIMIZED WAY OF DOINGTHINGS, BUT DOES  STOPS US FROM CREATING TASKS THAT ARE RUNNING IN THE BACKGROUND , instead they are created when needed.
            ArrayList<DownloadUnit> temp = new ArrayList<>();//to remove old
            ArrayList<DownloadTask> tempTask = new ArrayList<>();//to remove old
            Downloader.getDownloads().parallelStream().filter((DownloadUnit du) -> multipleSelection.stream().
                    filter((DownloadUnit d) -> d.getId() == du.getId()).count() > 0).forEach((DownloadUnit du) -> {
                        try {
                            // IN CASE OF STATE BEIGN SOMETHING ELSE THEN PAUSED !  dont execute !
                            if (du.getState() == DownloadUnit.STATE_PAUSED) {// i dont care here for scheduler since  i cant resume scheduled download
                                tempTask.add(new DownloadTask(du.getName(), new URL(du.getSource()), du.getSize(), du.getDirectory(), du.getNumberOfConnections(), du.getId(), du.getState()));
                                temp.add(du);//old pointer remove from downloads
                            } else {
                                //TODO create notifications
                                System.out.println("notify user that this cannot be resumed state:" + du.getState() + " , id :" + du.getId());
                            }

                        } catch (MalformedURLException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (DownloadTask.SizeNotDeterminedException | SQLException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
            System.out.println("Size of temp: " + temp.size());
            //remove old
            temp.forEach((du) -> {
                System.out.println("REMOVING FROM OCTABLEVIEW");
                Downloader.getDownloads().remove(du);

            });
            //add new
            tempTask.forEach((dt) -> {
                System.out.println("gonna resume dt:" + dt.getId() + " state:" + dt.getState());
                Downloader.getDtCache().add(dt);
                Downloader.getDownloads().add(dt);
                Thread t = new Thread(dt);
                t.start();
                dt.resume();
            });

        });

        /*
         //slight revork above maybe
         Downloader.getDtCache().parallelStream().filter((DownloadTask dt) -> multipleSelection.stream().
         filter((DownloadUnit d) -> d.getId() == dt.getId()).count() > 0).forEach((DownloadTask dt) -> {
         dt.resume();
         System.out.println("gonna resume dt:" + dt.getId());
         });
         /*
         multipleSelection.stream().forEach((DownloadUnit du) -> {
         du.setState(DownloadUnit.STATE_DOWNLOADING);
         });
         */
    }

    //TODO move all ofthese methods that actually do something usefull liek remove download etc etc , to downloader and call the mfrom ehre with parameter of multipleselection !!!!!!!!!!!!!!!!!!!
    public final void cancelDownload() {
        new Thread(() -> {
            multipleSelection.forEach((d) -> {
                d.cancel(); //this must be tested
            });
            //remove existing
            //remove data probably
            //dont do anythign for now
            //removeItemsOnEvent();
        }).start();
    }

    public final void redownloadItems() {

        new Thread(() -> {
            multipleSelection.forEach((DownloadUnit d) -> {
                //TODO take care of partitioned file/or nonsegmented downlad
                if (d.getState() == DownloadUnit.STATE_COMPLETED || d.getNumberOfConnections() == 1) {

                    //just delete final file
                    try {
                        Files.deleteIfExists(Paths.get(d.getDirectory() + d.getName()));
                    } catch (IOException ex) {
                        Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    for (int i = 0; i < d.getNumberOfConnections(); i++) {
                        try {
                            Files.deleteIfExists(Paths.get(d.getDirectory() + "/" + d.getName() + DownloadTask.FILENAME_PARTIAL + (i + 1)));
                        } catch (IOException ex) {
                            Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            });
            //start new download with same parameters/might use internal notification / sys tray may get annoying
            multipleSelection.forEach((d) -> {
                try {
                    Downloader.downloadFile(new URL(d.getSource()), d.getName(), d.getDirectory(), d.getNumberOfConnections(), false, true);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(OCTableView.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            //remove existing
            removeItemsOnEvent();
        }).start();

    }

    //internally called has to delete items from db as well
    public void removeItemsOnEvent() {
        //remove from table view
        // getItems().removeAll(multipleSelection);

        //TODO execture OUTSIDE OF FXAT
        new Thread(() -> {
            Downloader.removeDownloads(multipleSelection);
            //multipleSelection.removeAll(multipleSelection);
        }).start();

        //call directly deletion from DBUtil 
    }

    public void removeItemsOnEvent(ObservableList<DownloadUnit> list) {
        //remove from table view
        // getItems().removeAll(multipleSelection);

        //TODO execture OUTSIDE OF FXAT
        new Thread(() -> {
            Downloader.removeDownloads(list);
            //multipleSelection.removeAll(multipleSelection);
        }).start();

        //call directly deletion from DBUtil 
    }

    public void pauseOnEvent() {
        System.out.println("Calling pause");
        new Thread(() -> {//not intensive just testing.
            Downloader.getDtCache().parallelStream().filter((DownloadTask dt) -> multipleSelection.stream().
                    filter((DownloadUnit d) -> d.getId() == dt.getId()).count() > 0).forEach((DownloadTask dt) -> {
                        dt.pause();
                        //something fuckn updated it after it was nullified//it was iteration of DownloadTask
                        // dt.downloadSpeedProperty().set(0);
                        // dt.etaProperty().set("");
                    });
        }).start();

    }

    public void copyURLOnEvent() {
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multipleSelection.size(); i++) {
            //TODO IMPLEMENT
            if (i + 1 == multipleSelection.size()) {
                sb.append(multipleSelection.get(i).getSource());
            } else {
                sb.append(multipleSelection.get(i).getSource()).append(",");
            }

        }
        StringSelection stringSelection = new StringSelection(sb.toString());
        clpbrd.setContents(stringSelection, null);
    }

    public void openDirOnEvent() {

        multipleSelection.stream().forEach((du) -> {
            try {
                if (du.getNumberOfConnections() == 1 || du.getState() == DownloadUnit.STATE_COMPLETED) {
                    Runtime.getRuntime().exec("explorer.exe /select," + du.getDirectory() + du.getName());
                } else {
                    Runtime.getRuntime().exec("explorer.exe /select," + du.getDirectory() + du.getName() + DownloadUnit.FILENAME_PARTIAL + 1);
                }

                //   new ProcessBuilder("explorer.exe", "/select," ).start();
                System.out.println("NAME:" + du.getName() + "  direcotry:" + du.getDirectory());
                // Runtime.getRuntime().exec("explorer.exe /select," + du.getDirectory() + "\\" + du.getName());//open in explorer and highlight it
                //Desktop.getDesktop().open(new File(du.getDirectory()));
            } catch (IOException ex) {
                //todo show some popup that i cant open this
                ex.printStackTrace();
            }
        });

    }
//https://community.oracle.com/thread/3677813?start=0&tstart=0

    public void md5OnEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.Resource.FXML_FILE_CHECKSUM));
            loader.setController(new FxmlChecksumViewController(multipleSelection));//setSome params there
            BorderPane root = (BorderPane) loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);
            stage.setTitle("OpenChannel Checksum");
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(OCTableView.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void openFileOnEvent() {
        multipleSelection.stream().forEach((du) -> {
            try {
                Desktop.getDesktop().open(new File(du.getDirectory() + "\\" + du.getName()));
            } catch (IOException ex) {
                //todo show some popup that i cant open this
                ex.printStackTrace();
            }
        });

    }
    /*
     public void removeItemsOnEvent(List<DownloadTask> list) {
     getItems().removeAll(list);
     Downloader.removeDownload();
     }
     */

    public void removeItems(List<DownloadUnit> list) {
        getItems().removeAll(list);
    }

    public void removeItem(DownloadUnit dt) {
        getItems().remove(dt);
    }

    public void removeItem(Long ID) {
        for (Object dt : getItems()) {
            if (((DownloadUnit) dt).getId() == ID) {
                getItems().remove(dt);
            }
        }
    }

    private void setSelectionModel() {

        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change c) -> {
            multipleSelection.removeAll(multipleSelection);
            currentSelect = ((DownloadUnit) c.getList().get(0));
            c.getList().forEach((Object d) -> {
                multipleSelection.add(
                        ((DownloadUnit) d)
                );
                //TODO
                //give options what can user do , scroll down panel or osmething

            });

        });

    }

    public OCTableView(ObservableList<DownloadUnit> items) {

    }

}
