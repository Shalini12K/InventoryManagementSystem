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
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PurchaseOrderView {
    private VBox root = new VBox(15);
    private TableView<Map<String, Object>> poTable = new TableView<>();
    private ObservableList<Map<String, Object>> masterData = FXCollections.observableArrayList();
    private TextField searchField = new TextField(); // NEW: Search Field

    public PurchaseOrderView() {
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7f6;");

        // --- Header ---
        Label title = new Label("📋 Purchase Order Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // --- Search & Controls Row ---
        searchField.setPromptText("🔍 Search by item name or supplier...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 15; -fx-padding: 8;");

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> refresh());

        Button btnExport = new Button("Excel Export");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExport.setOnAction(e -> handleExcelExport());

        HBox topBar = new HBox(15, searchField, btnRefresh, btnExport);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // --- Table Setup ---
        setupTable();
        setupSearchLogic();

        root.getChildren().addAll(title, topBar, poTable);
        VBox.setVgrow(poTable, Priority.ALWAYS);

        refresh();
    }

    private void setupSearchLogic() {
        // Create a FilteredList wrapping our masterData
        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(masterData, p -> true);

        // Bind the search field to the filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                String name = item.get("name").toString().toLowerCase();
                String supplier = item.get("supplierName") != null ? item.get("supplierName").toString().toLowerCase() : "";

                return name.contains(lowerCaseFilter) || supplier.contains(lowerCaseFilter);
            });
        });

        poTable.setItems(filteredData);
    }

    private void setupTable() {
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name").toString()));

        TableColumn<Map<String, Object>, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data -> {
            Object s = data.getValue().get("supplierName");
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.toString() : "Not Assigned");
        });

        TableColumn<Map<String, Object>, Double> stockCol = new TableColumn<>("Current Stock");
        stockCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue().get("quantity")).asObject());

        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("CRITICAL LOW"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        poTable.getColumns().addAll(nameCol, supplierCol, stockCol, statusCol);
        poTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void handleExcelExport() {
        try {
            String url = "http://localhost:8080/api/products/purchase-orders/export";
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        Platform.runLater(() -> {
            try {
                var response = ApiClient.get("/products");
                if (response.statusCode() == 200) {
                    List<Map<String, Object>> allProducts = new Gson().fromJson(response.body(),
                            new TypeToken<List<Map<String, Object>>>() {}.getType());

                    List<Map<String, Object>> lowStock = allProducts.stream()
                            .filter(p -> p.get("quantity") != null && ((Double) p.get("quantity")) < 15)
                            .collect(Collectors.toList());

                    masterData.setAll(lowStock);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Parent getView() { return root; }
}