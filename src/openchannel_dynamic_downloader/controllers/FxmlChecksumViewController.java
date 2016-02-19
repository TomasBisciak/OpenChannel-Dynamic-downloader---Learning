/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import openchannel_dynamic_downloader.downloader.DownloadUnit;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Kofola
 */
public class FxmlChecksumViewController implements Initializable {

    @FXML
    private Button btnGenerate;
    @FXML
    private ProgressBar progressBarGenerate;
    @FXML
    private TableView<MD5Pair> generatedTable;

    public class MD5Pair {

        private SimpleLongProperty id;
        private SimpleStringProperty MD5;

        public MD5Pair(long id, String MD5) {
            this.MD5 = new SimpleStringProperty(MD5);
            this.id = new SimpleLongProperty(id);
        }

        public long getId() {
            return id.get();
        }

        public void setId(long id) {
            this.id.set(id);
        }

        public String getMD5() {
            return MD5.get();
        }

        public void setMD5(String MD5) {
            this.MD5.set(MD5);
        }

    }

    @FXML
    private TableView originalTable;
    @FXML
    private TableView<MD5Pair> resultTable;

    //private final TableView<MD5Pair>[] tables = new TableView[]{generatedTable, originalTable, resultTable};
    @FXML
    private Button btnCopyToClipboard;
    @FXML
    private ImageView imgLoader;

    private List<MD5Pair> multipleSelection = new ArrayList<>();
    private MD5Pair currentSelect;

    private ArrayList<TableColumn> tcmns = new ArrayList<>();

    @FXML
    private Button btnCompare;
//    @FXML
//    private ImageView imgLoader;
    private List<DownloadUnit> downloadUnitData = new ArrayList<>();

    private final ObservableList<MD5Pair> generatedData = FXCollections.observableArrayList();
    private final ObservableList<MD5Pair> originalData = FXCollections.observableArrayList();
    private final ObservableList<MD5Pair> resultData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Callback<TableColumn, TableCell> cellFactory
                = (TableColumn p) -> new EditingCell();

        generatedTable.getColumns().get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        generatedTable.getColumns().get(1).setCellValueFactory(
                new PropertyValueFactory<>("MD5")
        );

        tcmns.add(new TableColumn("ID"));
        tcmns.add(new TableColumn("MD5"));
        tcmns.get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        tcmns.get(1).setCellValueFactory(
                new PropertyValueFactory<>("MD5")
        );

        resultTable.getColumns().get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        resultTable.getColumns().get(1).setCellValueFactory(
                new PropertyValueFactory<>("MD5")
        );

        tcmns.get(1).setCellFactory(cellFactory);

        tcmns.get(1).setOnEditCommit(
                new EventHandler<CellEditEvent<MD5Pair, String>>() {
                    @Override
                    public void handle(CellEditEvent<MD5Pair, String> t) {
                        ((MD5Pair) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setMD5(t.getNewValue());
                    }
                }
        );

        originalTable.getColumns().set(0, tcmns.get(0));
        originalTable.getColumns().set(1, tcmns.get(1));

        for (DownloadUnit du : downloadUnitData) {
            originalData.add(new MD5Pair(du.getId(), ""));
        }
        originalTable.setItems(originalData);
        generatedTable.setItems(generatedData);
        resultTable.setItems(resultData);

        setSelectionModel();
    }

    public FxmlChecksumViewController(List<DownloadUnit> downloadUnitData) {
        this.downloadUnitData = downloadUnitData;
    }

    @FXML
    private void generateMD5() {
        btnGenerate.setDisable(true);
        progressBarGenerate.setProgress(0);
        imgLoader.setVisible(true);
        boolean[] flags = new boolean[downloadUnitData.size()];
        double inc = (1.0 / downloadUnitData.size());
        for (DownloadUnit du : downloadUnitData) {
            new Thread(() -> {
                try (FileInputStream fis = new FileInputStream(new File(du.getDirectory() + du.getName()));) {
                    String md5 = DigestUtils.md5Hex(fis);
                    Platform.runLater(() -> {
                        generatedData.add(new MD5Pair(du.getId(), md5));
                    });
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FxmlChecksumViewController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FxmlChecksumViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                flags[downloadUnitData.indexOf(du)] = true;
                progressBarGenerate.setProgress(progressBarGenerate.getProgress() + inc);
            }).start();
        }

        new Thread(() -> {
            while (true && imgLoader.isVisible()) {
                int c = 0;
                for (boolean ended : flags) {
                    if (ended) {
                        c++;
                    }
                }
                if (c == flags.length) {
                    imgLoader.setVisible(false);
                }

            }
        }).start();
    }

    @FXML
    private void toClipbrd() {
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multipleSelection.size(); i++) {
            if (i + 1 == multipleSelection.size()) {
                sb.append(multipleSelection.get(i).getMD5());
            } else {
                sb.append(multipleSelection.get(i).getMD5()).append(",");
            }

        }
        StringSelection stringSelection = new StringSelection(sb.toString());
        clpbrd.setContents(stringSelection, null);
    }

    @FXML
    private void compare() {
        resultData.removeAll(resultData);
        for (int i = 0; i < generatedData.size(); i++) {
            if (generatedData.get(i).getMD5().equals(originalData.get(i).getMD5())) {
                resultData.add(new MD5Pair(generatedData.get(i).getId(), "matches"));
            } else {
                resultData.add(new MD5Pair(generatedData.get(i).getId(), "does not match"));
            }
        }
    }

    private void setSelectionModel() {

        generatedTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        generatedTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MD5Pair> c) -> {
            multipleSelection.removeAll(multipleSelection);
            currentSelect = ((MD5Pair) c.getList().get(0));
            c.getList().forEach((Object d) -> {
                multipleSelection.add(
                        ((MD5Pair) d)
                );
            });

        });

    }

    class EditingCell extends TableCell<MD5Pair, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
                if (!arg2) {
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
}
