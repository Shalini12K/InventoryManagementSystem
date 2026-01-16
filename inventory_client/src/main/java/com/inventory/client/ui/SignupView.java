package com.inventory.client.ui;

import com.inventory.client.util.ApiClient;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class SignupView {
    private VBox root = new VBox(20);
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private PasswordField confirmPasswordField = new PasswordField();
    private Button registerBtn = new Button("Register");
    private Button backBtn = new Button("Back to Login");
    private Label statusLabel = new Label();

    public SignupView(Stage stage) {
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 500);
        root.getStyleClass().add("login-container");
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        usernameField.setPromptText("Choose Username");
        usernameField.setMaxWidth(250);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(250);

        registerBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");
        registerBtn.getStyleClass().add("button-primary");
        registerBtn.setPrefWidth(250);
        registerBtn.setOnAction(e -> handleSignup(stage));

        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #27ae60; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            stage.getScene().setRoot(new LoginView(stage).getView());
        });

        root.getChildren().addAll(title, usernameField, passwordField, confirmPasswordField, registerBtn, backBtn, statusLabel);
    }

    private void handleSignup(Stage stage) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("All fields are required!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!pass.equals(confirm)) {
            statusLabel.setText("Passwords do not match!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            var response = ApiClient.post("/auth/signup", new UserDTO(user, pass));
            if (response.statusCode() == 200) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registration Successful! You can now login.");
                alert.showAndWait();
                stage.getScene().setRoot(new LoginView(stage).getView());
            } else {
                statusLabel.setText("Error: " + response.body());
            }
        } catch (Exception ex) {
            statusLabel.setText("Connection failed!");
        }
    }

    public Parent getView() { return root; }

    // DTO for JSON Mapping
    public static class UserDTO {
        public String username;
        public String password;
        public UserDTO(String u, String p) { this.username = u; this.password = p; }
    }
}