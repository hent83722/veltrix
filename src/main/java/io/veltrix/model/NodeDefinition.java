package io.veltrix.model;

import java.util.ArrayList;
import java.util.List;

public final class NodeDefinition {
    private final String type;
    private final String displayName;
    private final NodeCategory category;
    private final List<PortSpec> inputSpecs;
    private final List<PortSpec> outputSpecs;

    public NodeDefinition(String type, String displayName, NodeCategory category,
                          List<PortSpec> inputSpecs, List<PortSpec> outputSpecs) {
        this.type = type;
        this.displayName = displayName;
        this.category = category;
        this.inputSpecs = List.copyOf(inputSpecs);
        this.outputSpecs = List.copyOf(outputSpecs);
    }

    public String type() {
        return type;
    }

    public String displayName() {
        return displayName;
    }

    public NodeCategory category() {
        return category;
    }

    public List<PortSpec> inputSpecs() {
        return inputSpecs;
    }

    public List<PortSpec> outputSpecs() {
        return outputSpecs;
    }

    public Node createNode(double x, double y) {
        Node node = new Node(type, displayName, category, x, y);
        for (PortSpec spec : inputSpecs) {
            node.addInputPort(new Port(node.id(), spec.name(), PortDirection.INPUT, spec.kind(), spec.type()));
        }
        for (PortSpec spec : outputSpecs) {
            node.addOutputPort(new Port(node.id(), spec.name(), PortDirection.OUTPUT, spec.kind(), spec.type()));
        }
        return node;
    }

    public static Builder builder(String type, String displayName, NodeCategory category) {
        return new Builder(type, displayName, category);
    }

    public record PortSpec(String name, PortKind kind, PortType type) {}

    public static final class Builder {
        private final String type;
        private final String displayName;
        private final NodeCategory category;
        private final List<PortSpec> inputs = new ArrayList<>();
        private final List<PortSpec> outputs = new ArrayList<>();

        private Builder(String type, String displayName, NodeCategory category) {
            this.type = type;
            this.displayName = displayName;
            this.category = category;
        }

        public Builder input(String name, PortKind kind, PortType type) {
            inputs.add(new PortSpec(name, kind, type));
            return this;
        }

        public Builder output(String name, PortKind kind, PortType type) {
            outputs.add(new PortSpec(name, kind, type));
            return this;
        }

        public NodeDefinition build() {
            return new NodeDefinition(type, displayName, category, inputs, outputs);
        }
    }
}
