package com.inventory.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inventory.client.util.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.List;
import java.util.Map;

public class DashboardView {
    private ScrollPane scrollRoot = new ScrollPane();
    private VBox content = new VBox(20);

    private Label totalProductsLabel = new Label("0");
    private Label lowStockLabel = new Label("0");
    private Label totalValueLabel = new Label("Rs. 0.00");

    private PieChart categoryChart = new PieChart();
    private BarChart<Number, String> stockBarChart;
    private VBox poListContainer = new VBox(10);

    public DashboardView() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f4f7f6;");
        content.setFillWidth(true);

        Label header = new Label("📊 Business Intelligence Dashboard");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // --- 1. Summary Cards ---
        HBox cardsContainer = new HBox(15);
        cardsContainer.setAlignment(Pos.CENTER);
        VBox card1 = createCard("Total Products", totalProductsLabel, "#27ae60");
        VBox card2 = createCard("Low Stock", lowStockLabel, "#e67e22");
        VBox card3 = createCard("Stock Value", totalValueLabel, "#2980b9");
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        cardsContainer.getChildren().addAll(card1, card2, card3);

        // --- 2. Dual Charts Row ---
        HBox chartsRow = new HBox(20);
        VBox pieBox = createContentBox("Category Distribution", categoryChart);
        categoryChart.setPrefHeight(300);
        HBox.setHgrow(pieBox, Priority.ALWAYS);

        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        xAxis.setLabel("Stock Quantity");
        yAxis.setLabel("Products");

        stockBarChart = new BarChart<>(xAxis, yAxis);
        stockBarChart.setLegendVisible(false);
        stockBarChart.setPrefHeight(300);
        stockBarChart.setAnimated(false);

        VBox barBox = createContentBox("Top Stock Items", stockBarChart);
        HBox.setHgrow(barBox, Priority.ALWAYS);
        chartsRow.getChildren().addAll(pieBox, barBox);

        // --- 3. Bottom Row ---
        VBox poBox = createContentBox("📋 Reorder List", poListContainer);
        VBox.setVgrow(poBox, Priority.ALWAYS);

        content.getChildren().addAll(header, cardsContainer, chartsRow, poBox);
        scrollRoot.setContent(content);
        scrollRoot.setFitToWidth(true);
        scrollRoot.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        refresh();
    }

    public void refresh() {
        new Thread(() -> {
            try {
                var summaryRes = ApiClient.get("/products/summary");
                var productsRes = ApiClient.get("/products");
                Platform.runLater(() -> {
                    if (summaryRes.statusCode() == 200) updateSummaryUI(summaryRes.body());
                    if (productsRes.statusCode() == 200) updateBarChart(productsRes.body());
                });
            } catch (Exception e) {
                System.err.println("Dashboard Refresh Error: " + e.getMessage());
            }
        }).start();
    }

    private void addTextInsideBar(javafx.scene.Node barNode, String text) {
        // We use Platform.runLater to ensure the bar is rendered before calculating position
        Platform.runLater(() -> {
            Pane parent = (Pane) barNode.getParent();
            if (parent == null) return;

            Text label = new Text(text);
            label.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
            label.setMouseTransparent(true); // Clicks pass through to the bar

            // Update position whenever the bar moves or resizes
            barNode.boundsInParentProperty().addListener((observable, oldBounds, newBounds) -> {
                label.setLayoutX(newBounds.getMinX() + 10); // Left padding inside bar
                label.setLayoutY(newBounds.getMinY() + (newBounds.getHeight() / 2) + 4);
            });

            parent.getChildren().add(label);
        });
    }

    private String getBarColor(String name) {
        String ln = name.toLowerCase();
        if (ln.contains("pen")) return "#3498db";
        if (ln.contains("paper")) return "#f1c40f";
        if (ln.contains("notebook")) return "#9b59b6";
        if (ln.contains("pencil")) return "#e67e22";
        return "#2ecc71";
    }

    private VBox createCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    private VBox createContentBox(String title, javafx.scene.Node contentNode) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        box.getChildren().addAll(lblTitle, contentNode);
        return box;
    }

    public Parent getView() { return scrollRoot; }
}