package com.inventory.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inventory.client.util.ApiClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesView {
    private VBox root = new VBox(20);
    private TableView<Map<String, Object>> cartTable = new TableView<>();
    private ObservableList<Map<String, Object>> cartData = FXCollections.observableArrayList();

    private ComboBox<ProductModel> productPicker = new ComboBox<>();
    private TextField qtyInput = new TextField("1");

    private Label subtotalLabel = new Label("Subtotal: Rs 0.00");
    private Label taxLabel = new Label("Tax (10%): Rs 0.00");
    private Label totalLabel = new Label("Total: Rs 0.00");

    private double subtotal = 0.0;

    // Callback to refresh Dashboard when a sale is complete
    private Runnable onSaleCompleted;

    public void setOnSaleCompletedListener(Runnable listener) {
        this.onSaleCompleted = listener;
    }

    public SalesView() {
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7f6;");

        // --- Header ---
        Label header = new Label("🛒 Stationery Checkout System");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // --- Selection Bar ---
        HBox selectionBar = new HBox(15);
        selectionBar.setPadding(new Insets(15));
        selectionBar.setAlignment(Pos.CENTER_LEFT);
        selectionBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        productPicker.setPromptText("Search items...");
        productPicker.setPrefWidth(300);

        qtyInput.setPrefWidth(60);

        Button addBtn = new Button("Add to Cart");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> addToCart());

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> { cartData.clear(); calculateTotals(); });

        selectionBar.getChildren().addAll(new Label("Product:"), productPicker, new Label("Qty:"), qtyInput, addBtn, clearBtn);

        // --- Cart Table ---
        setupCartTable();

        // --- Footer: Summary & Checkout ---
        VBox summaryBox = new VBox(5);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        summaryBox.getChildren().addAll(subtotalLabel, taxLabel, totalLabel);
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Button checkoutBtn = new Button("💳 Complete Sale");
        checkoutBtn.setPrefHeight(50);
        checkoutBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8;");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> processCheckout());

        HBox footer = new HBox(40, new Region(), summaryBox, checkoutBtn);
        HBox.setHgrow(footer.getChildren().get(0), Priority.ALWAYS);

        root.getChildren().addAll(header, selectionBar, cartTable, footer);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        refresh();
    }

    private void setupCartTable() {
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("name").toString()));

        TableColumn<Map<String, Object>, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d -> new SimpleObjectProperty<>((Integer) d.getValue().get("qty")));

        TableColumn<Map<String, Object>, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(d -> new SimpleObjectProperty<>((Double) d.getValue().get("price")));
        priceCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("Rs %.2f", v));
            }
        });

        cartTable.getColumns().addAll(nameCol, qtyCol, priceCol);
        cartTable.setItems(cartData);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Right-click to delete
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("Remove Item");
        delete.setOnAction(e -> {
            cartData.remove(cartTable.getSelectionModel().getSelectedItem());
            calculateTotals();
        });
        menu.getItems().add(delete);
        cartTable.setContextMenu(menu);
    }

    private void addToCart() {
        ProductModel p = productPicker.getValue();
        if (p == null) return;

        try {
            int qty = Integer.parseInt(qtyInput.getText());
            if (qty > p.getQuantity()) {
                new Alert(Alert.AlertType.ERROR, "Only " + p.getQuantity() + " left in stock!").show();
                return;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("name", p.getName());
            item.put("qty", qty);
            item.put("price", p.getPrice() * qty);

            cartData.add(item);
            calculateTotals();
            qtyInput.setText("1");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid Quantity").show();
        }
    }

    private void calculateTotals() {
        subtotal = cartData.stream().mapToDouble(i -> (Double) i.get("price")).sum();
        double tax = subtotal * 0.10;
        subtotalLabel.setText(String.format("Subtotal: Rs %.2f", subtotal));
        taxLabel.setText(String.format("Tax (10%%): Rs %.2f", tax));
        totalLabel.setText(String.format("Total: Rs %.2f", subtotal + tax));
    }

    private void processCheckout() {
        if (cartData.isEmpty()) return;

        try {
            List<String> itemStrings = new ArrayList<>();
            cartData.forEach(i -> itemStrings.add(i.get("name") + " (x" + i.get("qty") + ")"));

            Map<String, Object> payload = new HashMap<>();
            payload.put("items", itemStrings);
            payload.put("totalAmount", subtotal * 1.10);

            var response = ApiClient.post("/sales", payload);

            if (response.statusCode() == 200) {
                // IMPORTANT: Notify Dashboard via MainLayout bridge
                if (onSaleCompleted != null) onSaleCompleted.run();

                showReceipt();
                cartData.clear();
                calculateTotals();
                refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Sale failed: " + response.body()).show();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showReceipt() {
        StringBuilder sb = new StringBuilder("--- STATIONERY PRO RECEIPT ---\n\n");
        cartData.forEach(i -> sb.append(String.format("%-15s x%d  Rs%.2f\n", i.get("name"), i.get("qty"), i.get("price"))));
        sb.append("\n------------------------------\n");
        sb.append(String.format("TOTAL PAID: Rs %.2f\n", subtotal * 1.10));
        sb.append("------------------------------\nTHANK YOU!");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Transaction Complete");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    public void refresh() {
        new Thread(() -> {
            try {
                var res = ApiClient.get("/products");
                List<ProductModel> products = new Gson().fromJson(res.body(), new TypeToken<List<ProductModel>>(){}.getType());
                Platform.runLater(() -> productPicker.setItems(FXCollections.observableArrayList(products)));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public Parent getView() { return root; }
}