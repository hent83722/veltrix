package io.veltrix.model;

import java.util.UUID;

public final class Connection {
    private final String id;
    private final String fromPortId;
    private final String toPortId;
    private final PortKind kind;

    public Connection(String fromPortId, String toPortId, PortKind kind) {
        this.id = UUID.randomUUID().toString();
        this.fromPortId = fromPortId;
        this.toPortId = toPortId;
        this.kind = kind;
    }

    public String id() {
        return id;
    }

    public String fromPortId() {
        return fromPortId;
    }

    public String toPortId() {
        return toPortId;
    }

    public PortKind kind() {
        return kind;
    }
}
