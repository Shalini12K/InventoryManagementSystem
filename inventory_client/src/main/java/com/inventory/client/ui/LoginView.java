package com.inventory.client.ui;

import com.inventory.client.util.ApiClient;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LoginView {
    private VBox root = new VBox(20);
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button loginBtn = new Button("Sign In");
    private Button signupBtn = new Button("Create Account");
    private Label statusLabel = new Label();

    public LoginView(Stage stage) {
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 550);
        root.getStyleClass().add("login-container");
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");

        Label title = new Label("Stationary Pro Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);
        usernameField.setStyle("-fx-padding: 10;");

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);
        passwordField.setStyle("-fx-padding: 10;");

        // --- NEW: Enter Key Listeners ---
        // Allows user to press Enter in either field to log in
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(stage);
        });

        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(stage);
        });

        loginBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");
        loginBtn.setPrefWidth(250);
        loginBtn.setOnAction(e -> handleLogin(stage));

        signupBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #27ae60; -fx-cursor: hand; -fx-font-weight: bold;");
        signupBtn.setOnAction(e -> {
            stage.getScene().setRoot(new SignupView(stage).getView());
        });

        root.getChildren().addAll(title, usernameField, passwordField, loginBtn, signupBtn, statusLabel);
    }

    private void handleLogin(Stage stage) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            var response = ApiClient.post("/auth/login", new UserDTO(user, pass));

            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                MainLayout mainLayout = new MainLayout(stage);

                // --- FIX: Specific Screen Fit for 35.6 cm (14-inch) Laptops ---
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

                stage.getScene().setRoot(mainLayout.getView());

                // Apply bounds so it fits your screen exactly
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());

            } else {
                statusLabel.setText("Invalid credentials!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception ex) {
            statusLabel.setText("Server Error: Check if backend is running.");
            ex.printStackTrace();
        }
    }

    public Parent getView() { return root; }

    public static class UserDTO {
        public String username;
        public String password;

        public UserDTO(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}