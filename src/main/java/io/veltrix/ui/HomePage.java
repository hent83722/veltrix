package io.veltrix.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import io.veltrix.project.ProjectRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public final class HomePage extends VBox {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault());

    private final TilePane projectsGrid = new TilePane();

    private Runnable onCreateProject = () -> {};
    private Consumer<ProjectRecord> onOpenProject = p -> {};
    private Consumer<ProjectRecord> onRenameProject = p -> {};
    private Consumer<ProjectRecord> onRevealProject = p -> {};
    private Consumer<ProjectRecord> onDuplicateProject = p -> {};
    private Consumer<ProjectRecord> onDeleteProject = p -> {};
    private String appVersion = "dev";
    private final Label versionLabel = new Label();

    public HomePage() {
        getStyleClass().add("home-root");
        setSpacing(14);
        setPadding(new Insets(18));

        projectsGrid.setHgap(14);
        projectsGrid.setVgap(14);
        projectsGrid.setPrefTileWidth(270);
        projectsGrid.getStyleClass().add("projects-grid");

        Label title = new Label("Veltrix Projects");
        title.getStyleClass().add("home-title");

        Label subtitle = new Label("Create a new project or open an existing one.");
        subtitle.getStyleClass().add("home-subtitle");

        versionLabel.setText("v" + appVersion);
        versionLabel.getStyleClass().add("home-version");

        Button createButton = new Button("New Project");
        createButton.getStyleClass().add("accent-btn");
        createButton.setOnAction(e -> onCreateProject.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, versionLabel, createButton);

        ScrollPane scroll = new ScrollPane(projectsGrid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("home-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, subtitle, scroll);
    }

    public void setOnCreateProject(Runnable onCreateProject) {
        this.onCreateProject = onCreateProject;
    }

    public void setOnOpenProject(Consumer<ProjectRecord> onOpenProject) {
        this.onOpenProject = onOpenProject;
    }

    public void setOnRenameProject(Consumer<ProjectRecord> onRenameProject) {
        this.onRenameProject = onRenameProject;
    }

    public void setOnRevealProject(Consumer<ProjectRecord> onRevealProject) {
        this.onRevealProject = onRevealProject;
    }

    public void setOnDuplicateProject(Consumer<ProjectRecord> onDuplicateProject) {
        this.onDuplicateProject = onDuplicateProject;
    }

    public void setOnDeleteProject(Consumer<ProjectRecord> onDeleteProject) {
        this.onDeleteProject = onDeleteProject;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion == null || appVersion.isBlank() ? "dev" : appVersion.trim();
        versionLabel.setText("v" + this.appVersion);
    }

    public void setProjects(List<ProjectRecord> projects) {
        projectsGrid.getChildren().clear();

        if (projects.isEmpty()) {
            Label empty = new Label("No projects yet. Use File > New Project to get started.");
            empty.getStyleClass().add("home-empty");
            projectsGrid.getChildren().add(empty);
            return;
        }

        for (ProjectRecord project : projects) {
            Button card = new Button();
            card.getStyleClass().add("project-card");
            card.setMaxWidth(280);
            card.setPrefWidth(270);
            card.setMinHeight(140);

            Label name = new Label(project.name());
            name.getStyleClass().add("project-name");

            Label pkg = new Label(project.packageName());
            pkg.getStyleClass().add("project-package");

            Label location = new Label(project.projectPath());
            location.getStyleClass().add("project-location");

            String when = project.updatedAt() > 0
                ? DATE_FORMAT.format(Instant.ofEpochMilli(project.updatedAt()))
                : "unknown";
            Label updated = new Label("Last opened: " + when);
            updated.getStyleClass().add("project-updated");

            Label openHint = new Label("Open Project");
            openHint.getStyleClass().add("project-open-hint");

            Button menuButton = new Button("⋯");
            menuButton.getStyleClass().add("project-menu-btn");

            MenuItem renameItem = new MenuItem("Rename Project");
            renameItem.setOnAction(e -> onRenameProject.accept(project));

            MenuItem revealItem = new MenuItem("Reveal Project Folder");
            revealItem.setOnAction(e -> onRevealProject.accept(project));

            MenuItem duplicateItem = new MenuItem("Duplicate Project");
            duplicateItem.setOnAction(e -> onDuplicateProject.accept(project));

            MenuItem deleteItem = new MenuItem("Delete Project");
            deleteItem.getStyleClass().add("project-menu-delete");
            deleteItem.setOnAction(e -> onDeleteProject.accept(project));
            ContextMenu menu = new ContextMenu(
                renameItem,
                revealItem,
                duplicateItem,
                new SeparatorMenuItem(),
                deleteItem
            );
            menu.getStyleClass().add("project-menu");

            menuButton.setOnAction(e -> {
                if (menu.isShowing()) {
                    menu.hide();
                } else {
                    menu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 6);
                }
                e.consume();
            });

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            Region topSpacer = new Region();
            HBox.setHgrow(topSpacer, Priority.ALWAYS);
            HBox topRow = new HBox(8, name, topSpacer, menuButton);
            topRow.setAlignment(Pos.TOP_LEFT);

            VBox content = new VBox(4, topRow, pkg, location, spacer, updated, openHint);
            content.getStyleClass().add("project-card-content");
            StackPane wrapper = new StackPane(content);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            card.setGraphic(wrapper);
            card.setOnAction(e -> onOpenProject.accept(project));

            projectsGrid.getChildren().add(card);
        }
    }
}
