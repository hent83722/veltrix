package io.mibhutt.craftgraph.model;

import java.util.UUID;

public final class Port {
    private final String id;
    private final String nodeId;
    private final String name;
    private final PortDirection direction;
    private final PortKind kind;
    private final PortType type;

    public Port(String nodeId, String name, PortDirection direction, PortKind kind, PortType type) {
        this.id = UUID.randomUUID().toString();
        this.nodeId = nodeId;
        this.name = name;
        this.direction = direction;
        this.kind = kind;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public String nodeId() {
        return nodeId;
    }

    public String name() {
        return name;
    }

    public PortDirection direction() {
        return direction;
    }

    public PortKind kind() {
        return kind;
    }

    public PortType type() {
        return type;
    }
}
