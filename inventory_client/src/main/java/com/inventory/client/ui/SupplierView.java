package com.inventory.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inventory.client.ui.SupplierModel;
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

public class SupplierView {

    private VBox root = new VBox(20);
    private TableView<SupplierModel> table = new TableView<>();
    private TextField searchField = new TextField(); // Search Bar
    private ObservableList<SupplierModel> masterData = FXCollections.observableArrayList();


    // Form Inputs
    private TextField nameInput = new TextField();
    private TextField contactInput = new TextField();
    private TextField phoneInput = new TextField();
    private TextField emailInput = new TextField();
    private TextField addressInput = new TextField();


    public SupplierView() {

        root.setPadding(new Insets(20));
        root.getStyleClass().add("content-area");

        // --- TOP BAR WITH SEARCH ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);


        Label header = new Label("🚚 Supplier Management");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        searchField.setPromptText("🔍 Search by company or contact name...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-radius: 20; -fx-padding: 8 15;");
        // Listen for typing to filter the table
        searchField.textProperty().addListener((obs, old, val) -> filterData(val));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(header, spacer, searchField);

        setupTableColumns();

        HBox mainContent = new HBox(20, table, createSidePanel());
        HBox.setHgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(topBar, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        refreshTable();
    }

    private VBox createSidePanel() {

        VBox panel = new VBox(15);
        panel.setPrefWidth(300);

        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label formTitle = new Label("Supplier Actions");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        nameInput.setPromptText("Company Name");
        contactInput.setPromptText("Contact Person");
        phoneInput.setPromptText("Phone Number");
        emailInput.setPromptText("Email Address");
        addressInput.setPromptText("Office Address");

        Button saveBtn = new Button("➕ Add Supplier");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> handleSave());


        Button editBtn = new Button("📝 Edit Selected");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        editBtn.setMaxWidth(Double.MAX_VALUE);

        editBtn.setOnAction(e -> handleEdit());
        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> handleDelete());

        form.getChildren().addAll(formTitle, nameInput, contactInput, phoneInput, emailInput, addressInput, saveBtn, editBtn, deleteBtn);

        VBox exportBox = new VBox(10);
        exportBox.setPadding(new Insets(15));
        exportBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");

        Button exportBtn = new Button("📊 Export Suppliers (CSV)");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> exportToExcel());

        exportBox.getChildren().addAll(new Label("Reports"), exportBtn);
        panel.getChildren().addAll(form, exportBox);

        return panel;
    }

    private void filterData(String searchText) {
        FilteredList<SupplierModel> filteredData = new FilteredList<>(masterData, s -> true);
        filteredData.setPredicate(s -> {
            if (searchText == null || searchText.isEmpty()) return true;
            String lower = searchText.toLowerCase();
            return s.getName().toLowerCase().contains(lower) ||
                    s.getContactPerson().toLowerCase().contains(lower);
        });
        table.setItems(filteredData);
    }
    private void handleEdit() {
        SupplierModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a supplier to edit!").show();
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("📝 Edit Supplier");
        dialog.setHeaderText("Update details for: " + selected.getName());

        TextField editName = new TextField(selected.getName());
        TextField editContact = new TextField(selected.getContactPerson());
        TextField editPhone = new TextField(selected.getPhone());
        TextField editEmail = new TextField(selected.getEmail());
        TextField editAddress = new TextField(selected.getAddress());

        GridPane grid = new GridPane();

        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Company:"), 0, 0); grid.add(editName, 1, 0);
        grid.add(new Label("Contact:"), 0, 1); grid.add(editContact, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(editPhone, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(editEmail, 1, 3);
        grid.add(new Label("Address:"), 0, 4); grid.add(editAddress, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType updateBtnType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtnType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == updateBtnType) {
                selected.setName(editName.getText());
                selected.setContactPerson(editContact.getText());
                selected.setPhone(editPhone.getText());
                selected.setEmail(editEmail.getText());
                selected.setAddress(editAddress.getText());
                try {
                    ApiClient.post("/suppliers", selected);
                    refreshTable();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void setupTableColumns() {

        TableColumn<SupplierModel, String> nameCol = new TableColumn<>("Company");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<SupplierModel, String> contactCol = new TableColumn<>("Contact Person");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        TableColumn<SupplierModel, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<SupplierModel, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // FIXED: Property name must match the field name in SupplierModel (likely "address")
        TableColumn<SupplierModel, String> addressCol = new TableColumn<>("Office Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(200);

        table.getColumns().addAll(nameCol, contactCol, phoneCol, emailCol,addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refreshTable() {
        Platform.runLater(() -> {
            try {
                var response = ApiClient.get("/suppliers");
                List<SupplierModel> suppliers = new Gson().fromJson(response.body(), new TypeToken<List<SupplierModel>>(){}.getType());
                masterData.setAll(suppliers);
                table.setItems(masterData);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void refresh() { refreshTable(); }

    private void handleSave() {
        try {
            SupplierModel s = new SupplierModel();
            s.setName(nameInput.getText());
            s.setContactPerson(contactInput.getText());
            s.setPhone(phoneInput.getText());
            s.setEmail(emailInput.getText());
            s.setAddress(addressInput.getText());

            ApiClient.post("/suppliers", s);
            refreshTable();

            clearForm();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDelete() {
        SupplierModel selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ApiClient.delete("/suppliers/" + selected.getId());
                refreshTable();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("Suppliers.csv");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Company,Contact,Phone,Email");
                for (SupplierModel s : table.getItems()) {
                    writer.println(s.getName() + "," + s.getContactPerson() + "," + s.getPhone() + "," + s.getEmail());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void clearForm() {
        nameInput.clear(); contactInput.clear(); phoneInput.clear(); emailInput.clear(); addressInput.clear();
    }

    public Parent getView() { return root; }

}