package io.mibhutt.craftgraph.registry;

import io.mibhutt.craftgraph.model.NodeCategory;
import io.mibhutt.craftgraph.model.NodeDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class NodeRegistry {
    private final Map<String, NodeDefinition> byType = new LinkedHashMap<>();

    public void register(NodeDefinition definition) {
        byType.put(definition.type(), definition);
    }

    public Optional<NodeDefinition> find(String type) {
        return Optional.ofNullable(byType.get(type));
    }

    public List<NodeDefinition> all() {
        return new ArrayList<>(byType.values());
    }

    public Map<NodeCategory, List<NodeDefinition>> byCategory() {
        return byType.values().stream()
            .collect(Collectors.groupingBy(
                NodeDefinition::category,
                LinkedHashMap::new,
                Collectors.collectingAndThen(Collectors.toList(), list ->
                    list.stream().sorted(Comparator.comparing(NodeDefinition::displayName)).toList())
            ));
    }
}
