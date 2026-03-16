package io.mibhutt.craftgraph.ui;

import io.mibhutt.craftgraph.model.Node;
import io.mibhutt.craftgraph.model.Port;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class NodeView extends VBox {
    private final Node node;
    private final Map<String, Circle> portCircles = new HashMap<>();
    private final VBox body = new VBox(6);

    private double dragStartNodeX;
    private double dragStartNodeY;
    private double dragStartParentX;
    private double dragStartParentY;

    public NodeView(Node node,
                    BiConsumer<Node, MouseEvent> onPressed,
                    TriDrag onDragged,
                    Consumer<Node> onReleased,
                    BiConsumer<Port, Point2D> onPortClicked,
                    Consumer<Node> onCollapseToggle) {
        this.node = node;

        getStyleClass().addAll("graph-node", "node-" + node.category().key());
        setPrefWidth(230);
        setMinWidth(180);

        Label title = new Label(node.displayName());
        title.getStyleClass().add("node-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button collapse = new Button(node.collapsed() ? "+" : "-");
        collapse.getStyleClass().add("collapse-btn");
        collapse.setOnAction(e -> {
            onCollapseToggle.accept(node);
            collapse.setText(node.collapsed() ? "+" : "-");
        });

        HBox header = new HBox(8, title, spacer, collapse);
        header.getStyleClass().add("node-header");
        header.setAlignment(Pos.CENTER_LEFT);

        body.getStyleClass().add("node-body");
        buildPorts(onPortClicked);
        buildValueEditors();

        getChildren().addAll(header, body);

        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!e.isPrimaryButtonDown()) {
                return;
            }
            Object target = e.getTarget();
            if (target instanceof TextInputControl || target instanceof ButtonBase || target instanceof Circle) {
                return;
            }
            dragStartNodeX = node.x();
            dragStartNodeY = node.y();
            Point2D parentPoint = getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
            dragStartParentX = parentPoint.getX();
            dragStartParentY = parentPoint.getY();
            onPressed.accept(node, e);
            e.consume();
        });

        addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!e.isPrimaryButtonDown()) {
                return;
            }
            Point2D parentPoint = getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
            double nx = dragStartNodeX + (parentPoint.getX() - dragStartParentX);
            double ny = dragStartNodeY + (parentPoint.getY() - dragStartParentY);
            onDragged.accept(node, nx, ny);
            e.consume();
        });

        addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            onReleased.accept(node);
            e.consume();
        });

        refreshCollapsed();
    }

    private void buildPorts(BiConsumer<Port, Point2D> onPortClicked) {
        int rows = Math.max(node.inputPorts().size(), node.outputPorts().size());
        for (int i = 0; i < rows; i++) {
            Port input = i < node.inputPorts().size() ? node.inputPorts().get(i) : null;
            Port output = i < node.outputPorts().size() ? node.outputPorts().get(i) : null;

            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);

            if (input != null) {
                row.getChildren().add(portWidget(input, onPortClicked, true));
            } else {
                row.getChildren().add(new Region());
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().add(spacer);

            if (output != null) {
                row.getChildren().add(portWidget(output, onPortClicked, false));
            } else {
                row.getChildren().add(new Region());
            }

            body.getChildren().add(row);
        }
    }

    private HBox portWidget(Port port, BiConsumer<Port, Point2D> onPortClicked, boolean leftSide) {
        Circle circle = new Circle(5);
        circle.getStyleClass().addAll("port", "port-" + port.kind().name().toLowerCase());

        Label label = new Label(port.name());
        label.getStyleClass().add("port-label");

        HBox box = new HBox(4);
        if (leftSide) {
            box.getChildren().addAll(circle, label);
        } else {
            box.getChildren().addAll(label, circle);
        }

        circle.setOnMouseClicked(e -> {
            var bounds = circle.localToScene(circle.getBoundsInLocal());
            Point2D worldPoint = getParent().sceneToLocal(
                bounds.getMinX() + bounds.getWidth() * 0.5,
                bounds.getMinY() + bounds.getHeight() * 0.5
            );
            onPortClicked.accept(port, worldPoint);
            e.consume();
        });

        portCircles.put(port.id(), circle);
        return box;
    }

    private void buildValueEditors() {
        VBox editorBox = new VBox(4);
        editorBox.getStyleClass().add("node-editor-box");

        switch (node.type()) {
            case "data.text" -> addTextPropertyEditor(editorBox, "Text", "value", node.valueOrDefault("value", ""));
            case "data.number" -> addTextPropertyEditor(editorBox, "Number", "value", node.valueOrDefault("value", "1"));
            case "logic.delay_timer" -> addTextPropertyEditor(editorBox, "Ticks", "ticks", node.valueOrDefault("ticks", "20"));
            case "logic.random_chance" -> addTextPropertyEditor(editorBox, "Chance", "chance", node.valueOrDefault("chance", "0.5"));
            case "action.send_message" -> addTextPropertyEditor(editorBox, "Default Text", "text", node.valueOrDefault("text", "Hello"));
            case "action.run_command" -> addTextPropertyEditor(editorBox, "Command", "command", node.valueOrDefault("command", "say Hello"));
            case "action.play_sound" -> addTextPropertyEditor(editorBox, "Sound", "sound", node.valueOrDefault("sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
            case "action.spawn_entity" -> addTextPropertyEditor(editorBox, "Entity", "entityType", node.valueOrDefault("entityType", "ZOMBIE"));
            default -> {
                return;
            }
        }

        body.getChildren().add(editorBox);
    }

    private void addTextPropertyEditor(VBox editorBox, String labelText, String key, String initialValue) {
        Label label = new Label(labelText);
        label.getStyleClass().add("node-prop-label");

        TextField input = new TextField(initialValue);
        input.getStyleClass().add("node-prop-input");
        input.textProperty().addListener((obs, oldValue, newValue) -> node.setValue(key, newValue));

        editorBox.getChildren().addAll(label, input);
    }

    public Point2D portCenterInWorld(String portId) {
        Circle circle = portCircles.get(portId);
        if (circle == null || !isVisible()) {
            return null;
        }
        var bounds = circle.localToScene(circle.getBoundsInLocal());
        return getParent().sceneToLocal(
            bounds.getMinX() + bounds.getWidth() * 0.5,
            bounds.getMinY() + bounds.getHeight() * 0.5
        );
    }

    public void setSelected(boolean selected) {
        pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("selected"), selected);
    }

    public void refreshCollapsed() {
        body.setVisible(!node.collapsed());
        body.setManaged(!node.collapsed());
    }

    @FunctionalInterface
    public interface TriDrag {
        void accept(Node node, double x, double y);
    }
}
