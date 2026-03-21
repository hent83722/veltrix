package io.veltrix.registry;

import io.veltrix.model.NodeCategory;
import io.veltrix.model.NodeDefinition;
import io.veltrix.model.PortKind;
import io.veltrix.model.PortType;

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
        registry.register(NodeDefinition.builder("event.player_move", "Player Move", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("From", PortKind.DATA, PortType.LOCATION)
            .output("To", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("event.player_death", "Player Death", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Killer", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.player_damage", "Player Damage", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Damage", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("event.player_interact", "Player Interact", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("event.player_drop_item", "Player Drop Item", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("event.player_pickup_item", "Player Pickup Item", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("event.entity_death", "Entity Death", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Entity", PortKind.DATA, PortType.ENTITY)
            .output("Killer", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.block_explode", "Block Explode", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("event.redstone_change", "Redstone Change", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .output("Power", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("event.command_run", "Command Run", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Command", PortKind.DATA, PortType.TEXT)
            .output("Arguments", PortKind.DATA, PortType.TEXT)
            .build());
        registry.register(NodeDefinition.builder("event.server_tick", "Server Tick", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("event.player_shoot_bow", "Player Shoot Bow", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.player_eat", "Player Eat", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .build());
        registry.register(NodeDefinition.builder("event.player_toggle_sneak", "Player Toggle Sneak", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Sneaking", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("event.player_toggle_sprint", "Player Toggle Sprint", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Player", PortKind.DATA, PortType.PLAYER)
            .output("Sprinting", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("event.weather_change", "Weather Change", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("IsStorm", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("event.time_change", "Time Change", NodeCategory.EVENT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .output("Time", PortKind.DATA, PortType.NUMBER)
            .build());
    }

    private static void registerLogic(NodeRegistry registry) {
        registry.register(NodeDefinition.builder("logic.if_condition", "If Condition", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Condition", PortKind.DATA, PortType.BOOLEAN)
            .output("True", PortKind.EXECUTION, PortType.FLOW)
            .output("False", PortKind.EXECUTION, PortType.FLOW)
            .build());
        if (registry.find("logic.loop").isEmpty()) {
            registry.register(NodeDefinition.builder("logic.loop", "Loop", NodeCategory.LOGIC)
                .input("In", PortKind.EXECUTION, PortType.FLOW)
                .input("Count", PortKind.DATA, PortType.NUMBER)
                .output("Loop", PortKind.EXECUTION, PortType.FLOW)
                .output("Done", PortKind.EXECUTION, PortType.FLOW)
                .build());
        }
        if (registry.find("logic.foreach_player").isEmpty()) {
            registry.register(NodeDefinition.builder("logic.foreach_player", "For Each Player", NodeCategory.LOGIC)
                .input("In", PortKind.EXECUTION, PortType.FLOW)
                .output("Loop", PortKind.EXECUTION, PortType.FLOW)
                .output("Player", PortKind.DATA, PortType.PLAYER)
                .output("Done", PortKind.EXECUTION, PortType.FLOW)
                .build());
        }
        if (registry.find("logic.while").isEmpty()) {
            registry.register(NodeDefinition.builder("logic.while", "While Loop", NodeCategory.LOGIC)
                .input("In", PortKind.EXECUTION, PortType.FLOW)
                .input("Condition", PortKind.DATA, PortType.BOOLEAN)
                .output("Loop", PortKind.EXECUTION, PortType.FLOW)
                .output("Done", PortKind.EXECUTION, PortType.FLOW)
                .build());
        }
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
        if (registry.find("logic.random_number").isEmpty()) {
            registry.register(NodeDefinition.builder("logic.random_number", "Random Number", NodeCategory.LOGIC)
                .input("Min", PortKind.DATA, PortType.NUMBER)
                .input("Max", PortKind.DATA, PortType.NUMBER)
                .output("Value", PortKind.DATA, PortType.NUMBER)
                .build());
        }
        if (registry.find("logic.math").isEmpty()) {
            registry.register(NodeDefinition.builder("logic.math", "Math Operation", NodeCategory.LOGIC)
                .input("A", PortKind.DATA, PortType.NUMBER)
                .input("B", PortKind.DATA, PortType.NUMBER)
                .output("Result", PortKind.DATA, PortType.NUMBER)
                .build());
        }
        registry.register(NodeDefinition.builder("logic.text_join", "Text Join", NodeCategory.LOGIC)
            .input("A", PortKind.DATA, PortType.TEXT)
            .input("B", PortKind.DATA, PortType.TEXT)
            .output("Text", PortKind.DATA, PortType.TEXT)
            .build());
        registry.register(NodeDefinition.builder("logic.number_to_text", "Number To Text", NodeCategory.LOGIC)
            .input("Number", PortKind.DATA, PortType.NUMBER)
            .output("Text", PortKind.DATA, PortType.TEXT)
            .build());
        registry.register(NodeDefinition.builder("logic.set_variable", "Set Variable", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Name", PortKind.DATA, PortType.TEXT)
            .input("Value", PortKind.DATA, PortType.ANY)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.get_variable", "Get Variable", NodeCategory.LOGIC)
            .input("Name", PortKind.DATA, PortType.TEXT)
            .output("Value", PortKind.DATA, PortType.ANY)
            .build());
        registry.register(NodeDefinition.builder("logic.set_player_variable", "Set Player Variable", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Name", PortKind.DATA, PortType.TEXT)
            .input("Value", PortKind.DATA, PortType.ANY)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.get_player_variable", "Get Player Variable", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Name", PortKind.DATA, PortType.TEXT)
            .output("Value", PortKind.DATA, PortType.ANY)
            .build());
        registry.register(NodeDefinition.builder("logic.get_distance", "Get Distance", NodeCategory.LOGIC)
            .input("Location A", PortKind.DATA, PortType.LOCATION)
            .input("Location B", PortKind.DATA, PortType.LOCATION)
            .output("Distance", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("logic.player_has_item", "Has Item", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .output("Has", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.break", "Break Loop", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.continue", "Continue Loop", NodeCategory.LOGIC)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("logic.cooldown", "Cooldown Check", NodeCategory.LOGIC)
            .input("Key", PortKind.DATA, PortType.TEXT)
            .input("Time", PortKind.DATA, PortType.NUMBER)
            .output("Ready", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.color_text", "Color Text", NodeCategory.LOGIC)
            .input("Text", PortKind.DATA, PortType.TEXT)
            .output("Text", PortKind.DATA, PortType.TEXT)
            .build());
        registry.register(NodeDefinition.builder("logic.is_sneaking", "Is Sneaking", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.is_sprinting", "Is Sprinting", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.is_on_ground", "Is On Ground", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.is_in_water", "Is In Water", NodeCategory.LOGIC)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Result", PortKind.DATA, PortType.BOOLEAN)
            .build());
        registry.register(NodeDefinition.builder("logic.random_player", "Random Player", NodeCategory.LOGIC)
            .output("Player", PortKind.DATA, PortType.PLAYER)
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
        registry.register(NodeDefinition.builder("action.set_player_health", "Set Player Health", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Health", PortKind.DATA, PortType.NUMBER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.set_player_hunger", "Set Player Hunger", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Hunger", PortKind.DATA, PortType.NUMBER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.set_block", "Set Block", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Location", PortKind.DATA, PortType.LOCATION)
            .input("Material", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.break_block", "Break Block", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Location", PortKind.DATA, PortType.LOCATION)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.remove_item", "Remove Item", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.clear_inventory", "Clear Inventory", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.damage_player", "Damage Player", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Amount", PortKind.DATA, PortType.NUMBER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.heal_player", "Heal Player", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Amount", PortKind.DATA, PortType.NUMBER)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.send_title", "Send Title", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Title", PortKind.DATA, PortType.TEXT)
            .input("Subtitle", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.send_actionbar", "Send Action Bar", NodeCategory.ACTION)
            .input("In", PortKind.EXECUTION, PortType.FLOW)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .input("Text", PortKind.DATA, PortType.TEXT)
            .output("Then", PortKind.EXECUTION, PortType.FLOW)
            .build());
        registry.register(NodeDefinition.builder("action.set_item_name", "Set Item Name", NodeCategory.ACTION)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .input("Name", PortKind.DATA, PortType.TEXT)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("action.set_item_lore", "Set Item Lore", NodeCategory.ACTION)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .input("Lore", PortKind.DATA, PortType.TEXT)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
            .build());
        registry.register(NodeDefinition.builder("action.enchant_item", "Enchant Item", NodeCategory.ACTION)
            .input("Item", PortKind.DATA, PortType.ITEMSTACK)
            .input("Enchantment", PortKind.DATA, PortType.TEXT)
            .input("Level", PortKind.DATA, PortType.NUMBER)
            .output("Item", PortKind.DATA, PortType.ITEMSTACK)
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
        registry.register(NodeDefinition.builder("data.get_player_health", "Get Player Health", NodeCategory.DATA)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Health", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("data.get_player_hunger", "Get Player Hunger", NodeCategory.DATA)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Hunger", PortKind.DATA, PortType.NUMBER)
            .build());
        registry.register(NodeDefinition.builder("data.get_player_location", "Get Player Location", NodeCategory.DATA)
            .input("Player", PortKind.DATA, PortType.PLAYER)
            .output("Location", PortKind.DATA, PortType.LOCATION)
            .build());
        registry.register(NodeDefinition.builder("data.get_block_type", "Get Block Type", NodeCategory.DATA)
            .input("Location", PortKind.DATA, PortType.LOCATION)
            .output("Material", PortKind.DATA, PortType.TEXT)
            .build());
    }
}
