module com.inventory.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    // REQUIRED: This allows the app to open the system browser for Excel downloads
    requires java.desktop;

    // This line gives GSON and JavaFX permission to access your DTOs and UI classes
    opens com.inventory.client.ui to com.google.gson, javafx.fxml, javafx.base;

    // This allows JavaFX to launch the App class
    opens com.inventory.client to javafx.graphics;

    exports com.inventory.client;
}