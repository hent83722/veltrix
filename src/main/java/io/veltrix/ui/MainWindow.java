package io.veltrix.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.veltrix.export.PluginExporter;
import io.veltrix.graph.GraphManager;
import io.veltrix.model.Connection;
import io.veltrix.model.NodeCategory;
import io.veltrix.model.NodeDefinition;
import io.veltrix.model.NodeGroup;
import io.veltrix.model.Port;
import io.veltrix.model.PortDirection;
import io.veltrix.project.ProjectData;
import io.veltrix.project.ProjectRecord;
import io.veltrix.registry.DefaultNodes;
import io.veltrix.registry.NodeRegistry;

public final class MainWindow extends BorderPane {
    private final GraphManager graph = new GraphManager();
    private final NodeRegistry registry = DefaultNodes.createRegistry();
    private final NodeCanvas canvas = new NodeCanvas(graph, registry);
    private final Label status = new Label("Ready");
    private final TextField pluginNameField = new TextField("Veltrix");
    private final TextField packageNameField = new TextField("io.veltrix.generated");
    private String projectPath = null;

    public MainWindow() {
        getStyleClass().add("app-root");
        setPadding(new Insets(10));

        setTop(buildToolbar());
        setLeft(buildNodePalette());
        setCenter(canvas);
        setBottom(buildStatusBar());

        canvas.setStatusConsumer(status::setText);
    }

    public void loadProject(ProjectRecord project) {
        this.projectPath = project.projectPath();
        pluginNameField.setText(project.name());
        packageNameField.setText(project.packageName());
        graph.clear();
        canvas.reloadAll();
        status.setText("Opened project: " + project.name());
    }

    public void loadProject(ProjectData data) {
        this.projectPath = data.projectPath();
        pluginNameField.setText(data.name());
        packageNameField.setText(data.packageName());
        graph.clear();

        Map<String, io.veltrix.model.Node> nodesByKey = new HashMap<>();
        for (ProjectData.NodeData nodeData : data.nodes()) {
            registry.find(nodeData.type()).ifPresent(def -> {
                io.veltrix.model.Node node = def.createNode(nodeData.x(), nodeData.y());
                node.setCollapsed(nodeData.collapsed());
                nodeData.values().forEach(node::setValue);
                graph.addNode(node);
                nodesByKey.put(nodeData.key(), node);
            });
        }

        for (ProjectData.ConnectionData connectionData : data.connections()) {
            io.veltrix.model.Node fromNode = nodesByKey.get(connectionData.fromNodeKey());
            io.veltrix.model.Node toNode = nodesByKey.get(connectionData.toNodeKey());
            if (fromNode == null || toNode == null) {
                continue;
            }
            Optional<Port> fromPort = findPortByName(fromNode, connectionData.fromPortName(), PortDirection.OUTPUT);
            Optional<Port> toPort = findPortByName(toNode, connectionData.toPortName(), PortDirection.INPUT);
            if (fromPort.isPresent() && toPort.isPresent()) {
                graph.connect(fromPort.get().id(), toPort.get().id());
            }
        }

        for (ProjectData.GroupData groupData : data.groups()) {
            List<String> nodeIds = groupData.nodeKeys().stream()
                .map(nodesByKey::get)
                .filter(java.util.Objects::nonNull)
                .map(io.veltrix.model.Node::id)
                .toList();
            NodeGroup group = graph.createGroup(
                groupData.name(),
                groupData.x(),
                groupData.y(),
                groupData.width(),
                groupData.height(),
                nodeIds
            );
            group.setCollapsed(groupData.collapsed());
        }

        canvas.reloadAll();
        status.setText("Opened project: " + data.name());
    }

    public ProjectData captureProjectData(String projectId) {
        List<io.veltrix.model.Node> nodes = new ArrayList<>(graph.nodes());
        Map<String, String> keyByNodeId = new HashMap<>();
        List<ProjectData.NodeData> nodeData = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            io.veltrix.model.Node node = nodes.get(i);
            String key = "n" + i;
            keyByNodeId.put(node.id(), key);
            nodeData.add(new ProjectData.NodeData(
                key,
                node.type(),
                node.x(),
                node.y(),
                node.collapsed(),
                new HashMap<>(node.values())
            ));
        }

        List<ProjectData.ConnectionData> connections = new ArrayList<>();
        for (Connection connection : graph.connections()) {
            Optional<Port> fromPort = graph.findPort(connection.fromPortId());
            Optional<Port> toPort = graph.findPort(connection.toPortId());
            if (fromPort.isEmpty() || toPort.isEmpty()) {
                continue;
            }
            String fromNodeKey = keyByNodeId.get(fromPort.get().nodeId());
            String toNodeKey = keyByNodeId.get(toPort.get().nodeId());
            if (fromNodeKey == null || toNodeKey == null) {
                continue;
            }
            connections.add(new ProjectData.ConnectionData(
                fromNodeKey,
                fromPort.get().name(),
                toNodeKey,
                toPort.get().name()
            ));
        }

        List<ProjectData.GroupData> groups = new ArrayList<>();
        for (NodeGroup group : graph.groups()) {
            List<String> groupNodeKeys = group.nodeIds().stream()
                .map(keyByNodeId::get)
                .filter(java.util.Objects::nonNull)
                .toList();
            groups.add(new ProjectData.GroupData(
                group.name(),
                group.x(),
                group.y(),
                group.width(),
                group.height(),
                group.collapsed(),
                groupNodeKeys
            ));
        }

        return new ProjectData(
            projectId,
            pluginNameField.getText().trim(),
            packageNameField.getText().trim(),
            projectPath,
            nodeData,
            connections,
            groups,
            System.currentTimeMillis()
        );
    }

    private Optional<Port> findPortByName(io.veltrix.model.Node node, String name, PortDirection direction) {
        List<Port> ports = direction == PortDirection.INPUT ? node.inputPorts() : node.outputPorts();
        return ports.stream().filter(p -> p.name().equals(name)).findFirst();
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

        Button compileBtn = new Button("Compile");
        compileBtn.getStyleClass().add("accent-btn");
        compileBtn.setOnAction(e -> compilePlugin());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8,
            new Label("Plugin:"), pluginNameField,
            new Label("Package:"), packageNameField,
            new Separator(),
            groupBtn, duplicateBtn, deleteBtn,
            spacer,
            compileBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("toolbar");
        return bar;
    }

    private VBox buildNodePalette() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("node-palette");
        panel.setPadding(new Insets(8));
        panel.setPrefWidth(280);

        Button compileBtn = new Button("Compile");
        compileBtn.getStyleClass().add("accent-btn");
        compileBtn.setMaxWidth(Double.MAX_VALUE);
        compileBtn.setOnAction(e -> compilePlugin());

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

        panel.getChildren().addAll(compileBtn, searchField, listScroll);
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

    private void compilePlugin() {
        if (projectPath == null || projectPath.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            applyDialogTheme(alert);
            alert.setTitle("No Project Path");
            alert.setHeaderText("Cannot compile");
            alert.setContentText("Project path is not set. Please open a project from the home screen.");
            alert.showAndWait();
            return;
        }

        try {
            Path dir = java.nio.file.Path.of(projectPath);
            PluginExporter exporter = new PluginExporter();
            String pluginName = pluginNameField.getText();
            String packageName = packageNameField.getText();
            if (!isValidPackage(packageName)) {
                throw new IllegalArgumentException("Invalid Java package name: " + packageName);
            }

            PluginExporter.ExportResult result = exporter.export(graph, dir, packageName, pluginName);
            if (!Files.exists(result.outputDir())) {
                throw new IllegalStateException("Compile failed: output folder not created: " + result.outputDir());
            }

            status.setText("Exported. Compiling plugin project...");
            compileExportedPlugin(result.outputDir());
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            applyDialogTheme(alert);
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
                        showCompileResultDialog(true, "Compile succeeded",
                            "Plugin compiled successfully.\n\nOutput: " + outputDir.resolve("target") + "\n\n" + trimForDialog(resultOutput));
                        status.setText("Compile finished successfully: " + outputDir.resolve("target"));
                    } else {
                        showCompileResultDialog(false, "Compile failed", trimForDialog(resultOutput));
                        status.setText("Compile failed. See error dialog.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    applyDialogTheme(alert);
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

    private void showCompileResultDialog(boolean success, String title, String message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        applyDialogTheme(dialog);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStyleClass().add("compile-result-dialog");

        Label headline = new Label(success ? "Build Completed" : "Build Failed");
        headline.getStyleClass().add("compile-result-headline");

        Label statusBadge = new Label(success ? "SUCCESS" : "FAILED");
        statusBadge.getStyleClass().add(success ? "compile-badge-success" : "compile-badge-failed");

        Label subtitle = new Label(success
            ? "Maven finished and produced build artifacts."
            : "Maven reported errors while building this project.");
        subtitle.getStyleClass().add("compile-result-subtitle");

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.getStyleClass().add("compile-output");
        textArea.setStyle("-fx-control-inner-background: #111827; -fx-background-color: #111827; -fx-text-fill: #d0d7e7; -fx-highlight-fill: #2563eb; -fx-highlight-text-fill: #ffffff; -fx-font-family: 'JetBrains Mono';");

        HBox top = new HBox(10, headline, statusBadge);
        top.getStyleClass().add("compile-result-top");

        VBox content = new VBox(10, top, subtitle, textArea);
        content.getStyleClass().add("compile-result-root");
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #101727, #0c1220); -fx-padding: 12;");

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void applyDialogTheme(Dialog<?> dialog) {
        String stylesheet = MainWindow.class.getResource("/styles/app.css").toExternalForm();
        if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }
        dialog.getDialogPane().getStyleClass().add("app-dialog");
        dialog.getDialogPane().setStyle("-fx-background-color: #0d1019; -fx-border-color: #243450;");
    }

    private boolean isValidPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return false;
        }
        return packageName.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }
}
