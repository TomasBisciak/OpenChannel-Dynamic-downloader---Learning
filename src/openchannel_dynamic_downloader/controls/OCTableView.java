/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import openchannel_dynamic_downloader.downloader.DownloadUnit;

/**
 *
 * @author tomas
 */
public class OCTableView extends TableView {
    public static DownloadsFilter viewFilter = DownloadsFilter.SHOW_EVERYTHING;

    private List<TableColumn> tcmns = new ArrayList<>();
    private List<Integer> multipleSelection = new ArrayList<>();
    private DownloadUnit currentSelect;
    

    TableColumn idColumn = new TableColumn("ID");
    TableColumn nameColumn = new TableColumn("Name");
    TableColumn sizeColumn = new TableColumn("Size");
    TableColumn sourceColumn = new TableColumn("Source");
    TableColumn<DownloadUnit, DownloadUnit> progressColumn = new TableColumn("Progress");
    TableColumn downloadSpeedColumn = new TableColumn("Download Speed");
    TableColumn etaColumn = new TableColumn("ETA");
    TableColumn addedColumn = new TableColumn("Added");
    TableColumn completedOnColumn = new TableColumn("Completed on");

    TableColumn[] tableColumns = {idColumn, nameColumn, sizeColumn, sourceColumn, progressColumn, downloadSpeedColumn, etaColumn, addedColumn, completedOnColumn};

    public OCTableView() {
        this.setVisible(false);
        //set pref widths
        idColumn.setCellValueFactory(new PropertyValueFactory("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory("name"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory("size"));
        sourceColumn.setCellValueFactory(new PropertyValueFactory("source"));
        progressColumn.setCellValueFactory(new PropertyValueFactory("status"));
        downloadSpeedColumn.setCellValueFactory(new PropertyValueFactory("downloadSpeed"));
        etaColumn.setCellValueFactory(new PropertyValueFactory("eta"));
        addedColumn.setCellValueFactory(new PropertyValueFactory("added"));
        completedOnColumn.setCellValueFactory(new PropertyValueFactory("completedOn"));

        //no lambda , more understandable
        progressColumn.setCellFactory(new Callback<TableColumn<DownloadUnit, DownloadUnit>, TableCell<DownloadUnit, DownloadUnit>>() {

            @Override
            public TableCell<DownloadUnit, DownloadUnit> call(TableColumn<DownloadUnit, DownloadUnit> param) {
                return new ProgressTableCell();
            }
        });

        this.getColumns().addAll(Arrays.asList(tableColumns));

        //set up selection mdoel and functionality
        setSelectionModel();

        this.setEditable(false);
        this.setTableMenuButtonVisible(true);

        //test
        for(int i =0;i<10;i++){
            DownloadUnit unit=new DownloadUnit();
            
             this.getItems().add(unit);
        }
       

        loadFilter(viewFilter);//not sure if it wil be like this we wil lsee

        this.setVisible(true);
    }

    private void setSelectionModel() {

        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change c) -> {
            multipleSelection.removeAll(multipleSelection);
            currentSelect = ((DownloadUnit) c.getList().get(0));
            c.getList().forEach((Object d) -> {
                multipleSelection.add(Integer.parseInt(((DownloadUnit) d).getId()));
                //TODO
                //give options what can user do , scroll down panel or osmething

            });

        });

    }

    public final void loadFilter(DownloadsFilter viewFilter) {
        switch (viewFilter) {
            case SHOW_EVERYTHING: {
                
                break;
            }
            case SHOW_ACTIVE:{
                
                break;
            }
            default: {
                //use all , show everything prolly
            }
        }
    }

    public OCTableView(ObservableList<DownloadUnit> items) {//TODO not sure about this downlaodUnit

    }

    class ProgressTableCell extends TableCell<DownloadUnit, DownloadUnit> {

        private ProgressBar progressBar;
        private StackPane pane;
        private Label label;

        public ProgressTableCell() {
            pane = new StackPane();
            label = new Label("TESTING");
            progressBar = new ProgressBar(0.2);//TODO TEST
            progressBar.setStyle("-fx-accent:#3498db");
            pane.getChildren().add(0, progressBar);
            pane.getChildren().add(1, label);
            setGraphic(pane);
           
        }

        @Override
        protected void updateItem(DownloadUnit item, boolean empty) {
            if (item != null) {

                //effects on progress abr
                if (item.getState() == DownloadUnit.STATE_ACTIVE) {
                    if (item.getDownloadSpeed() < 200) {
                        progressBar.setStyle("-fx-accent:#f1c40f");
                    } else if (item.getDownloadSpeed() < 100) {
                        progressBar.setStyle("-fx-accent:#e67e22");
                    } else {
                        progressBar.setStyle("-fx-accent:#3498db");
                    }
                    label.setText(item.getProgress() + "%");

                    progressBar.setProgress(item.getProgress());
                    return;
                }
                if (item.getState() == DownloadUnit.STATE_COMPLETED) {
                    progressBar.setStyle("-fx-accent:#2ecc71");
                    label.setText("Completed");
                    return;
                }
                if (item.getState() == DownloadUnit.STATE_INACTIVE) {
                    progressBar.setStyle("-fx-accent:#bdc3c7");

                    if (item.getState() == DownloadUnit.STATE_PAUSED) {

                        label.setText("Inactive");

                    }
                    return;
                }
                //falls into inactive
                if (item.getState() == DownloadUnit.STATE_PAUSED) {
                    progressBar.setStyle("-fx-accent:#95a5a6");
                    label.setText("Paused");
                }
                
            }
        }

    }
    
     public enum DownloadsFilter{
        SHOW_EVERYTHING,
        SHOW_ACTIVE,
        SHOW_COMPLETED,
        SHOW_INACTIVE,
        SHOW_PAUSED
        
    }

}

