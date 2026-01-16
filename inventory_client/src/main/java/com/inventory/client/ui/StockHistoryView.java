package com.inventory.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inventory.client.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class StockHistoryView {
    private VBox root = new VBox(15);
    private TableView<StockLog> table = new TableView<>();
    private TextField searchField = new TextField();

    // Buttons
    private Button deleteBtn = new Button("🗑 Delete");
    private Button exportExcelBtn = new Button("Excel (CSV)");

    private ObservableList<StockLog> masterData = FXCollections.observableArrayList();

    public StockHistoryView() {
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("📜 Stock Movement History");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // --- Toolbar (Search + Export + Delete) ---
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search logs...");
        searchField.setStyle("-fx-padding: 8;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Styling Buttons for 14-inch screen (Compact)
        exportExcelBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // Button Actions
        exportExcelBtn.setOnAction(e -> exportToCSV());
        deleteBtn.setOnAction(e -> handleSelectionDelete());

        toolbar.getChildren().addAll(searchField, exportExcelBtn, deleteBtn);

        // --- Table Configuration ---
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<StockLog, String> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        TableColumn<StockLog, String> colName = new TableColumn<>("Product Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<StockLog, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        TableColumn<StockLog, Integer> colQty = new TableColumn<>("Final Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        table.getColumns().addAll(colTime, colName, colAction, colQty);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- Filtering Logic ---
        FilteredList<StockLog> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(log -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return log.getProductName().toLowerCase().contains(filter) ||
                        log.getAction().toLowerCase().contains(filter);
            });
        });

        table.setItems(filteredData);
        root.getChildren().addAll(title, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS); // Ensure table fills screen height
    }

    // --- FEATURE: Export to Excel (CSV Format) ---
    private void exportToCSV() {
        if (table.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No data available to export.").show();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Stock History Report");
        fileChooser.setInitialFileName("Stock_History_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Timestamp,Product Name,Action,Final Quantity");
                for (StockLog log : table.getItems()) {
                    writer.println(String.format("%s,%s,%s,%d",
                            log.getTimestamp(), log.getProductName(), log.getAction(), log.getQuantity()));
                }
                new Alert(Alert.AlertType.INFORMATION, "Report exported successfully!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            }
        }
    }


    private void handleSelectionDelete() {
        StockLog selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a row to delete.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected record?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    var apiResponse = ApiClient.delete("/products/history/" + selected.getId());
                    if (apiResponse.statusCode() == 200) {
                        refresh();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    public void refresh() {
        Platform.runLater(() -> {
            try {
                var response = ApiClient.get("/products/history");
                if (response.statusCode() == 200) {
                    List<StockLog> logs = new Gson().fromJson(response.body(),
                            new TypeToken<List<StockLog>>(){}.getType());
                    masterData.setAll(logs);
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public Parent getView() { return root; }

    public static class StockLog {
        private Long id;
        private String productName, action, timestamp;
        private Integer quantity;

        public Long getId() { return id; }
        public String getProductName() { return productName; }
        public String getAction() { return action; }
        public String getTimestamp() { return timestamp; }
        public Integer getQuantity() { return quantity; }
    }
}