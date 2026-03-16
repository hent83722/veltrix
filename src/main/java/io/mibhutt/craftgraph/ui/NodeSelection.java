package io.mibhutt.craftgraph.ui;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class NodeSelection {
    private final Set<String> selectedNodeIds = new LinkedHashSet<>();

    public void clear() {
        selectedNodeIds.clear();
    }

    public void setSingle(String nodeId) {
        selectedNodeIds.clear();
        selectedNodeIds.add(nodeId);
    }

    public void toggle(String nodeId) {
        if (!selectedNodeIds.remove(nodeId)) {
            selectedNodeIds.add(nodeId);
        }
    }

    public void add(String nodeId) {
        selectedNodeIds.add(nodeId);
    }

    public boolean isSelected(String nodeId) {
        return selectedNodeIds.contains(nodeId);
    }

    public Set<String> all() {
        return Collections.unmodifiableSet(selectedNodeIds);
    }

    public boolean isEmpty() {
        return selectedNodeIds.isEmpty();
    }
}
