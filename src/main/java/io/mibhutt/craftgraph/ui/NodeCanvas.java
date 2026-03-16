package io.mibhutt.craftgraph.ui;

import io.mibhutt.craftgraph.graph.GraphManager;
import io.mibhutt.craftgraph.model.Connection;
import io.mibhutt.craftgraph.model.Node;
import io.mibhutt.craftgraph.model.NodeGroup;
import io.mibhutt.craftgraph.model.Port;
import io.mibhutt.craftgraph.model.PortDirection;
import io.mibhutt.craftgraph.model.PortKind;
import io.mibhutt.craftgraph.registry.NodeRegistry;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class NodeCanvas extends StackPane {
    private static final double GRID_SIZE = 32.0;

    private final GraphManager graph;
    private final NodeRegistry registry;
    private final NodeSelection selection = new NodeSelection();

    private final Canvas gridCanvas = new Canvas();
    private final Pane groupLayer = new Pane();
    private final Pane connectionLayer = new Pane();
    private final Pane nodeLayer = new Pane();
    private final Pane overlayLayer = new Pane();
    private final Group world = new Group();

    private final Map<String, NodeView> nodeViews = new HashMap<>();
    private final Map<String, CubicCurve> connectionViews = new HashMap<>();
    private final Map<String, Rectangle> groupBoxes = new HashMap<>();

    private String connectingFromPortId;
    private PortKind connectingKind;
    private CubicCurve temporaryWire;

    private String draggingNodeId;
    private double dragNodeOffsetX;
    private double dragNodeOffsetY;

    private double panStartX;
    private double panStartY;
    private double panOriginX;
    private double panOriginY;

    private double worldOffsetX = 0;
    private double worldOffsetY = 0;
    private double worldScale = 1.0;

    private Point2D boxStart;
    private final Rectangle selectionBox = new Rectangle();

    private Consumer<String> statusConsumer = s -> {};

    public NodeCanvas(GraphManager graph, NodeRegistry registry) {
        this.graph = graph;
        this.registry = registry;

        world.getChildren().addAll(groupLayer, connectionLayer, nodeLayer);
        getChildren().addAll(gridCanvas, world, overlayLayer);

        selectionBox.getStyleClass().add("selection-box");
        selectionBox.setManaged(false);
        selectionBox.setVisible(false);
        overlayLayer.getChildren().add(selectionBox);
        overlayLayer.setPickOnBounds(false);

        groupLayer.setPickOnBounds(false);
        connectionLayer.setPickOnBounds(false);
        nodeLayer.setPickOnBounds(false);

        widthProperty().addListener((obs, oldV, newV) -> {
            gridCanvas.setWidth(newV.doubleValue());
            redrawGrid();
        });
        heightProperty().addListener((obs, oldV, newV) -> {
            gridCanvas.setHeight(newV.doubleValue());
            redrawGrid();
        });

        setFocusTraversable(true);
        setupCanvasInteraction();
        setupKeyboardShortcuts();
        applyWorldTransform();
        redrawGrid();
    }

    public void setStatusConsumer(Consumer<String> statusConsumer) {
        this.statusConsumer = statusConsumer;
    }

    public void addNode(String type, double x, double y) {
        registry.find(type).ifPresent(def -> {
            Node node = def.createNode(x, y);
            graph.addNode(node);
            createNodeView(node);
            refreshConnections();
        });
    }

    public Set<String> selectedNodeIds() {
        return selection.all();
    }

    public void duplicateSelection() {
        if (selection.isEmpty()) {
            return;
        }
        List<Node> created = new ArrayList<>();
        for (String id : selection.all()) {
            created.add(graph.duplicateNode(id, 50, 50));
        }
        selection.clear();
        for (Node node : created) {
            createNodeView(node);
            selection.add(node.id());
        }
        refreshSelectionStyles();
        refreshConnections();
    }

    public void deleteSelection() {
        List<String> ids = new ArrayList<>(selection.all());
        for (String id : ids) {
            graph.removeNode(id);
            NodeView view = nodeViews.remove(id);
            if (view != null) {
                nodeLayer.getChildren().remove(view);
            }
        }
        selection.clear();
        refreshConnections();
        refreshGroups();
    }

    public void groupSelection() {
        if (selection.all().size() < 2) {
            statusConsumer.accept("Select at least two nodes to create a group.");
            return;
        }
        List<Node> selectedNodes = graph.nodes().stream().filter(n -> selection.isSelected(n.id())).toList();
        double minX = selectedNodes.stream().mapToDouble(Node::x).min().orElse(0) - 30;
        double minY = selectedNodes.stream().mapToDouble(Node::y).min().orElse(0) - 50;
        double maxX = selectedNodes.stream().mapToDouble(n -> n.x() + nodeViews.get(n.id()).getWidth()).max().orElse(300) + 30;
        double maxY = selectedNodes.stream().mapToDouble(n -> n.y() + nodeViews.get(n.id()).getHeight()).max().orElse(200) + 30;

        graph.createGroup("Group " + (graph.groups().size() + 1), minX, minY, maxX - minX, maxY - minY,
            selectedNodes.stream().map(Node::id).toList());
        refreshGroups();
    }

    public GraphManager graph() {
        return graph;
    }

    public void reloadAll() {
        nodeLayer.getChildren().clear();
        nodeViews.clear();
        for (Node node : graph.nodes()) {
            createNodeView(node);
        }
        refreshGroups();
        refreshConnections();
    }

    private void setupCanvasInteraction() {
        addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (temporaryWire != null) {
                Point2D worldPos = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
                updateTemporaryWireEnd(worldPos);
            }
        });

        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            requestFocus();
            boolean overNode = isPointerOverNode(e);
            if (!overNode && (e.getTarget() == this || e.getTarget() == gridCanvas || e.getTarget() == overlayLayer)) {
                if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
                    startPanning(e);
                    e.consume();
                } else if (e.getButton() == MouseButton.PRIMARY) {
                    if (connectingFromPortId != null) {
                        cancelTemporaryWire();
                        refreshConnections();
                    }
                    startBoxSelection(e);
                    e.consume();
                }
            }
        });

        addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (draggingNodeId != null && e.isPrimaryButtonDown()) {
                Optional<Node> dragging = graph.findNode(draggingNodeId);
                if (dragging.isPresent()) {
                    Point2D worldPos = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
                    onNodeDragged(dragging.get(), worldPos.getX() - dragNodeOffsetX, worldPos.getY() - dragNodeOffsetY);
                }
                e.consume();
            } else if (e.isMiddleButtonDown() || e.isSecondaryButtonDown()) {
                panTo(e);
                e.consume();
            } else if (temporaryWire != null) {
                Point2D worldPos = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
                updateTemporaryWireEnd(worldPos);
            } else if (boxStart != null) {
                updateBoxSelection(e);
                e.consume();
            }
        });

        addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (draggingNodeId != null) {
                graph.findNode(draggingNodeId).ifPresent(this::onNodeReleased);
                draggingNodeId = null;
            }
            if (boxStart != null) {
                finishBoxSelection();
                e.consume();
            }
            setCursor(Cursor.DEFAULT);
        });

        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaY()) < 0.01) {
                return;
            }
            double factor = Math.exp(-e.getDeltaY() * 0.0015);
            Point2D local = sceneToLocal(e.getSceneX(), e.getSceneY());
            zoomAt(local.getX(), local.getY(), factor);
            e.consume();
        });
    }

    private void setupKeyboardShortcuts() {
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
                if (!selection.isEmpty()) {
                    deleteSelection();
                }
                if (connectionViews.values().stream().anyMatch(CubicCurve::isFocused)) {
                    connectionViews.entrySet().stream().filter(en -> en.getValue().isFocused()).findFirst()
                        .ifPresent(en -> {
                            graph.removeConnection(en.getKey());
                            refreshConnections();
                        });
                }
                e.consume();
            }
            if (e.isControlDown() && e.getCode() == KeyCode.D) {
                duplicateSelection();
                e.consume();
            }
            if (e.isControlDown() && (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS)) {
                zoomAt(getWidth() * 0.5, getHeight() * 0.5, 1.15);
                e.consume();
            }
            if (e.isControlDown() && (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.UNDERSCORE)) {
                zoomAt(getWidth() * 0.5, getHeight() * 0.5, 0.87);
                e.consume();
            }
        });
    }

    private void createNodeView(Node node) {
        NodeView view = new NodeView(node,
            this::onNodePressed,
            this::onNodeDragged,
            this::onNodeReleased,
            this::onPortClicked,
            this::toggleNodeCollapse);
        view.relocate(node.x(), node.y());
        nodeViews.put(node.id(), view);
        nodeLayer.getChildren().add(view);
    }

    private void onNodePressed(Node node, MouseEvent e) {
        requestFocus();
        if (connectingFromPortId != null && e.getButton() == MouseButton.PRIMARY) {
            if (tryConnectToFirstCompatibleInput(node)) {
                cancelTemporaryWire();
                refreshConnections();
                e.consume();
                return;
            }
        }

        Point2D worldPos = sceneToWorld(new Point2D(e.getSceneX(), e.getSceneY()));
        draggingNodeId = node.id();
        dragNodeOffsetX = worldPos.getX() - node.x();
        dragNodeOffsetY = worldPos.getY() - node.y();

        if (!e.isControlDown()) {
            selection.setSingle(node.id());
        } else {
            selection.toggle(node.id());
        }
        refreshSelectionStyles();
    }

    private void onNodeDragged(Node node, double x, double y) {
        NodeView view = nodeViews.get(node.id());
        node.setX(x);
        node.setY(y);
        view.relocate(x, y);
        refreshConnections();
        refreshGroups();
    }

    private void onNodeReleased(Node node) {
        refreshConnections();
    }

    private void onPortClicked(Port port, Point2D worldPoint) {
        if (connectingFromPortId == null) {
            if (port.direction() == PortDirection.OUTPUT) {
                connectingFromPortId = port.id();
                connectingKind = port.kind();
                startTemporaryWire(worldPoint);
                statusConsumer.accept("Connecting from " + port.name() + "...");
            }
            return;
        }

        if (port.direction() == PortDirection.OUTPUT) {
            connectingFromPortId = port.id();
            connectingKind = port.kind();
            startTemporaryWire(worldPoint);
            statusConsumer.accept("Connection source switched to " + port.name() + ".");
            return;
        }

        Optional<Port> maybeStart = graph.findPort(connectingFromPortId);
        if (maybeStart.isPresent() && port.direction() == PortDirection.INPUT) {
            if (graph.connect(connectingFromPortId, port.id()).isPresent()) {
                statusConsumer.accept("Connected " + maybeStart.get().name() + " -> " + port.name());
            } else {
                statusConsumer.accept("Connection rejected: incompatible ports.");
            }
        }
        cancelTemporaryWire();
        refreshConnections();
    }

    private void toggleNodeCollapse(Node node) {
        node.setCollapsed(!node.collapsed());
        NodeView view = nodeViews.get(node.id());
        view.refreshCollapsed();
        refreshConnections();
    }

    private void refreshSelectionStyles() {
        for (Map.Entry<String, NodeView> entry : nodeViews.entrySet()) {
            entry.getValue().setSelected(selection.isSelected(entry.getKey()));
        }
    }

    private void refreshConnections() {
        connectionLayer.getChildren().clear();
        connectionViews.clear();

        for (Connection connection : graph.connections()) {
            Optional<Port> fromPort = graph.findPort(connection.fromPortId());
            Optional<Port> toPort = graph.findPort(connection.toPortId());
            if (fromPort.isEmpty() || toPort.isEmpty()) {
                continue;
            }

            Point2D from = resolvePortWorldCenter(fromPort.get().id());
            Point2D to = resolvePortWorldCenter(toPort.get().id());
            if (from == null || to == null) {
                continue;
            }

            CubicCurve wire = createWire(from, to, connection.kind().name().toLowerCase());
            wire.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    wire.requestFocus();
                    if (e.getClickCount() == 2) {
                        graph.removeConnection(connection.id());
                        refreshConnections();
                    }
                    e.consume();
                }
            });
            connectionViews.put(connection.id(), wire);
            connectionLayer.getChildren().add(wire);
        }

        if (temporaryWire != null && connectingFromPortId != null) {
            connectionLayer.getChildren().add(temporaryWire);
        }
    }

    private CubicCurve createWire(Point2D from, Point2D to, String typeCss) {
        CubicCurve curve = new CubicCurve();
        curve.setStartX(from.getX());
        curve.setStartY(from.getY());
        curve.setEndX(to.getX());
        curve.setEndY(to.getY());
        double dx = Math.max(70, Math.abs(to.getX() - from.getX()) * 0.5);
        curve.setControlX1(from.getX() + dx);
        curve.setControlY1(from.getY());
        curve.setControlX2(to.getX() - dx);
        curve.setControlY2(to.getY());
        curve.getStyleClass().addAll("wire", "wire-" + typeCss);
        curve.setFill(Color.TRANSPARENT);
        curve.setFocusTraversable(true);
        return curve;
    }

    private Point2D resolvePortWorldCenter(String portId) {
        for (NodeView view : nodeViews.values()) {
            Point2D p = view.portCenterInWorld(portId);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    private void startTemporaryWire(Point2D startPoint) {
        String kindCss = connectingKind == PortKind.EXECUTION ? "execution" : "data";
        temporaryWire = createWire(startPoint, startPoint, kindCss);
        refreshConnections();
    }

    private void updateTemporaryWireEnd(Point2D endPoint) {
        Point2D start = resolvePortWorldCenter(connectingFromPortId);
        if (start == null || temporaryWire == null) {
            return;
        }
        temporaryWire.setStartX(start.getX());
        temporaryWire.setStartY(start.getY());
        temporaryWire.setEndX(endPoint.getX());
        temporaryWire.setEndY(endPoint.getY());
        double dx = Math.max(70, Math.abs(endPoint.getX() - start.getX()) * 0.5);
        temporaryWire.setControlX1(start.getX() + dx);
        temporaryWire.setControlY1(start.getY());
        temporaryWire.setControlX2(endPoint.getX() - dx);
        temporaryWire.setControlY2(endPoint.getY());
    }

    private void cancelTemporaryWire() {
        connectingFromPortId = null;
        connectingKind = null;
        temporaryWire = null;
    }

    private boolean tryConnectToFirstCompatibleInput(Node node) {
        Optional<Port> start = graph.findPort(connectingFromPortId);
        if (start.isEmpty()) {
            return false;
        }
        for (Port input : node.inputPorts()) {
            if (graph.connect(start.get().id(), input.id()).isPresent()) {
                statusConsumer.accept("Connected " + start.get().name() + " -> " + input.name());
                return true;
            }
        }
        statusConsumer.accept("No compatible input found on " + node.displayName());
        return false;
    }

    private boolean isPointerOverNode(MouseEvent e) {
        javafx.scene.Node current = e.getPickResult() != null ? e.getPickResult().getIntersectedNode() : null;
        while (current != null) {
            if (current instanceof NodeView) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void startPanning(MouseEvent e) {
        panStartX = e.getSceneX();
        panStartY = e.getSceneY();
        panOriginX = worldOffsetX;
        panOriginY = worldOffsetY;
        setCursor(Cursor.CLOSED_HAND);
    }

    private void panTo(MouseEvent e) {
        worldOffsetX = panOriginX + (e.getSceneX() - panStartX);
        worldOffsetY = panOriginY + (e.getSceneY() - panStartY);
        applyWorldTransform();
    }

    private void zoomAt(double x, double y, double factor) {
        double oldScale = worldScale;
        double newScale = Math.min(2.5, Math.max(0.3, worldScale * factor));
        if (Math.abs(newScale - oldScale) < 0.0001) {
            return;
        }

        double worldX = (x - worldOffsetX) / oldScale;
        double worldY = (y - worldOffsetY) / oldScale;
        worldScale = newScale;
        worldOffsetX = x - worldX * worldScale;
        worldOffsetY = y - worldY * worldScale;
        applyWorldTransform();
    }

    private Point2D sceneToWorld(Point2D scenePoint) {
        Point2D localPoint = world.sceneToLocal(scenePoint);
        return new Point2D(localPoint.getX(), localPoint.getY());
    }

    private void applyWorldTransform() {
        world.setScaleX(worldScale);
        world.setScaleY(worldScale);
        world.setTranslateX(worldOffsetX);
        world.setTranslateY(worldOffsetY);
        redrawGrid();
        refreshConnections();
    }

    private void redrawGrid() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#111420"));
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.setStroke(Color.web("#20283b"));
        gc.setLineWidth(1);

        double scaledGrid = GRID_SIZE * worldScale;
        if (scaledGrid < 8) {
            return;
        }

        double xStart = worldOffsetX % scaledGrid;
        double yStart = worldOffsetY % scaledGrid;

        for (double x = xStart; x < gridCanvas.getWidth(); x += scaledGrid) {
            gc.strokeLine(x, 0, x, gridCanvas.getHeight());
        }
        for (double y = yStart; y < gridCanvas.getHeight(); y += scaledGrid) {
            gc.strokeLine(0, y, gridCanvas.getWidth(), y);
        }
    }

    private void startBoxSelection(MouseEvent e) {
        boxStart = new Point2D(e.getX(), e.getY());
        selectionBox.setVisible(true);
        selectionBox.setX(boxStart.getX());
        selectionBox.setY(boxStart.getY());
        selectionBox.setWidth(1);
        selectionBox.setHeight(1);
    }

    private void updateBoxSelection(MouseEvent e) {
        double x = Math.min(boxStart.getX(), e.getX());
        double y = Math.min(boxStart.getY(), e.getY());
        double w = Math.abs(e.getX() - boxStart.getX());
        double h = Math.abs(e.getY() - boxStart.getY());
        selectionBox.setX(x);
        selectionBox.setY(y);
        selectionBox.setWidth(w);
        selectionBox.setHeight(h);
    }

    private void finishBoxSelection() {
        Bounds rectBounds = selectionBox.localToScene(selectionBox.getBoundsInLocal());
        selection.clear();
        for (Map.Entry<String, NodeView> entry : nodeViews.entrySet()) {
            Bounds nodeBounds = entry.getValue().localToScene(entry.getValue().getBoundsInLocal());
            if (rectBounds.intersects(nodeBounds)) {
                selection.add(entry.getKey());
            }
        }
        refreshSelectionStyles();
        selectionBox.setVisible(false);
        boxStart = null;
    }

    private void refreshGroups() {
        groupLayer.getChildren().clear();
        groupBoxes.clear();

        for (NodeGroup group : graph.groups()) {
            Rectangle rect = new Rectangle(group.x(), group.y(), group.width(), group.height());
            rect.getStyleClass().add("group-box");
            Label label = new Label(group.name() + (group.collapsed() ? " (collapsed)" : ""));
            label.getStyleClass().add("group-title");
            label.relocate(group.x() + 8, group.y() + 6);

            rect.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    group.setCollapsed(!group.collapsed());
                    for (String nodeId : group.nodeIds()) {
                        NodeView nodeView = nodeViews.get(nodeId);
                        if (nodeView != null) {
                            nodeView.setVisible(!group.collapsed());
                        }
                    }
                    refreshConnections();
                    refreshGroups();
                }
                e.consume();
            });

            groupBoxes.put(group.id(), rect);
            groupLayer.getChildren().addAll(rect, label);
        }
    }
}
