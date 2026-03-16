package io.mibhutt.craftgraph.registry;

import io.mibhutt.craftgraph.model.NodeCategory;
import io.mibhutt.craftgraph.model.NodeDefinition;
import io.mibhutt.craftgraph.model.PortKind;
import io.mibhutt.craftgraph.model.PortType;

public final class DefaultNodes {
    private DefaultNodes() {}

    public static NodeRegistry createRegistry() {
        NodeRegistry registry = new NodeRegistry();

        registerEvents(registry);
        registerLogic(registry);
        registerActions(registry);
        registerData(registry);

        return registry;
    }

    private static void registerEvents(NodeRegistry registry) {
        registry.register(NodeDefinition.builder("event.plugin_enable", "Plugin Enable", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("event.plugin_disable", "Plugin Disable", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("event.player_join", "Player Join", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.player_leave", "Player Leave", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.player_break_block", "Player Break Block", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("event.player_place_block", "Player Place Block", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("event.player_chat", "Player Chat", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Message", PortKind.DATA, PortType.TEXT)
            .build());
        registry.register(NodeDefinition.builder("event.server_tick", "Server Tick", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
    }

    private static void registerLogic(NodeRegistry registry) {
        registry.register(NodeDefinition.builder("logic.if_condition", "If Condition", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Condition", PortKind.DATA, PortType.BOOLEAN)
            .output("True", PortKind.EXECUTION, PortType.FLOW)
            .output("False", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.compare_values", "Compare Values", NodeCategory.LOGIC)
            .input("A", PortKind.DATA, PortType.ANY)
            .input("B", PortKind.DATA, PortType.ANY)
            .output("Equal", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.boolean_and", "Boolean And", NodeCategory.LOGIC)
            .input("A", PortKind.DATA, PortType.BOOLEAN)
            .input("B", PortKind.DATA, PortType.BOOLEAN)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.boolean_or", "Boolean Or", NodeCategory.LOGIC)
            .input("A", PortKind.DATA, PortType.BOOLEAN)
            .input("B", PortKind.DATA, PortType.BOOLEAN)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.boolean_not", "Boolean Not", NodeCategory.LOGIC)
            .input("Value", PortKind.DATA, PortType.BOOLEAN)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.delay_timer", "Delay Timer", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Ticks", PortKind.DATA, PortType.NUMBER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.random_chance", "Random Chance", NodeCategory.LOGIC)
            .input("Chance", PortKind.DATA, PortType.NUMBER)
            .output("Success", PortKind.DATA, PortType.BOOLEAN)
            .build());
    }

    private static void registerActions(NodeRegistry registry) {
        registry.register(NodeDefinition.builder("action.send_message", "Send Message", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Text", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.spawn_entity", "Spawn Entity", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Location", PortKind.DATA, PortType.LOCATION)
            .input("Entity Type", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.give_item", "Give Item", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.teleport_player", "Teleport Player", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Location", PortKind.DATA, PortType.LOCATION)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.play_sound", "Play Sound", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Sound", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.run_command", "Run Command", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Command", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
    }

    private static void registerData(NodeRegistry registry) {
        registry.register(NodeDefinition.builder("data.player", "Player Object", NodeCategory.DATA)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("data.location", "Location", NodeCategory.DATA)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("data.itemstack", "ItemStack", NodeCategory.DATA)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("data.number", "Number", NodeCategory.DATA)
            .output("Value", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("data.text", "Text", NodeCategory.DATA)
            .output("Text", PortKind.DATA, PortType.TEXT)
            .build());
    }
}
