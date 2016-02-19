/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import openchannel_dynamic_downloader.h2.H2DatabaseConnector;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.FileUtils;

/**
 *
 * @author tomas
 */
public class FxmlDatabaseViewController implements Initializable {

    @FXML
    private Hyperlink hl1hsqldbdoc;
    @FXML
    private Hyperlink hl2sqlref;
    @FXML
    private Hyperlink hl3ecpsdocs;
    @FXML
    private Button csvExport;
    @FXML
    private Button xlsxExport;
    @FXML
    private Button pdfExport;

    private final String[] links = {
        "http://www.h2database.com/h2.pdf",
        "http://www.w3schools.com/sql/sql_quickref.asp",
        ""

    };

    @FXML
    private CheckBox checkTransaction;
    @FXML
    private CheckBox checkResult;
    @FXML
    private CheckBox checkException;
    @FXML
    public CheckBox[] checkBoxes;
    @FXML
    private VBox vBoxBottom;

    @FXML
    private TextArea commandTextArea;

    private StringBuilder query;
    //keep size under 20
    private LinkedList<String> queryList;
    @FXML
    public TitledPane titPaneExcept;
    @FXML
    public TextArea exceptArea;
    @FXML
    private Button executeBtn;
    @FXML
    private TableView tableView;

    @FXML
    private Label dbNameLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Label passLabel;
    @FXML
    private Label dirLabel;

    private H2DatabaseConnectorImpl connector;

    public FxmlDatabaseViewController() {
        query = new StringBuilder();
        queryList = new LinkedList<>();
        connector = new H2DatabaseConnectorImpl(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        
        hl1hsqldbdoc.setOnMouseClicked(new HlListener(links[0]));
        hl2sqlref.setOnMouseClicked(new HlListener(links[1]));
        hl3ecpsdocs.setOnMouseClicked(new HlListener(links[2]));
        
        dbNameLabel.setText(H2DatabaseConnector.DB_FILE_NAME);
        userLabel.setText(MainDataModel.getInstance().loginProfile.getUsername());
        passLabel.setText(MainDataModel.getInstance().loginProfile.getPassword());
        dirLabel.setText(H2DatabaseConnector.DB_DIR + MainDataModel.getInstance().loginProfile.getUsername());

      
       

        checkBoxes = new CheckBox[]{
            checkTransaction,
            checkResult,
            checkException

        };

    }

    @FXML
    private void executeQuery() {
            query.append(commandTextArea.getText());
        
        if (!queryList.contains(query.toString())) {
            trimList();
            queryList.push(query.toString());
        }

        try {
            connector.openConnection();
            //execute
            connector.execute(query.toString());

        } catch (SQLException ex) {
            Logger.getLogger(FxmlDatabaseViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //clear out string builder and close connection
        query.delete(0, query.length());
        // connector.closeConnector();

    }

    private void trimList() {
        if (queryList.size() == 20) {
            queryList.removeFirst();
        }
    }

    @FXML
    private void exportPDF() {
        System.out.println("Exporting PDF");
        // FileUtils.exportDataFromTable(tableView, FileUtils.FILETYPE_PDF, FXCollections.observableArrayList());
    }

    @FXML
    private void exportCSV() {
        System.out.println("Exporting CSV");
        //FileUtils.exportDataFromTable(tableView, FileUtils.FILETYPE_CSV, FXCollections.observableArrayList());
    }

    @FXML
    private void exportXLSX() {
        System.out.println("Exporting XLSX");
        //   FileUtils.exportDataFromTable(tableView, FileUtils.FILETYPE_XLSX);
    }

    private class HlListener implements EventHandler<MouseEvent> {

        String url;

        /**
         * Creates a listener that opens up webpage on hyperlinks...
         *
         * @param url url to open in a browser
         */
        public HlListener(String url) {
            this.url = url;
        }

        @Override
        public void handle(MouseEvent event) {
            try {
                FileUtils.openWebpage(new URI(url));
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }

    }

    private static class H2DatabaseConnectorImpl extends H2DatabaseConnector {

        //getExceptArea
        StringBuilder logBuilder = new StringBuilder();
        FxmlDatabaseViewController dpcontroller;

        private ArrayList<TableColumn> columns = new ArrayList<>();

        private ObservableList<ObservableList> dataRows = FXCollections.observableArrayList();

        public H2DatabaseConnectorImpl(FxmlDatabaseViewController dpcontroller) {
            this.dpcontroller = dpcontroller;
        }

        @Override
        public void execute(String query) {
            clearLog();
            checkRep();
            resetTable();

            new Thread(() -> {

                try {

                    dpcontroller.titPaneExcept.setExpanded(true);

                    if (dpcontroller.checkBoxes[0].isSelected()) {
                        getConnection().setAutoCommit(true);
                    } else {
                        getConnection().setAutoCommit(false);
                    }


                    if (!dpcontroller.checkBoxes[1].isSelected()) {//reversed logic... too lazy
                        this.statement.executeQuery(query);
                        getConnection().commit();
                    } else {
                        //EXECUTE and retrieve data into table
                        this.resultSet = this.statement.executeQuery(query);
                        this.resultSetMd = resultSet.getMetaData();

                        //populate tableData
                        for (int i = 0; i < this.resultSetMd.getColumnCount(); i++) {
                            final int j = i;
                            TableColumn col = new TableColumn(this.resultSetMd.getColumnName(i + 1));
                            
                            col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                                @Override
                                public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                                    try {
                                        return new SimpleStringProperty(param.getValue().get(j).toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return new SimpleStringProperty("Empty");
                                    }
                                }

                            });
                            Platform.runLater(() -> {
                                dpcontroller.tableView.getColumns().addAll(col);
                            });

                        }

                        //ADD DATA OF ROWS
                        while (this.resultSet.next()) {
                            ObservableList<String> row = FXCollections.observableArrayList();
                            for (int i = 1; i < this.resultSetMd.getColumnCount() + 1; i++) {
                                row.add(resultSet.getString(i));

                            }
                            dataRows.add(row);
                        }

                    }

                    //AFTER SUCESS
                    Platform.runLater(() -> {
                        populateTable();
                        setExceptColor("GREEN");
                        logBuilder.append("Query successful");
                        dpcontroller.exceptArea.setText(logBuilder.toString());
                    });

                } catch (SQLException e) {
                    e.printStackTrace();

                    if (dpcontroller.checkBoxes[0].isSelected()) {
                        try {
                            getConnection().rollback();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }

                    if(dpcontroller.checkBoxes[2].isSelected()){
                        
                    //exception check shown or not
                    Platform.runLater(() -> {
                        try {
                            setExceptColor("RED");
                            logBuilder.append("Query failed\n");
                            PrintWriter printWriter;
                            try (StringWriter writer = new StringWriter()) {
                                printWriter = new PrintWriter(writer);
                                e.printStackTrace(printWriter);
                                printWriter.flush();
                                dpcontroller.exceptArea.setText(logBuilder.append(writer.toString()).toString());
                            }
                            printWriter.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    });
                    }
                }

            }).start();
        }

        private void clearLog() {
            logBuilder.delete(0, logBuilder.length());
            dpcontroller.exceptArea.setText("");
        }

        private void setExceptColor(String s) {
            dpcontroller.exceptArea.setStyle(
                    "-fx-text-fill:" + s + ";"
            );
        }

        private void checkRep() {
            for (CheckBox b : dpcontroller.checkBoxes) {
                System.out.println("check :" + b.isSelected());
            }
        }

        //TODO debug this method
        //does not show up all the information...
        private void populateTable() {

            dpcontroller.tableView.setItems(dataRows);

        }

        private class ValueFactory implements Callback {

            private int rowNumber;
            private int columnNumber;

            public ValueFactory(int columnNumber, int rowNumber) {
                this.columnNumber = columnNumber;
                this.rowNumber = rowNumber;
            }

            @Override
            public Object call(Object o) {
                SimpleStringProperty stringProperty = new SimpleStringProperty((String) dataRows.get(rowNumber).get(columnNumber));// WTF no idea yet whats there
                System.out.println("row number " + rowNumber + "    column number " + columnNumber);
                return stringProperty;
            }
        }

        private void resetTable() {
            columns.removeAll(columns);
            dpcontroller.tableView.getColumns().removeAll(dpcontroller.tableView.getColumns());
            dataRows.removeAll(dataRows);

        }

    }

}
