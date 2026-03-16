package io.mibhutt.craftgraph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Node {
    private final String id;
    private final String type;
    private final String displayName;
    private final NodeCategory category;
    private final List<Port> inputPorts = new ArrayList<>();
    private final List<Port> outputPorts = new ArrayList<>();
    private final Map<String, String> values = new HashMap<>();
    private double x;
    private double y;
    private boolean collapsed;
    private String groupId;

    public Node(String type, String displayName, NodeCategory category, double x, double y) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.displayName = displayName;
        this.category = category;
        this.x = x;
        this.y = y;
        initializeDefaults();
    }

    public Node duplicate(double offsetX, double offsetY) {
        Node copy = new Node(type, displayName, category, x + offsetX, y + offsetY);
        copy.collapsed = collapsed;
        for (Port in : inputPorts) {
            copy.addInputPort(new Port(copy.id(), in.name(), in.direction(), in.kind(), in.type()));
        }
        for (Port out : outputPorts) {
            copy.addOutputPort(new Port(copy.id(), out.name(), out.direction(), out.kind(), out.type()));
        }
        copy.values.putAll(values);
        return copy;
    }

    private void initializeDefaults() {
        switch (type) {
            case "data.text" -> values.put("value", "Hello from CraftGraph");
            case "data.number" -> values.put("value", "1");
            case "logic.delay_timer" -> values.put("ticks", "20");
            case "logic.random_chance" -> values.put("chance", "0.5");
            case "action.send_message" -> values.put("text", "Hello from CraftGraph");
            case "action.run_command" -> values.put("command", "say Hello from CraftGraph");
            case "action.play_sound" -> values.put("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            case "action.spawn_entity" -> values.put("entityType", "ZOMBIE");
            default -> {
            }
        }
    }

    public String id() {
        return id;
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

    public List<Port> inputPorts() {
        return inputPorts;
    }

    public List<Port> outputPorts() {
        return outputPorts;
    }

    public void addInputPort(Port port) {
        inputPorts.add(port);
    }

    public void addOutputPort(Port port) {
        outputPorts.add(port);
    }

    public double x() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean collapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public String groupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String value(String key) {
        return values.get(key);
    }

    public String valueOrDefault(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public void setValue(String key, String value) {
        values.put(key, value);
    }

    public Map<String, String> values() {
        return values;
    }
}
