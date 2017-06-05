/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import openchannel_dynamic_downloader.utils.MiscUtils;

/**
 *
 * @author Kofola
 */
public class DailyStatTable extends TableView {

    TableColumn<DailyStat, Long> idColumn = new TableColumn("Day(yyyy-MM-dd)");//in reality its id in different format bud we convert it 
    TableColumn<DailyStat, Long> bytesDownColumn = new TableColumn("Data Downloaded");
    TableColumn numberOfDownColumn = new TableColumn("Number of Downloads");

    private final List<DailyStat> multipleSelection = new ArrayList<>();
    private DailyStat currentSelect;

    TableColumn[] tableColumns = {idColumn, bytesDownColumn, numberOfDownColumn};

    public DailyStatTable() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory("idDate"));

        idColumn.setCellFactory(
                new Callback<TableColumn<DailyStat, Long>, TableCell<DailyStat, Long>>() {

                    @Override
                    public TableCell<DailyStat, Long> call(TableColumn<DailyStat, Long> param
                    ) {
                        return new TableCell<DailyStat, Long>() {

                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    String year = String.valueOf(item).substring(0, 4);
                                    String month = String.valueOf(item).substring(4, 6);
                                    String day = String.valueOf(item).substring(6, 8);
                                    setText(year + "-" + month + "-" + day);
                                }
                            }

                        };
                    }
                }
        );

        bytesDownColumn.setCellValueFactory(
                new PropertyValueFactory("bytesDownloaded"));

        bytesDownColumn.setCellFactory(
                new Callback<TableColumn<DailyStat, Long>, TableCell<DailyStat, Long>>() {

                    @Override
                    public TableCell<DailyStat, Long> call(TableColumn<DailyStat, Long> param
                    ) {
                        return new TableCell<DailyStat, Long>() {

                            @Override
                            protected void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
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

        numberOfDownColumn.setCellValueFactory(
                new PropertyValueFactory("numberOfDownloads"));

        this.getColumns().addAll(Arrays.asList(tableColumns));
        setSelectionModel();
    }

    private void setSelectionModel() {

        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change c) -> {
            multipleSelection.removeAll(multipleSelection);
            currentSelect = ((DailyStat) c.getList().get(0));
            c.getList().forEach((Object d) -> {
                multipleSelection.add(
                        ((DailyStat) d)
                );
                //TODO
                //give options what can user do , scroll down panel or osmething

            });

        });

    }

}
