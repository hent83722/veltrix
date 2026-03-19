package io.veltrix.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Optional;

import io.veltrix.project.ProjectData;
import io.veltrix.project.ProjectRecord;
import io.veltrix.project.ProjectStore;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public final class AppShell extends BorderPane {
    private static final Duration AUTO_SAVE_INTERVAL = Duration.seconds(10);

    private final ProjectStore projectStore = new ProjectStore();
    private final HomePage homePage = new HomePage();
    private final MainWindow mainWindow = new MainWindow();
    private final Timeline autoSaveTimeline;
    private final String appVersion = loadAppVersion();
    private ProjectRecord activeProject;

    public AppShell() {
        getStyleClass().add("app-root");

        setTop(buildMenuBar());
        setCenter(homePage);

        homePage.setAppVersion(appVersion);
        homePage.setOnCreateProject(this::createProjectDialog);
        homePage.setOnOpenProject(this::openProject);
        homePage.setOnRenameProject(this::renameProjectDialog);
        homePage.setOnRevealProject(this::revealProjectFolder);
        homePage.setOnDuplicateProject(this::duplicateProject);
        homePage.setOnDeleteProject(this::confirmDeleteProject);
        refreshProjects();

        autoSaveTimeline = new Timeline(new KeyFrame(AUTO_SAVE_INTERVAL, e -> autoSaveActiveProject()));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimeline.play();
    }

    private MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");

        MenuItem newProjectItem = new MenuItem("New Project...");
        newProjectItem.setOnAction(e -> createProjectDialog());

        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> {
            refreshProjects();
            setCenter(homePage);
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> getScene().getWindow().hide());

        fileMenu.getItems().addAll(newProjectItem, homeItem, exitItem);

        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.getStyleClass().add("app-menu");
        return menuBar;
    }

    private void refreshProjects() {
        homePage.setProjects(projectStore.listProjects());
    }

    private void renameProjectDialog(ProjectRecord project) {
        TextInputDialog dialog = new TextInputDialog(project.name());
        dialog.setTitle("Rename Project");
        dialog.setHeaderText("Rename project");
        dialog.setContentText("New project name:");
        applyDialogTheme(dialog);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        ProjectRecord renamed = projectStore.renameProject(project.id(), result.get());
        if (activeProject != null && activeProject.id().equals(project.id())) {
            activeProject = renamed;
        }
        refreshProjects();
    }

    private void revealProjectFolder(ProjectRecord project) {
        Thread thread = new Thread(() -> {
            try {
                Path path = Path.of(project.projectPath());
                Files.createDirectories(path);
                openInFileExplorer(path);
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    applyDialogTheme(alert);
                    alert.setTitle("Reveal Folder Failed");
                    alert.setHeaderText("Unable to open project folder");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }, "reveal-project-folder");
        thread.setDaemon(true);
        thread.start();
    }

    private void openInFileExplorer(Path path) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<List<String>> candidates = new ArrayList<>();

        if (os.contains("win")) {
            candidates.add(Arrays.asList("explorer.exe", path.toString()));
        } else if (os.contains("mac")) {
            candidates.add(Arrays.asList("open", path.toString()));
        } else {
            candidates.add(Arrays.asList("xdg-open", path.toString()));
            candidates.add(Arrays.asList("gio", "open", path.toString()));
            candidates.add(Arrays.asList("kde-open5", path.toString()));
            candidates.add(Arrays.asList("kde-open", path.toString()));
            candidates.add(Arrays.asList("gnome-open", path.toString()));
        }

        IOException last = null;
        for (List<String> command : candidates) {
            try {
                new ProcessBuilder(command).start();
                return;
            } catch (IOException ex) {
                last = ex;
            }
        }

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(path.toFile());
            return;
        }

        if (last != null) {
            throw new IOException("No supported file explorer command was found on this system.", last);
        }
        throw new IOException("No supported file explorer command was found on this system.");
    }

    private void duplicateProject(ProjectRecord project) {
        ProjectRecord duplicate = projectStore.duplicateProject(project);
        refreshProjects();
        openProject(duplicate);
    }

    private void confirmDeleteProject(ProjectRecord project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        applyDialogTheme(alert);
        alert.setTitle("Delete Project");
        alert.setHeaderText("Delete project from Veltrix?");
        alert.setContentText("This removes the project from the home screen and deletes its saved editor data. Files in the project folder are left untouched.\n\nProject: " + project.name());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        projectStore.deleteProject(project.id());
        if (activeProject != null && activeProject.id().equals(project.id())) {
            activeProject = null;
            setCenter(homePage);
        }
        refreshProjects();
    }

    private void openProject(ProjectRecord project) {
        activeProject = project;
        Optional<ProjectData> savedData = projectStore.loadProjectData(project.id());
        if (savedData.isPresent()) {
            mainWindow.loadProject(savedData.get());
        } else {
            projectStore.touch(project);
            mainWindow.loadProject(project);
        }
        setCenter(mainWindow);
        refreshProjects();
    }

    private void autoSaveActiveProject() {
        if (activeProject == null || getCenter() != mainWindow) {
            return;
        }
        try {
            ProjectData snapshot = mainWindow.captureProjectData(activeProject.id());
            projectStore.saveProjectData(snapshot);
            activeProject = new ProjectRecord(
                snapshot.projectId(),
                snapshot.name(),
                snapshot.packageName(),
                activeProject.projectPath(),
                snapshot.updatedAt()
            );
        } catch (Exception ex) {
        }
    }

    private void createProjectDialog() {
        Dialog<ProjectRecord> dialog = new Dialog<>();
        dialog.setTitle("New Project");
        applyDialogTheme(dialog);

        ButtonType createType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);
        dialog.getDialogPane().getStyleClass().add("new-project-dialog");

        TextField nameField = new TextField("untitled");
        nameField.setPromptText("untitled");

        TextField locationField = new TextField(projectStore.defaultProjectsRoot().toString());
        locationField.setPromptText("Project location");

        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose Project Location");
            java.io.File current = new java.io.File(locationField.getText().trim());
            if (current.exists() && current.isDirectory()) {
                chooser.setInitialDirectory(current);
            }
            java.io.File selected = chooser.showDialog(getScene().getWindow());
            if (selected != null) {
                locationField.setText(selected.getAbsolutePath());
            }
        });

        TextField packageField = new TextField("io.veltrix.generated");
        packageField.setPromptText("io.example.plugin");

        Label previewLabel = new Label();
        previewLabel.getStyleClass().add("new-project-preview");

        Runnable updatePreview = () -> {
            String name = nameField.getText() == null || nameField.getText().isBlank() ? "untitled" : nameField.getText().trim();
            String location = locationField.getText() == null ? "" : locationField.getText().trim();
            String createdIn = location.isBlank() ? name : java.nio.file.Path.of(location).resolve(name).toString();
            previewLabel.setText("Project will be created in: " + createdIn);
        };
        nameField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());
        locationField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());
        updatePreview.run();

        Label nameLabel = new Label("Name:");
        Label locationLabel = new Label("Location:");
        Label packageLabel = new Label("Package:");
        Label basicsSection = new Label("Project Basics");
        Label packageSection = new Label("Plugin Package");
        Label locationSection = new Label("Storage");
        nameLabel.getStyleClass().add("new-project-label");
        locationLabel.getStyleClass().add("new-project-label");
        packageLabel.getStyleClass().add("new-project-label");
        basicsSection.getStyleClass().add("new-project-section");
        packageSection.getStyleClass().add("new-project-section");
        locationSection.getStyleClass().add("new-project-section");

        Label helper = new Label("Create a clean plugin workspace with your preferred package namespace.");
        helper.getStyleClass().add("new-project-helper");

        HBox locationRow = new HBox(8, locationField, browseButton);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        Region dividerA = new Region();
        Region dividerB = new Region();
        dividerA.getStyleClass().add("new-project-divider");
        dividerB.getStyleClass().add("new-project-divider");

        VBox content = new VBox(8,
            helper,
            basicsSection,
            nameLabel,
            nameField,
            dividerA,
            locationSection,
            locationLabel,
            locationRow,
            dividerB,
            packageSection,
            packageLabel,
            packageField,
            previewLabel
        );
        content.setPadding(new Insets(12));
        content.getStyleClass().add("new-project-pane");
        content.setStyle("-fx-background-color: linear-gradient(to bottom, #101727, #0d1321);");

        String fieldStyle = "-fx-background-color: #1b2438; -fx-text-fill: #d0d7e7; -fx-prompt-text-fill: #7f92bb; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #2e3a56;";
        nameField.setStyle(fieldStyle);
        locationField.setStyle(fieldStyle);
        packageField.setStyle(fieldStyle);
        browseButton.setStyle("-fx-background-color: #1b2438; -fx-text-fill: #d0d7e7; -fx-border-color: #2e3a56; -fx-background-radius: 8; -fx-border-radius: 8;");
        previewLabel.setStyle("-fx-text-fill: #9bb0db; -fx-padding: 9; -fx-background-color: #111b2d; -fx-background-radius: 8; -fx-border-color: #1f2a42; -fx-border-radius: 8;");

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == createType) {
                String baseLocation = locationField.getText() == null ? "" : locationField.getText().trim();
                String projectName = nameField.getText() == null ? "" : nameField.getText().trim();
                String projectPath = java.nio.file.Path.of(baseLocation).resolve(projectName).toString();
                return projectStore.createProject(projectName, packageField.getText(), projectPath);
            }
            return null;
        });

        try {
            Optional<ProjectRecord> created = dialog.showAndWait();
            created.ifPresent(this::openProject);
            refreshProjects();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            applyDialogTheme(alert);
            alert.setTitle("Project Creation Failed");
            alert.setHeaderText("Unable to create project");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void applyDialogTheme(Dialog<?> dialog) {
        String stylesheet = AppShell.class.getResource("/styles/app.css").toExternalForm();
        if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }
        dialog.getDialogPane().getStyleClass().add("app-dialog");
        dialog.getDialogPane().setStyle("-fx-background-color: #0d1019; -fx-border-color: #243450;");
    }

    private String loadAppVersion() {
        try (InputStream in = AppShell.class.getResourceAsStream("/META-INF/maven/io.veltrix/veltrix/pom.properties")) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                String version = properties.getProperty("version", "").trim();
                if (!version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException ignored) {
        }

        Package appPackage = AppShell.class.getPackage();
        if (appPackage != null && appPackage.getImplementationVersion() != null && !appPackage.getImplementationVersion().isBlank()) {
            return appPackage.getImplementationVersion();
        }
        return "dev";
    }
}
