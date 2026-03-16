package io.mibhutt.craftgraph.graph;

import io.mibhutt.craftgraph.model.Connection;
import io.mibhutt.craftgraph.model.Node;
import io.mibhutt.craftgraph.model.NodeGroup;
import io.mibhutt.craftgraph.model.Port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GraphManager {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final List<NodeGroup> groups = new ArrayList<>();
    private final Map<String, Port> portsById = new HashMap<>();
    private final ConnectionManager connectionManager = new ConnectionManager();

    public List<Node> nodes() {
        return nodes;
    }

    public List<Connection> connections() {
        return connections;
    }

    public List<NodeGroup> groups() {
        return groups;
    }

    public void addNode(Node node) {
        nodes.add(node);
        for (Port p : node.inputPorts()) {
            portsById.put(p.id(), p);
        }
        for (Port p : node.outputPorts()) {
            portsById.put(p.id(), p);
        }
    }

    public void removeNode(String nodeId) {
        nodes.removeIf(n -> n.id().equals(nodeId));
        portsById.values().removeIf(p -> p.nodeId().equals(nodeId));
        connectionManager.removeConnectionsForNode(nodeId, connections, portsById);
        for (NodeGroup group : groups) {
            group.nodeIds().remove(nodeId);
        }
    }

    public Optional<Node> findNode(String nodeId) {
        return nodes.stream().filter(n -> n.id().equals(nodeId)).findFirst();
    }

    public Optional<Port> findPort(String portId) {
        return Optional.ofNullable(portsById.get(portId));
    }

    public Optional<Connection> connect(String fromPortId, String toPortId) {
        Port from = portsById.get(fromPortId);
        Port to = portsById.get(toPortId);
        Optional<Connection> created = connectionManager.connect(from, to, connections, portsById);
        created.ifPresent(connections::add);
        return created;
    }

    public boolean removeConnection(String connectionId) {
        return connectionManager.removeConnectionById(connectionId, connections);
    }

    public Node duplicateNode(String nodeId, double offsetX, double offsetY) {
        Node original = findNode(nodeId).orElseThrow();
        Node copy = original.duplicate(offsetX, offsetY);
        addNode(copy);
        return copy;
    }

    public NodeGroup createGroup(String name, double x, double y, double width, double height, List<String> nodeIds) {
        NodeGroup group = new NodeGroup(name, x, y, width, height);
        group.nodeIds().addAll(nodeIds);
        for (Node node : nodes) {
            if (group.nodeIds().contains(node.id())) {
                node.setGroupId(group.id());
            }
        }
        groups.add(group);
        return group;
    }

    public void removeGroup(String groupId) {
        groups.removeIf(g -> g.id().equals(groupId));
        for (Node node : nodes) {
            if (groupId.equals(node.groupId())) {
                node.setGroupId(null);
            }
        }
    }

    public List<Connection> outgoingConnectionsForNode(String nodeId) {
        List<Connection> result = new ArrayList<>();
        for (Connection c : connections) {
            Port from = portsById.get(c.fromPortId());
            if (from != null && from.nodeId().equals(nodeId)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Connection> incomingConnectionsForNode(String nodeId) {
        List<Connection> result = new ArrayList<>();
        for (Connection c : connections) {
            Port to = portsById.get(c.toPortId());
            if (to != null && to.nodeId().equals(nodeId)) {
                result.add(c);
            }
        }
        return result;
    }
}
