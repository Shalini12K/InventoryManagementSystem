package com.inventory.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inventory.client.ui.ProductModel; // Ensure this matches your package structure
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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class InventoryView {
    private VBox root = new VBox(20);
    private TableView<ProductModel> table = new TableView<>();
    private TextField searchField = new TextField();

    // Add Form Inputs
    private TextField nameInput = new TextField();
    private TextField catInput = new TextField();
    private TextField qtyInput = new TextField();
    private TextField priceInput = new TextField();

    // Using Map<String, Object> to represent the Supplier object from JSON
    private ComboBox<Map<String, Object>> supplierDropdown = new ComboBox<>();
    private ObservableList<ProductModel> masterData = FXCollections.observableArrayList();

    public InventoryView() {
        root.setPadding(new Insets(20));
        root.getStyleClass().add("content-area");

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("📦 Inventory Management");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        searchField.setPromptText("🔍 Search by product name or category...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-radius: 20; -fx-padding: 8 15;");
        searchField.textProperty().addListener((obs, old, val) -> filterData(val));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(header, spacer, searchField);

        setupTableColumns();
        setupRowFactory();

        HBox mainContent = new HBox(20, table, createSidePanel());
        HBox.setHgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(topBar, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        refreshTable();
        refreshSuppliers();
    }

    private VBox createSidePanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(280);

        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        nameInput.setPromptText("Product Name");
        catInput.setPromptText("Category");
        qtyInput.setPromptText("Quantity");
        priceInput.setPromptText("Price");

        supplierDropdown.setPromptText("Select Supplier");
        supplierDropdown.setMaxWidth(Double.MAX_VALUE);
        setupSupplierDropdownDisplay();

        Button saveBtn = new Button("➕ Add Product");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave());

        Button editBtn = new Button("📝 Edit Selected");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setOnAction(e -> handleEdit());

        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> handleDelete());

        form.getChildren().addAll(new Label("Product Actions"), nameInput, catInput, qtyInput, priceInput, supplierDropdown, saveBtn, editBtn, deleteBtn);

        VBox exportBox = new VBox(10);
        exportBox.setPadding(new Insets(15));
        exportBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");

        Button exportBtn = new Button("📊 Export Inventory (CSV)");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setOnAction(e -> exportToExcel());

        exportBox.getChildren().addAll(new Label("Reports"), exportBtn);
        panel.getChildren().addAll(form, exportBox);
        return panel;
    }

    private void setupSupplierDropdownDisplay() {
        supplierDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : (String) item.get("name"));
            }
        });
        supplierDropdown.setButtonCell(supplierDropdown.getCellFactory().call(null));
    }

    private void setupTableColumns() {
        TableColumn<ProductModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ProductModel, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<ProductModel, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<ProductModel, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<ProductModel, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data -> {
            Map<String, Object> s = data.getValue().getSupplier();
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.get("name").toString() : "N/A");
        });

        table.getColumns().addAll(nameCol, catCol, qtyCol, priceCol, supplierCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void handleDelete() {
        ProductModel selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ApiClient.delete("/products/" + selected.getId());
                refreshTable();
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select an item to delete!").show();
        }
    }

    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("Inventory.csv");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Name,Category,Qty,Price");
                for (ProductModel p : table.getItems()) {
                    writer.println(p.getName() + "," + p.getCategory() + "," + p.getQuantity() + "," + p.getPrice());
                }
                new Alert(Alert.AlertType.INFORMATION, "Export Successful!").show();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void refreshTable() {
        Platform.runLater(() -> {
            try {
                var response = ApiClient.get("/products");
                List<ProductModel> products = new Gson().fromJson(response.body(), new TypeToken<List<ProductModel>>(){}.getType());
                masterData.setAll(products);
                table.setItems(masterData);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void refreshSuppliers() {
        Platform.runLater(() -> {
            try {
                var response = ApiClient.get("/suppliers");
                List<Map<String, Object>> suppliers = new Gson().fromJson(response.body(), new TypeToken<List<Map<String, Object>>>(){}.getType());
                supplierDropdown.setItems(FXCollections.observableArrayList(suppliers));
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void handleSave() {
        try {
            ProductModel p = new ProductModel();
            p.setName(nameInput.getText());
            p.setCategory(catInput.getText());
            p.setQuantity(Integer.parseInt(qtyInput.getText()));
            p.setPrice(Double.parseDouble(priceInput.getText()));
            p.setSupplier(supplierDropdown.getValue());
            ApiClient.post("/products", p);

            refreshTable();
            clearForm(); // NEW: Clear inputs after save
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Check inputs!").show();
            e.printStackTrace();
        }
    }

    // Helper to clear the form fields
    private void clearForm() {
        nameInput.clear();
        catInput.clear();
        qtyInput.clear();
        priceInput.clear();
        supplierDropdown.getSelectionModel().clearSelection();
    }

    private void filterData(String searchText) {
        FilteredList<ProductModel> filteredData = new FilteredList<>(masterData, p -> true);
        filteredData.setPredicate(p -> {
            if (searchText == null || searchText.isEmpty()) return true;
            String lower = searchText.toLowerCase();
            return p.getName().toLowerCase().contains(lower) || p.getCategory().toLowerCase().contains(lower);
        });
        table.setItems(filteredData);
    }

    private void setupRowFactory() {
        table.setRowFactory(tv -> new TableRow<ProductModel>() {
            @Override
            protected void updateItem(ProductModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getQuantity() != null && item.getQuantity() < 15) setStyle("-fx-background-color: #ffeded;");
                else setStyle("");
            }
        });
    }

    // --- NEW: Full Edit Implementation ---
    private void handleEdit() {
        // 1. Get selected item
        ProductModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a product to edit!").show();
            return;
        }

        // 2. Create Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("📝 Edit Product");
        dialog.setHeaderText("Editing: " + selected.getName());

        // 3. Create Fields & Pre-fill
        TextField editName = new TextField(selected.getName());
        TextField editCat = new TextField(selected.getCategory());
        TextField editQty = new TextField(String.valueOf(selected.getQuantity()));
        TextField editPrice = new TextField(String.valueOf(selected.getPrice()));

        // Setup Edit Supplier Dropdown (Copy of main dropdown)
        ComboBox<Map<String, Object>> editSupplier = new ComboBox<>();
        editSupplier.setItems(supplierDropdown.getItems());
        editSupplier.setCellFactory(supplierDropdown.getCellFactory());
        editSupplier.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : (String) item.get("name"));
            }
        });
        // Select current supplier
        editSupplier.getSelectionModel().select(selected.getSupplier());

        // 4. Layout (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Name:"), 0, 0);      grid.add(editName, 1, 0);
        grid.add(new Label("Category:"), 0, 1);  grid.add(editCat, 1, 1);
        grid.add(new Label("Qty:"), 0, 2);       grid.add(editQty, 1, 2);
        grid.add(new Label("Price:"), 0, 3);     grid.add(editPrice, 1, 3);
        grid.add(new Label("Supplier:"), 0, 4);  grid.add(editSupplier, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 5. Add Buttons
        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        // 6. Handle Result
        dialog.showAndWait().ifPresent(response -> {
            if (response == updateBtn) {
                try {
                    selected.setName(editName.getText());
                    selected.setCategory(editCat.getText());
                    selected.setQuantity(Integer.parseInt(editQty.getText()));
                    selected.setPrice(Double.parseDouble(editPrice.getText()));
                    selected.setSupplier(editSupplier.getValue());

                    ApiClient.post("/products", selected);
                    refreshTable();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Data! Check numbers.").show();
                }
            }
        });
    }

    public Parent getView() { return root; }
}