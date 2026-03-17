package io.veltrix.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import io.veltrix.graph.GraphManager;
import io.veltrix.model.Connection;
import io.veltrix.model.Node;
import io.veltrix.model.Port;
import io.veltrix.model.PortDirection;
import io.veltrix.model.PortKind;

public final class PluginExporter {
    public record ExportResult(Path outputDir, List<String> generatedFiles) {}

    public ExportResult export(GraphManager graph, Path outputDir, String packageName, String pluginName) throws IOException {
        String safePluginName = sanitizePluginName(pluginName);
        Path base = outputDir.toAbsolutePath().normalize().resolve(safePluginName + "-plugin");
        Files.createDirectories(base);
        Path srcMainJava = base.resolve("src/main/java").resolve(packageName.replace('.', '/'));
        Path srcMainResources = base.resolve("src/main/resources");

        Files.createDirectories(srcMainJava);
        Files.createDirectories(srcMainResources);

        String mainClassName = "GeneratedPlugin";
        String listenerClassName = "GeneratedListener";

        Path pom = base.resolve("pom.xml");
        Path mainClassFile = srcMainJava.resolve(mainClassName + ".java");
        Path listenerClassFile = srcMainJava.resolve(listenerClassName + ".java");
        Path pluginYml = srcMainResources.resolve("plugin.yml");

        Files.writeString(pom, buildPom(packageName, safePluginName));
        Files.writeString(mainClassFile, buildMainClass(packageName, mainClassName, listenerClassName));
        Files.writeString(listenerClassFile, buildListenerClass(graph, packageName, listenerClassName));
        Files.writeString(pluginYml, buildPluginYml(packageName, mainClassName, safePluginName));

        return new ExportResult(base, List.of(
            base.relativize(pom).toString(),
            base.relativize(mainClassFile).toString(),
            base.relativize(listenerClassFile).toString(),
            base.relativize(pluginYml).toString()
        ));
    }

    private String buildPom(String packageName, String pluginName) {
        String artifact = pluginName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return """
            <project xmlns=\"http://maven.apache.org/POM/4.0.0\"
                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
                     xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
                <modelVersion>4.0.0</modelVersion>
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>1.0.0</version>
                <name>%s</name>

                <properties>
                    <maven.compiler.source>21</maven.compiler.source>
                    <maven.compiler.target>21</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                </properties>

                <repositories>
                    <repository>
                        <id>papermc</id>
                        <url>https://repo.papermc.io/repository/maven-public/</url>
                    </repository>
                </repositories>

                <dependencies>
                    <dependency>
                        <groupId>io.papermc.paper</groupId>
                        <artifactId>paper-api</artifactId>
                        <version>1.20.6-R0.1-SNAPSHOT</version>
                        <scope>provided</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-shade-plugin</artifactId>
                            <version>3.5.3</version>
                            <executions>
                                <execution>
                                    <phase>package</phase>
                                    <goals>
                                        <goal>shade</goal>
                                    </goals>
                                    <configuration>
                                        <createDependencyReducedPom>false</createDependencyReducedPom>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """.formatted(packageName, artifact, pluginName);
    }

    private String buildMainClass(String packageName, String mainClassName, String listenerClassName) {
        return """
            package %s;

            import org.bukkit.plugin.java.JavaPlugin;

            public final class %s extends JavaPlugin {
                @Override
                public void onEnable() {
                    %s listener = new %s(this);
                    getServer().getPluginManager().registerEvents(listener, this);
                    listener.bootstrap();
                    listener.generatedOnEnable();
                    getLogger().info("Generated plugin enabled.");
                }

                @Override
                public void onDisable() {
                    %s listener = new %s(this);
                    listener.generatedOnDisable();
                    getLogger().info("Generated plugin disabled.");
                }
            }
            """.formatted(packageName, mainClassName, listenerClassName, listenerClassName, listenerClassName, listenerClassName);
    }

    private String buildPluginYml(String packageName, String mainClassName, String pluginName) {
        return """
            name: %s
            version: 1.0.0
            main: %s.%s
            api-version: '1.20'
            """.formatted(pluginName, packageName, mainClassName);
    }

    private String sanitizePluginName(String pluginName) {
        String value = pluginName == null ? "" : pluginName.trim();
        value = value.replaceAll("[^a-zA-Z0-9_-]+", "-");
        value = value.replaceAll("-+", "-");
        value = value.replaceAll("^-|-$", "");
        if (value.isBlank()) {
            return "VeltrixGenerated";
        }
        return value;
    }

    private String buildListenerClass(GraphManager graph, String packageName, String listenerClassName) {
        StringBuilder body = new StringBuilder();
        body.append("package ").append(packageName).append(";\n\n")
            .append("import org.bukkit.Bukkit;\n")
            .append("import org.bukkit.Location;\n")
            .append("import org.bukkit.Sound;\n")
            .append("import org.bukkit.entity.Entity;\n")
            .append("import org.bukkit.entity.EntityType;\n")
            .append("import org.bukkit.entity.Player;\n")
            .append("import org.bukkit.event.EventHandler;\n")
            .append("import org.bukkit.event.Listener;\n")
            .append("import org.bukkit.event.block.BlockBreakEvent;\n")
            .append("import org.bukkit.event.block.BlockExplodeEvent;\n")
            .append("import org.bukkit.event.block.BlockPlaceEvent;\n")
            .append("import org.bukkit.event.block.BlockRedstoneEvent;\n")
            .append("import org.bukkit.event.entity.EntityDamageEvent;\n")
            .append("import org.bukkit.event.entity.EntityDeathEvent;\n")
            .append("import org.bukkit.event.entity.EntityPickupItemEvent;\n")
            .append("import org.bukkit.event.player.AsyncPlayerChatEvent;\n")
            .append("import org.bukkit.event.player.PlayerCommandPreprocessEvent;\n")
            .append("import org.bukkit.event.entity.PlayerDeathEvent;\n")
            .append("import org.bukkit.event.player.PlayerDropItemEvent;\n")
            .append("import org.bukkit.event.player.PlayerInteractEvent;\n")
            .append("import org.bukkit.event.player.PlayerJoinEvent;\n")
            .append("import org.bukkit.event.player.PlayerMoveEvent;\n")
            .append("import org.bukkit.event.player.PlayerQuitEvent;\n")
            .append("import org.bukkit.inventory.ItemStack;\n")
            .append("import org.bukkit.plugin.java.JavaPlugin;\n")
            .append("\n")
            .append("public final class ").append(listenerClassName).append(" implements Listener {\n")
            .append("    private final JavaPlugin plugin;\n")
            .append("\n")
            .append("    public ").append(listenerClassName).append("(JavaPlugin plugin) {\n")
            .append("        this.plugin = plugin;\n")
            .append("    }\n\n")
            .append("    private double toNumber(Object value) {\n")
            .append("        if (value instanceof Number n) {\n")
            .append("            return n.doubleValue();\n")
            .append("        }\n")
            .append("        try {\n")
            .append("            return Double.parseDouble(String.valueOf(value));\n")
            .append("        } catch (Exception ex) {\n")
            .append("            return 0.0;\n")
            .append("        }\n")
            .append("    }\n\n")
            .append("    private boolean compareValues(Object a, Object b, String operator) {\n")
            .append("        return switch (operator) {\n")
            .append("            case \"!=\" -> !java.util.Objects.equals(a, b);\n")
            .append("            case \">\" -> toNumber(a) > toNumber(b);\n")
            .append("            case \">=\" -> toNumber(a) >= toNumber(b);\n")
            .append("            case \"<\" -> toNumber(a) < toNumber(b);\n")
            .append("            case \"<=\" -> toNumber(a) <= toNumber(b);\n")
            .append("            default -> java.util.Objects.equals(a, b);\n")
            .append("        };\n")
            .append("    }\n\n")
            .append("    private String extractCommandName(String raw) {\n")
            .append("        if (raw == null) {\n")
            .append("            return \"\";\n")
            .append("        }\n")
            .append("        String trimmed = raw.trim();\n")
            .append("        if (trimmed.startsWith(\"/\")) {\n")
            .append("            trimmed = trimmed.substring(1);\n")
            .append("        }\n")
            .append("        int split = trimmed.indexOf(' ');\n")
            .append("        return split >= 0 ? trimmed.substring(0, split) : trimmed;\n")
            .append("    }\n\n")
            .append("    private String extractCommandArgs(String raw) {\n")
            .append("        if (raw == null) {\n")
            .append("            return \"\";\n")
            .append("        }\n")
            .append("        String trimmed = raw.trim();\n")
            .append("        if (trimmed.startsWith(\"/\")) {\n")
            .append("            trimmed = trimmed.substring(1);\n")
            .append("        }\n")
            .append("        int split = trimmed.indexOf(' ');\n")
            .append("        return split >= 0 ? trimmed.substring(split + 1) : \"\";\n")
            .append("    }\n\n")
            .append("    public void bootstrap() {\n");

        List<Node> eventNodes = graph.nodes().stream()
            .filter(n -> n.type().startsWith("event."))
            .sorted(Comparator.comparing(Node::displayName))
            .toList();

        int idx = 1;
        for (Node eventNode : eventNodes) {
            if ("event.server_tick".equals(eventNode.type())) {
                body.append("        handleEvent").append(idx).append("();\n");
            }
            idx++;
        }
        body.append("    }\n\n")
            .append("    public void generatedOnEnable() {\n");

        for (Node eventNode : eventNodes) {
            if ("event.plugin_enable".equals(eventNode.type())) {
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("null", null, "null"), body, new HashSet<>());
            }
        }
        body.append("    }\n\n")
            .append("    public void generatedOnDisable() {\n");

        for (Node eventNode : eventNodes) {
            if ("event.plugin_disable".equals(eventNode.type())) {
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("null", null, "null"), body, new HashSet<>());
            }
        }
        body.append("    }\n\n");

        if (eventNodes.isEmpty()) {
            body.append("    @EventHandler\n")
                .append("    public void onJoin(PlayerJoinEvent event) {\n")
                .append("        event.getPlayer().sendMessage(\"No graph events found.\");\n")
                .append("    }\n");
        } else {
            idx = 1;
            for (Node eventNode : eventNodes) {
                appendEventHandler(graph, eventNode, idx++, body);
            }
        }

        body.append("}\n");
        return body.toString();
    }

    private void appendEventHandler(GraphManager graph, Node eventNode, int idx, StringBuilder body) {
        String methodName = "handleEvent" + idx;
        switch (eventNode.type()) {
            case "event.player_join" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerJoinEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getPlayer().getLocation()"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_leave" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerQuitEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getPlayer().getLocation()"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_break_block" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(BlockBreakEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getBlock().getLocation()"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_place_block" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(BlockPlaceEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getBlockPlaced().getLocation()"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_chat" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(AsyncPlayerChatEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", "event.getMessage()", "event.getPlayer().getLocation()"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_move" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerMoveEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getTo()", "event.getFrom()", "event.getTo()",
                        null, null, null, null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_death" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerDeathEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getEntity()", null, "event.getEntity().getLocation()", null, null,
                        "event.getEntity().getKiller()", null, null, null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_damage" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(EntityDamageEvent event) {\n")
                    .append("        if (!(event.getEntity() instanceof Player player)) {\n")
                    .append("            return;\n")
                    .append("        }\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("player", null, "player.getLocation()", null, null,
                        null, "event.getFinalDamage()", null, null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_interact" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerInteractEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null,
                        "event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : event.getPlayer().getLocation()",
                        null, null, null, null, "event.getItem()", null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_drop_item" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerDropItemEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getPlayer().getLocation()", null, null,
                        null, null, "event.getItemDrop().getItemStack()", null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.player_pickup_item" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(EntityPickupItemEvent event) {\n")
                    .append("        if (!(event.getEntity() instanceof Player player)) {\n")
                    .append("            return;\n")
                    .append("        }\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("player", null, "player.getLocation()", null, null,
                        null, null, "event.getItem().getItemStack()", null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.entity_death" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(EntityDeathEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getEntity().getKiller()", null, "event.getEntity().getLocation()", null, null,
                        "event.getEntity().getKiller()", null, null, "event.getEntity()", null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.block_explode" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(BlockExplodeEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext(null, null, "event.getBlock().getLocation()", null, null,
                        null, null, null, null, null, null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.redstone_change" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(BlockRedstoneEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext(null, null, "event.getBlock().getLocation()", null, null,
                        null, null, null, null, "event.getNewCurrent()", null, null), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.command_run" -> {
                body.append("    @EventHandler\n")
                    .append("    public void ").append(methodName).append("(PlayerCommandPreprocessEvent event) {\n");
                appendFlowFromPort(graph, eventNode, "Then", "        ",
                    new EventContext("event.getPlayer()", null, "event.getPlayer().getLocation()", null, null,
                        null, null, null, null, null,
                        "extractCommandName(event.getMessage())", "extractCommandArgs(event.getMessage())"), body, new HashSet<>());
                body.append("    }\n\n");
            }
            case "event.server_tick" -> {
                body.append("    public void ").append(methodName).append("() {\n")
                    .append("        Bukkit.getScheduler().runTaskTimer(plugin, () -> {\n");
                appendFlowFromPort(graph, eventNode, "Then", "            ",
                    new EventContext("null", null, "null"), body, new HashSet<>());
                body.append("        }, 1L, 1L);\n")
                    .append("    }\n\n");
            }
            case "event.plugin_enable", "event.plugin_disable" -> {
            }
            default -> {
            }
        }
    }

    private void appendFlowFromPort(GraphManager graph, Node node, String outputPortName, String indent,
                                    EventContext context, StringBuilder body, HashSet<String> callStack) {
        List<Node> nextNodes = nextExecutionNodes(graph, node, outputPortName);
        for (Node next : nextNodes) {
            appendNodeExecution(graph, next, indent, context, body, callStack);
        }
    }

    private void appendNodeExecution(GraphManager graph, Node node, String indent,
                                     EventContext context, StringBuilder body, HashSet<String> callStack) {
        if (!callStack.add(node.id())) {
            return;
        }

        switch (node.type()) {
            case "logic.if_condition" -> {
                String conditionExpr = resolveInputExpression(graph, node, "Condition", context, "true");
                body.append(indent).append("if (").append(conditionExpr).append(") {\n");
                appendFlowFromPort(graph, node, "True", indent + "    ", context, body, new HashSet<>(callStack));
                body.append(indent).append("} else {\n");
                appendFlowFromPort(graph, node, "False", indent + "    ", context, body, new HashSet<>(callStack));
                body.append(indent).append("}\n");
            }
            case "logic.loop" -> {
                String countExpr = resolveInputExpression(graph, node, "Count", context,
                    normalizeNumber(node.valueOrDefault("count", "1")));
                String loopVar = "loopCount_" + variableSuffix(node.id());
                String idxVar = "i_" + variableSuffix(node.id());
                body.append(indent).append("int ").append(loopVar)
                    .append(" = (int) Math.max(0, Math.floor(toNumber(").append(countExpr).append(")));\n");
                body.append(indent).append("for (int ").append(idxVar).append(" = 0; ").append(idxVar)
                    .append(" < ").append(loopVar).append("; ").append(idxVar).append("++) {\n");
                appendFlowFromPort(graph, node, "Loop", indent + "    ", context, body, new HashSet<>(callStack));
                body.append(indent).append("}\n");
                appendFlowFromPort(graph, node, "Done", indent, context, body, new HashSet<>(callStack));
            }
            case "logic.foreach_player" -> {
                String playerVar = "loopPlayer_" + variableSuffix(node.id());
                body.append(indent).append("for (Player ").append(playerVar).append(" : Bukkit.getOnlinePlayers()) {\n");
                EventContext loopContext = withPlayerContext(context, playerVar);
                appendFlowFromPort(graph, node, "Loop", indent + "    ", loopContext, body, new HashSet<>(callStack));
                body.append(indent).append("}\n");
                appendFlowFromPort(graph, node, "Done", indent, context, body, new HashSet<>(callStack));
            }
            case "logic.while" -> {
                String conditionExpr = resolveInputExpression(graph, node, "Condition", context, "false");
                String guardVar = "whileGuard_" + variableSuffix(node.id());
                body.append(indent).append("int ").append(guardVar).append(" = 0;\n");
                body.append(indent).append("while (").append(conditionExpr).append(" && ").append(guardVar).append("++ < 10000) {\n");
                appendFlowFromPort(graph, node, "Loop", indent + "    ", context, body, new HashSet<>(callStack));
                body.append(indent).append("}\n");
                appendFlowFromPort(graph, node, "Done", indent, context, body, new HashSet<>(callStack));
            }
            case "logic.delay_timer" -> {
                String ticksExpr = resolveInputExpression(graph, node, "Ticks", context,
                    normalizeNumber(node.valueOrDefault("ticks", "20")));
                body.append(indent).append("Bukkit.getScheduler().runTaskLater(plugin, () -> {\n");
                appendFlowFromPort(graph, node, "Then", indent + "    ", context, body, new HashSet<>(callStack));
                body.append(indent).append("}, ").append(ticksExpr).append("L);\n");
            }
            default -> {
                if (node.type().startsWith("action.")) {
                    body.append(indent).append(actionCode(graph, node, context)).append("\n");
                }
                appendFlowFromPort(graph, node, "Then", indent, context, body, callStack);
            }
        }
    }

    private String actionCode(GraphManager graph, Node node, EventContext context) {
        String playerExpr = resolveInputExpression(graph, node, "Player", context, context.playerExpr());
        String textExpr = resolveInputExpression(graph, node, "Text", context,
            quoteJava(node.valueOrDefault("text", "Hello from Veltrix")));
        String locationExpr = resolveInputExpression(graph, node, "Location", context,
            context.locationExpr() != null ? context.locationExpr() : "new Location(Bukkit.getWorlds().get(0), 0, 64, 0)");
        String itemExpr = resolveInputExpression(graph, node, "Item", context,
            itemStackExpression(node.valueOrDefault("material", "DIAMOND"), node.valueOrDefault("amount", "1")));
        String commandExpr = resolveInputExpression(graph, node, "Command", context,
            quoteJava(node.valueOrDefault("command", "say Executed command from graph")));
        String soundExpr = resolveInputExpression(graph, node, "Sound", context,
            quoteJava(node.valueOrDefault("sound", "ENTITY_EXPERIENCE_ORB_PICKUP")));
        String entityTypeExpr = resolveInputExpression(graph, node, "Entity Type", context,
            quoteJava(node.valueOrDefault("entityType", "ZOMBIE")));

        return switch (node.type()) {
            case "action.send_message" -> "if (" + playerExpr + " instanceof Player p) { p.sendMessage(" + textExpr + "); }";
            case "action.spawn_entity" -> "if (" + locationExpr + " != null && (" + locationExpr + ").getWorld() != null) { (" + locationExpr + ").getWorld().spawnEntity(" + locationExpr + ", EntityType.valueOf(" + entityTypeExpr + ")); }";
            case "action.give_item" -> "if (" + playerExpr + " instanceof Player p) { p.getInventory().addItem(" + itemExpr + "); }";
            case "action.teleport_player" -> "if (" + playerExpr + " instanceof Player p) { p.teleport(" + locationExpr + "); }";
            case "action.play_sound" -> "if (" + playerExpr + " instanceof Player p) { p.playSound(p.getLocation(), Sound.valueOf(" + soundExpr + "), 1.0f, 1.0f); }";
            case "action.run_command" -> "Bukkit.dispatchCommand(Bukkit.getConsoleSender(), " + commandExpr + ");";
            default -> "";
        };
    }

    private String resolveInputExpression(GraphManager graph, Node node, String inputName,
                                          EventContext context, String fallback) {
        Optional<Port> targetInput = node.inputPorts().stream()
            .filter(p -> p.kind() == PortKind.DATA && p.direction() == PortDirection.INPUT)
            .filter(p -> p.name().equalsIgnoreCase(inputName))
            .findFirst();
        if (targetInput.isEmpty()) {
            return fallback;
        }

        Optional<Connection> incoming = graph.connections().stream()
            .filter(c -> c.toPortId().equals(targetInput.get().id()))
            .findFirst();
        if (incoming.isEmpty()) {
            return fallback;
        }

        Optional<Port> sourcePort = graph.findPort(incoming.get().fromPortId());
        if (sourcePort.isEmpty()) {
            return fallback;
        }
        Optional<Node> sourceNode = graph.findNode(sourcePort.get().nodeId());
        if (sourceNode.isEmpty()) {
            return fallback;
        }
        return outputExpression(graph, sourceNode.get(), sourcePort.get().name(), context, fallback);
    }

    private String outputExpression(GraphManager graph, Node sourceNode, String sourceOutputName,
                                    EventContext context, String fallback) {
        return switch (sourceNode.type()) {
            case "data.number" -> normalizeNumber(sourceNode.valueOrDefault("value", "1"));
            case "data.text" -> quoteJava(sourceNode.valueOrDefault("value", "Veltrix"));
            case "data.player" -> context.playerExpr() != null ? context.playerExpr() : "null";
            case "data.location" -> {
                String world = sourceNode.valueOrDefault("world", "world");
                String x = normalizeNumber(sourceNode.valueOrDefault("x", "0"));
                String y = normalizeNumber(sourceNode.valueOrDefault("y", "64"));
                String z = normalizeNumber(sourceNode.valueOrDefault("z", "0"));
                String configured = "(Bukkit.getWorld(" + quoteJava(world) + ") != null ? new Location(Bukkit.getWorld(" + quoteJava(world) + "), " + x + ", " + y + ", " + z + ") : new Location(Bukkit.getWorlds().get(0), " + x + ", " + y + ", " + z + "))";
                yield configured;
            }
            case "data.itemstack" -> itemStackExpression(
                sourceNode.valueOrDefault("material", "DIAMOND"),
                sourceNode.valueOrDefault("amount", "1")
            );
            case "event.player_chat" -> sourceOutputName.equalsIgnoreCase("Message") && context.messageExpr() != null
                ? context.messageExpr()
                : (sourceOutputName.equalsIgnoreCase("Player") ? context.playerExpr() : fallback);
            case "event.player_break_block", "event.player_place_block" -> sourceOutputName.equalsIgnoreCase("Location")
                ? context.locationExpr()
                : context.playerExpr();
            case "event.player_move" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("From") && context.fromExpr() != null) {
                    yield context.fromExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("To") && context.toExpr() != null) {
                    yield context.toExpr();
                }
                yield fallback;
            }
            case "event.player_death" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Killer") && context.killerExpr() != null) {
                    yield context.killerExpr();
                }
                yield fallback;
            }
            case "event.player_damage" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Damage") && context.damageExpr() != null) {
                    yield context.damageExpr();
                }
                yield fallback;
            }
            case "event.player_interact" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Location") && context.locationExpr() != null) {
                    yield context.locationExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Item") && context.itemExpr() != null) {
                    yield context.itemExpr();
                }
                yield fallback;
            }
            case "event.player_drop_item", "event.player_pickup_item" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Item") && context.itemExpr() != null) {
                    yield context.itemExpr();
                }
                yield fallback;
            }
            case "event.entity_death" -> {
                if (sourceOutputName.equalsIgnoreCase("Entity") && context.entityExpr() != null) {
                    yield context.entityExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Killer") && context.killerExpr() != null) {
                    yield context.killerExpr();
                }
                yield fallback;
            }
            case "event.block_explode" -> sourceOutputName.equalsIgnoreCase("Location") && context.locationExpr() != null
                ? context.locationExpr()
                : fallback;
            case "event.redstone_change" -> {
                if (sourceOutputName.equalsIgnoreCase("Location") && context.locationExpr() != null) {
                    yield context.locationExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Power") && context.powerExpr() != null) {
                    yield context.powerExpr();
                }
                yield fallback;
            }
            case "event.command_run" -> {
                if (sourceOutputName.equalsIgnoreCase("Player")) {
                    yield context.playerExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Command") && context.commandExpr() != null) {
                    yield context.commandExpr();
                }
                if (sourceOutputName.equalsIgnoreCase("Arguments") && context.argumentsExpr() != null) {
                    yield context.argumentsExpr();
                }
                yield fallback;
            }
            case "event.player_join", "event.player_leave" -> context.playerExpr();
            case "logic.compare_values" -> {
                String a = resolveInputExpression(graph, sourceNode, "A", context, "null");
                String b = resolveInputExpression(graph, sourceNode, "B", context, "null");
                String operator = normalizeOperator(sourceNode.valueOrDefault("operator", "=="));
                yield "compareValues(" + a + ", " + b + ", " + quoteJava(operator) + ")";
            }
            case "logic.boolean_and" -> {
                String a = resolveInputExpression(graph, sourceNode, "A", context, "false");
                String b = resolveInputExpression(graph, sourceNode, "B", context, "false");
                yield "(" + a + " && " + b + ")";
            }
            case "logic.boolean_or" -> {
                String a = resolveInputExpression(graph, sourceNode, "A", context, "false");
                String b = resolveInputExpression(graph, sourceNode, "B", context, "false");
                yield "(" + a + " || " + b + ")";
            }
            case "logic.boolean_not" -> {
                String value = resolveInputExpression(graph, sourceNode, "Value", context, "false");
                yield "(!" + value + ")";
            }
            case "logic.random_chance" -> {
                String chance = resolveInputExpression(graph, sourceNode, "Chance", context,
                    normalizeNumber(sourceNode.valueOrDefault("chance", "0.5")));
                yield "(Math.random() < Math.max(0.0, Math.min(1.0, " + chance + ")))";
            }
            case "logic.random_number" -> {
                String min = resolveInputExpression(graph, sourceNode, "Min", context,
                    normalizeNumber(sourceNode.valueOrDefault("min", "0")));
                String max = resolveInputExpression(graph, sourceNode, "Max", context,
                    normalizeNumber(sourceNode.valueOrDefault("max", "1")));
                yield "(Math.min(toNumber(" + min + "), toNumber(" + max + ")) + (Math.random() * "
                    + "(Math.max(toNumber(" + min + "), toNumber(" + max + ")) - Math.min(toNumber(" + min + "), toNumber(" + max + ")))))";
            }
            case "logic.math" -> {
                String a = resolveInputExpression(graph, sourceNode, "A", context, "0");
                String b = resolveInputExpression(graph, sourceNode, "B", context, "0");
                String op = normalizeMathOperator(sourceNode.valueOrDefault("operator", "+"));
                yield switch (op) {
                    case "/" -> "(toNumber(" + b + ") == 0.0 ? 0.0 : (toNumber(" + a + ") / toNumber(" + b + ")))";
                    case "%" -> "(toNumber(" + b + ") == 0.0 ? 0.0 : (toNumber(" + a + ") % toNumber(" + b + ")))";
                    default -> "(toNumber(" + a + ") " + op + " toNumber(" + b + "))";
                };
            }
            case "logic.foreach_player" -> sourceOutputName.equalsIgnoreCase("Player")
                ? (context.playerExpr() != null ? context.playerExpr() : "null")
                : fallback;
            case "logic.text_join" -> {
                String a = resolveInputExpression(graph, sourceNode, "A", context, "\"\"");
                String b = resolveInputExpression(graph, sourceNode, "B", context, "\"\"");
                yield "(" + a + " + " + b + ")";
            }
            case "logic.number_to_text" -> {
                String value = resolveInputExpression(graph, sourceNode, "Number", context, "0");
                yield "String.valueOf(" + value + ")";
            }
            default -> fallback;
        };
    }

    private String itemStackExpression(String material, String amount) {
        return "new ItemStack(org.bukkit.Material.valueOf(" + quoteJava(normalizeEnumName(material, "DIAMOND")) + "), "
            + normalizeInteger(amount, "1") + ")";
    }

    private String quoteJava(String value) {
        String escaped = value == null ? "" : value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }

    private String normalizeNumber(String value) {
        if (value == null || value.isBlank()) {
            return "0";
        }
        try {
            Double.parseDouble(value.trim());
            return value.trim();
        } catch (NumberFormatException ex) {
            return "0";
        }
    }

    private String normalizeInteger(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return String.valueOf(Math.max(1, parsed));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String normalizeEnumName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        normalized = normalized.replaceAll("[^A-Z0-9_]", "");
        if (normalized.isBlank()) {
            return fallback;
        }
        return normalized;
    }

    private String normalizeOperator(String value) {
        if (value == null || value.isBlank()) {
            return "==";
        }
        return switch (value.trim()) {
            case "==", "!=", ">", ">=", "<", "<=" -> value.trim();
            default -> "==";
        };
    }

    private String normalizeMathOperator(String value) {
        if (value == null || value.isBlank()) {
            return "+";
        }
        return switch (value.trim()) {
            case "+", "-", "*", "/", "%" -> value.trim();
            default -> "+";
        };
    }

    private String variableSuffix(String nodeId) {
        String sanitized = (nodeId == null ? "node" : nodeId).replaceAll("[^a-zA-Z0-9]", "");
        return sanitized.isBlank() ? "node" : sanitized;
    }

    private EventContext withPlayerContext(EventContext base, String playerExpr) {
        String locationExpr = playerExpr + " != null ? " + playerExpr + ".getLocation() : null";
        return new EventContext(
            playerExpr,
            base.messageExpr(),
            locationExpr,
            base.fromExpr(),
            base.toExpr(),
            base.killerExpr(),
            base.damageExpr(),
            base.itemExpr(),
            base.entityExpr(),
            base.powerExpr(),
            base.commandExpr(),
            base.argumentsExpr()
        );
    }

    private List<Node> nextExecutionNodes(GraphManager graph, Node sourceNode, String outputPortName) {
        Optional<Port> outputPort = sourceNode.outputPorts().stream()
            .filter(p -> p.kind() == PortKind.EXECUTION && p.name().equalsIgnoreCase(outputPortName))
            .findFirst();
        if (outputPort.isEmpty()) {
            return List.of();
        }

        return graph.connections().stream()
            .filter(c -> c.fromPortId().equals(outputPort.get().id()))
            .map(c -> graph.findPort(c.toPortId()))
            .flatMap(Optional::stream)
            .filter(p -> p.kind() == PortKind.EXECUTION && p.direction() == PortDirection.INPUT)
            .map(p -> graph.findNode(p.nodeId()))
            .flatMap(Optional::stream)
            .toList();
    }

    private record EventContext(
        String playerExpr,
        String messageExpr,
        String locationExpr,
        String fromExpr,
        String toExpr,
        String killerExpr,
        String damageExpr,
        String itemExpr,
        String entityExpr,
        String powerExpr,
        String commandExpr,
        String argumentsExpr
    ) {
        private EventContext(String playerExpr, String messageExpr, String locationExpr) {
            this(playerExpr, messageExpr, locationExpr, null, null, null, null, null, null, null, null, null);
        }
    }
}
