package io.veltrix;

import io.veltrix.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        AppShell root = new AppShell();
        Scene scene = new Scene(root, 1440, 900);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setTitle("Veltrix - Minecraft Visual Plugin Editor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
