package io.veltrix.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import io.veltrix.export.PluginExporter;
import io.veltrix.graph.GraphManager;
import io.veltrix.model.NodeCategory;
import io.veltrix.model.NodeDefinition;
import io.veltrix.registry.DefaultNodes;
import io.veltrix.registry.NodeRegistry;

public final class MainWindow extends BorderPane {
    private final GraphManager graph = new GraphManager();
    private final NodeRegistry registry = DefaultNodes.createRegistry();
    private final NodeCanvas canvas = new NodeCanvas(graph, registry);
    private final Label status = new Label("Ready");
    private final TextField pluginNameField = new TextField("Veltrix");
    private final TextField packageNameField = new TextField("io.veltrix.generated");

    public MainWindow() {
        getStyleClass().add("app-root");
        setPadding(new Insets(10));

        setTop(buildToolbar());
        setLeft(buildNodePalette());
        setCenter(canvas);
        setBottom(buildStatusBar());

        canvas.setStatusConsumer(status::setText);
    }

    private HBox buildToolbar() {
        pluginNameField.setPromptText("Plugin Name");
        pluginNameField.setPrefWidth(220);
        packageNameField.setPromptText("Package");
        packageNameField.setPrefWidth(260);

        Button groupBtn = new Button("Group Selection");
        groupBtn.setOnAction(e -> canvas.groupSelection());

        Button duplicateBtn = new Button("Duplicate");
        duplicateBtn.setOnAction(e -> canvas.duplicateSelection());

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> canvas.deleteSelection());

        Button exportBtn = new Button("Export Plugin");
        exportBtn.getStyleClass().add("accent-btn");
        exportBtn.setOnAction(e -> exportPlugin(false));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8,
            new Label("Plugin:"), pluginNameField,
            new Label("Package:"), packageNameField,
            new Separator(),
            groupBtn, duplicateBtn, deleteBtn,
            spacer,
            exportBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("toolbar");
        return bar;
    }

    private VBox buildNodePalette() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("node-palette");
        panel.setPadding(new Insets(8));
        panel.setPrefWidth(280);

        Button exportCompileBtn = new Button("Export + Compile");
        exportCompileBtn.getStyleClass().add("accent-btn");
        exportCompileBtn.setMaxWidth(Double.MAX_VALUE);
        exportCompileBtn.setOnAction(e -> exportPlugin(true));

        TextField searchField = new TextField();
        searchField.setPromptText("Search nodes...");

        VBox list = new VBox(10);
        list.setFillWidth(true);
        populateNodePaletteList(list, "");

        searchField.textProperty().addListener((obs, oldValue, newValue) ->
            populateNodePaletteList(list, newValue)
        );

        ScrollPane listScroll = new ScrollPane(list);
        listScroll.setFitToWidth(true);
        listScroll.setFitToHeight(true);
        listScroll.setPannable(true);
        listScroll.getStyleClass().add("palette-scroll");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        panel.getChildren().addAll(exportCompileBtn, searchField, listScroll);
        return panel;
    }

    private void populateNodePaletteList(VBox list, String queryRaw) {
        list.getChildren().clear();
        String query = queryRaw == null ? "" : queryRaw.trim().toLowerCase(Locale.ROOT);

        registry.byCategory().entrySet().stream()
            .sorted(Comparator.comparingInt(e -> categoryOrder(e.getKey())))
            .forEach(entry -> {
                var matching = entry.getValue().stream()
                    .filter(def -> query.isEmpty()
                        || def.displayName().toLowerCase(Locale.ROOT).contains(query)
                        || def.type().toLowerCase(Locale.ROOT).contains(query)
                        || entry.getKey().name().toLowerCase(Locale.ROOT).contains(query))
                    .toList();

                if (matching.isEmpty()) {
                    return;
                }

                Label title = new Label(entry.getKey().name());
                title.getStyleClass().addAll("palette-category", "cat-" + entry.getKey().key());
                list.getChildren().add(title);

                for (NodeDefinition def : matching) {
                    Button add = new Button(def.displayName());
                    add.getStyleClass().addAll("palette-node", "node-" + def.category().key());
                    add.setMaxWidth(Double.MAX_VALUE);
                    add.setOnAction(e -> canvas.addNode(def.type(), 100 + Math.random() * 250, 100 + Math.random() * 180));
                    list.getChildren().add(add);
                }
            });

        if (list.getChildren().isEmpty()) {
            Label empty = new Label("No nodes found");
            empty.getStyleClass().add("palette-category");
            list.getChildren().add(empty);
        }
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox(10, status);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("statusbar");
        return bar;
    }

    private int categoryOrder(NodeCategory category) {
        return switch (category) {
            case EVENT -> 0;
            case LOGIC -> 1;
            case ACTION -> 2;
            case DATA -> 3;
        };
    }

    private void exportPlugin(boolean compileAfterExport) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Export Plugin Project");
            var dir = chooser.showDialog(getScene().getWindow());
            if (dir == null) {
                return;
            }

            PluginExporter exporter = new PluginExporter();
            String pluginName = pluginNameField.getText();
            String packageName = packageNameField.getText();
            if (!isValidPackage(packageName)) {
                throw new IllegalArgumentException("Invalid Java package name: " + packageName);
            }

            PluginExporter.ExportResult result = exporter.export(graph, Path.of(dir.toURI()), packageName, pluginName);
            if (!Files.exists(result.outputDir())) {
                throw new IllegalStateException("Export completed but output folder was not created: " + result.outputDir());
            }

            if (compileAfterExport) {
                status.setText("Exported. Compiling plugin project...");
                compileExportedPlugin(result.outputDir());
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Complete");
            alert.setHeaderText("Plugin project generated");
            alert.setContentText("Output: " + result.outputDir() + "\nFiles: " + String.join(", ", result.generatedFiles()));
            alert.showAndWait();
            status.setText("Exported plugin to " + result.outputDir());
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Unable to export plugin");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            status.setText("Export failed: " + ex.getMessage());
        }
    }

    private void compileExportedPlugin(Path outputDir) {
        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("mvn", "-q", "-DskipTests", "package");
                pb.directory(outputDir.toFile());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                String output;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    output = reader.lines().collect(Collectors.joining("\n"));
                }
                int exitCode = process.waitFor();

                String resultOutput = output;
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Compile Complete");
                        alert.setHeaderText("Plugin exported and compiled");
                        alert.setContentText("Output: " + outputDir + "\nJar should be in target/\n\n" + trimForDialog(resultOutput));
                        alert.showAndWait();
                        status.setText("Compile finished successfully: " + outputDir.resolve("target"));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Compile Failed");
                        alert.setHeaderText("Plugin export succeeded, compile failed");
                        alert.setContentText(trimForDialog(resultOutput));
                        alert.showAndWait();
                        status.setText("Compile failed. See error dialog.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Compile Failed");
                    alert.setHeaderText("Unable to run Maven in exported project");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    status.setText("Compile failed: " + ex.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String trimForDialog(String output) {
        int max = 1400;
        if (output == null || output.isBlank()) {
            return "No compiler output.";
        }
        if (output.length() <= max) {
            return output;
        }
        return output.substring(output.length() - max);
    }

    private boolean isValidPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return false;
        }
        return packageName.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }
}
