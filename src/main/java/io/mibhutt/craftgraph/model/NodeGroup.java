package io.mibhutt.craftgraph.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class NodeGroup {
    private final String id;
    private String name;
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean collapsed;
    private final Set<String> nodeIds = new HashSet<>();

    public NodeGroup(String name, double x, double y, double width, double height) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double width() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double height() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean collapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public Set<String> nodeIds() {
        return nodeIds;
    }
}
