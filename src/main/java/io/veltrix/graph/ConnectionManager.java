package io.veltrix.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.veltrix.model.Connection;
import io.veltrix.model.Port;
import io.veltrix.model.PortDirection;
import io.veltrix.model.PortType;

public final class ConnectionManager {
    public Optional<Connection> connect(Port from, Port to, List<Connection> existing, Map<String, Port> portsById) {
        if (from == null || to == null) {
            return Optional.empty();
        }
        if (from.direction() != PortDirection.OUTPUT || to.direction() != PortDirection.INPUT) {
            return Optional.empty();
        }
        if (from.kind() != to.kind()) {
            return Optional.empty();
        }
        if (!typeCompatible(from.type(), to.type())) {
            return Optional.empty();
        }

        List<Connection> toRemove = new ArrayList<>();
        for (Connection c : existing) {
            if (c.toPortId().equals(to.id())) {
                toRemove.add(c);
            }
            if (c.fromPortId().equals(from.id()) && c.toPortId().equals(to.id())) {
                return Optional.empty();
            }
        }
        existing.removeAll(toRemove);
        return Optional.of(new Connection(from.id(), to.id(), from.kind()));
    }

    public boolean removeConnectionById(String connectionId, List<Connection> existing) {
        return existing.removeIf(c -> c.id().equals(connectionId));
    }

    public void removeConnectionsForNode(String nodeId, List<Connection> existing, Map<String, Port> portsById) {
        existing.removeIf(c -> {
            Port from = portsById.get(c.fromPortId());
            Port to = portsById.get(c.toPortId());
            return (from != null && from.nodeId().equals(nodeId)) || (to != null && to.nodeId().equals(nodeId));
        });
    }

    private boolean typeCompatible(PortType from, PortType to) {
        return from == to || from == PortType.ANY || to == PortType.ANY;
    }
}
