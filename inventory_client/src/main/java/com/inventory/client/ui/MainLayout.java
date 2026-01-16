package com.inventory.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class MainLayout {
    private BorderPane root = new BorderPane();
    private VBox sidebar = new VBox(8); // Tighter spacing for modern feel
    private Stage stage;
    private List<Button> navButtons = new ArrayList<>();

    // View Instances
    private DashboardView dashboardView = new DashboardView();
    private InventoryView inventoryView = new InventoryView();
    private StockHistoryView historyView = new StockHistoryView();
    private SupplierView supplierView = new SupplierView();
    private PurchaseOrderView poView = new PurchaseOrderView();
    private SalesView salesView = new SalesView();

    public MainLayout(Stage stage) {
        this.stage = stage;
        initializeLayout();
        buildSidebar();
        switchView("dashboard");
    }

    private void initializeLayout() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Clean background for the center area
        root.setStyle("-fx-background-color: #f8fafc;");
    }

    private void buildSidebar() {
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setStyle("-fx-background-color: #1e293b;"); // Deeper Navy/Slate
        VBox.setVgrow(sidebar, Priority.ALWAYS);

        // --- Logo Section ---
        VBox logoBox = new VBox(5);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 0, 30, 10));

        Label title = new Label("STATIONERY PRO");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 2px;");

        Label subTitle = new Label("Inventory Management");
        subTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        logoBox.getChildren().addAll(title, subTitle);

        // --- Navigation Buttons ---
        Button btnDash = createNavButton("📊  Dashboard", "dashboard");
        Button btnInv = createNavButton("📦  Manage Stock", "inventory");
        Button btnPO = createNavButton("📋  Purchase Orders", "po");
        Button btnSuppliers = createNavButton("🏢  Suppliers", "suppliers");
        Button btnHistory = createNavButton("📜  Stock History", "history");

        // Special Style for POS Button
        Button btnSales = createNavButton("🛒  Checkout (POS)", "sales");
        btnSales.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 15; -fx-background-radius: 8; -fx-margin-top: 10;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- Logout Button ---
        Button btnLogout = new Button("🚪  Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(logoBox, new Separator(), btnDash, btnInv, btnPO, btnSuppliers, btnHistory,
                new Label(""), btnSales, spacer, btnLogout);
        root.setLeft(sidebar);
    }

    private void switchView(String viewKey) {
        // Reset all button styles to default before highlighting the active one
        updateButtonHighlight(viewKey);

        switch (viewKey.toLowerCase()) {
            case "dashboard":
                dashboardView.refresh();
                root.setCenter(dashboardView.getView());
                break;
            case "inventory":
                inventoryView.refreshTable();
                root.setCenter(inventoryView.getView());
                break;
            case "sales":
                salesView.refresh();
                root.setCenter(salesView.getView());
                break;
            case "po":
                poView.refresh();
                root.setCenter(poView.getView());
                break;
            case "suppliers":
                supplierView.refresh();
                root.setCenter(supplierView.getView());
                break;
            case "history":
                historyView.refresh();
                root.setCenter(historyView.getView());
                break;
        }
    }

    private Button createNavButton(String text, String viewKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setUserData(viewKey); // Store key for identification

        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 14px; " +
                "-fx-padding: 12 15; -fx-cursor: hand; -fx-background-radius: 8;";
        btn.setStyle(defaultStyle);

        btn.setOnAction(e -> switchView(viewKey));

        // Hover Effects
        btn.setOnMouseEntered(e -> {
            if (!root.getCenter().equals(getViewByKey(viewKey)) && !viewKey.equals("sales")) {
                btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-padding: 12 15; -fx-background-radius: 8;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!root.getCenter().equals(getViewByKey(viewKey)) && !viewKey.equals("sales")) {
                btn.setStyle(defaultStyle);
            } else if (viewKey.equals("sales")) {
                btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 8;");
            }
        });

        navButtons.add(btn);
        return btn;
    }

    private void updateButtonHighlight(String activeKey) {
        for (Button btn : navButtons) {
            String key = (String) btn.getUserData();
            if (key.equals(activeKey)) {
                if (key.equals("sales")) {
                    btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.4), 10, 0, 0, 0);");
                } else {
                    btn.setStyle("-fx-background-color: #334155; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 0 0 0 4;");
                }
            } else {
                if (key.equals("sales")) {
                    btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 8;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-padding: 12 15; -fx-cursor: hand; -fx-background-radius: 8;");
                }
            }
        }
    }

    // Helper to identify view nodes
    private Parent getViewByKey(String key) {
        return switch (key) {
            case "dashboard" -> dashboardView.getView();
            case "inventory" -> inventoryView.getView();
            case "sales" -> salesView.getView();
            case "po" -> poView.getView();
            case "suppliers" -> supplierView.getView();
            case "history" -> historyView.getView();
            default -> null;
        };
    }

    private void handleLogout() {
        stage.setMaximized(false);
        LoginView loginView = new LoginView(stage);
        stage.getScene().setRoot(loginView.getView());
        stage.setWidth(420);
        stage.setHeight(550);
        stage.centerOnScreen();
    }

    public Parent getView() { return root; }
}