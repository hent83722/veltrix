package io.veltrix.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public final class ProjectStore {
    private static final String PROJECT_IDS = "project.ids";

    private final Path dataDir;
    private final Path projectsFile;
    private final Path projectDataDir;

    public ProjectStore() {
        this.dataDir = resolveDataDir();
        this.projectsFile = dataDir.resolve("projects.properties");
        this.projectDataDir = dataDir.resolve("projects");
    }

    public List<ProjectRecord> listProjects() {
        Properties props = load();
        List<ProjectRecord> projects = new ArrayList<>();
        for (String id : readIds(props)) {
            String name = props.getProperty(key(id, "name"), "").trim();
            String packageName = props.getProperty(key(id, "package"), "").trim();
            String projectPath = props.getProperty(key(id, "path"), "").trim();
            long updatedAt = parseLong(props.getProperty(key(id, "updatedAt")), 0L);
            if (!name.isEmpty() && !packageName.isEmpty()) {
                if (projectPath.isEmpty()) {
                    projectPath = defaultProjectsRoot().resolve(sanitizeDirectoryName(name)).toString();
                }
                projects.add(new ProjectRecord(id, name, packageName, projectPath, updatedAt));
            }
        }
        projects.sort(Comparator.comparingLong(ProjectRecord::updatedAt).reversed());
        return projects;
    }

    public ProjectRecord createProject(String name, String packageName, String projectPath) {
        String trimmedName = name == null ? "" : name.trim();
        String trimmedPackage = packageName == null ? "" : packageName.trim();
        String trimmedPath = projectPath == null ? "" : projectPath.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Project name is required.");
        }
        if (!isValidPackage(trimmedPackage)) {
            throw new IllegalArgumentException("Invalid package name.");
        }
        if (trimmedPath.isEmpty()) {
            throw new IllegalArgumentException("Project location is required.");
        }

        long now = System.currentTimeMillis();
        ProjectRecord record = new ProjectRecord(UUID.randomUUID().toString(), trimmedName, trimmedPackage, trimmedPath, now);

        Properties props = load();
        List<String> ids = readIds(props);
        ids.remove(record.id());
        ids.add(record.id());
        writeIds(props, ids);
        writeProject(props, record);
        save(props);

        return record;
    }

    public Path defaultProjectsRoot() {
        Path home = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win") || os.contains("mac")) {
            return home.resolve("Documents").resolve("Veltrix Projects");
        }

        String xdgDocumentsDir = System.getenv("XDG_DOCUMENTS_DIR");
        if (xdgDocumentsDir != null && !xdgDocumentsDir.isBlank()) {
            String expanded = xdgDocumentsDir.replace("$HOME", home.toString()).replace("~", home.toString());
            return Paths.get(expanded).resolve("Veltrix Projects");
        }
        return home.resolve("Documents").resolve("Veltrix Projects");
    }

    public void touch(ProjectRecord project) {
        upsertProjectMetadata(project);
    }

    public void deleteProject(String projectId) {
        Properties props = load();
        List<String> ids = readIds(props);
        if (!ids.remove(projectId)) {
            return;
        }

        props.remove(key(projectId, "name"));
        props.remove(key(projectId, "package"));
        props.remove(key(projectId, "path"));
        props.remove(key(projectId, "updatedAt"));
        writeIds(props, ids);
        save(props);

        Path projectFile = projectDataDir.resolve(projectId + ".properties");
        try {
            Files.deleteIfExists(projectFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to delete saved project data", ex);
        }
    }

    public ProjectRecord renameProject(String projectId, String newName) {
        String trimmedName = newName == null ? "" : newName.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Project name is required.");
        }

        Properties props = load();
        ProjectRecord existing = findById(props, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        ProjectRecord renamed = new ProjectRecord(
            existing.id(),
            trimmedName,
            existing.packageName(),
            existing.projectPath(),
            System.currentTimeMillis()
        );

        writeProject(props, renamed);
        save(props);
        updateSavedProjectHeader(projectId, trimmedName, existing.packageName(), existing.projectPath(), renamed.updatedAt());
        return renamed;
    }

    public ProjectRecord duplicateProject(ProjectRecord source) {
        String duplicateName = nextDuplicateName(source.name());
        String duplicatePath = nextDuplicatePath(source.projectPath(), duplicateName);
        long now = System.currentTimeMillis();
        ProjectRecord duplicate = new ProjectRecord(
            UUID.randomUUID().toString(),
            duplicateName,
            source.packageName(),
            duplicatePath,
            now
        );

        Properties props = load();
        List<String> ids = readIds(props);
        ids.remove(duplicate.id());
        ids.add(duplicate.id());
        writeIds(props, ids);
        writeProject(props, duplicate);
        save(props);

        Optional<ProjectData> sourceData = loadProjectData(source.id());
        if (sourceData.isPresent()) {
            ProjectData data = sourceData.get();
            saveProjectData(new ProjectData(
                duplicate.id(),
                duplicate.name(),
                duplicate.packageName(),
                duplicate.projectPath(),
                data.nodes(),
                data.connections(),
                data.groups(),
                now
            ));
        }

        return duplicate;
    }

    public void saveProjectData(ProjectData projectData) {
        String trimmedName = projectData.name() == null ? "" : projectData.name().trim();
        String trimmedPackage = projectData.packageName() == null ? "" : projectData.packageName().trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Project name is required.");
        }
        if (!isValidPackage(trimmedPackage)) {
            throw new IllegalArgumentException("Invalid package name.");
        }

        long updatedAt = projectData.updatedAt() > 0 ? projectData.updatedAt() : System.currentTimeMillis();
        String projectPath = findById(load(), projectData.projectId())
            .map(ProjectRecord::projectPath)
            .orElse(defaultProjectsRoot().resolve(sanitizeDirectoryName(trimmedName)).toString());
        ProjectRecord record = new ProjectRecord(projectData.projectId(), trimmedName, trimmedPackage, projectPath, updatedAt);
        upsertProjectMetadata(record);

        Properties props = new Properties();
        props.setProperty("project.name", trimmedName);
        props.setProperty("project.package", trimmedPackage);
        props.setProperty("project.path", projectPath);
        props.setProperty("project.updatedAt", Long.toString(updatedAt));

        props.setProperty("node.count", Integer.toString(projectData.nodes().size()));
        for (int i = 0; i < projectData.nodes().size(); i++) {
            ProjectData.NodeData node = projectData.nodes().get(i);
            String base = "node." + i + ".";
            props.setProperty(base + "key", node.key());
            props.setProperty(base + "type", node.type());
            props.setProperty(base + "x", Double.toString(node.x()));
            props.setProperty(base + "y", Double.toString(node.y()));
            props.setProperty(base + "collapsed", Boolean.toString(node.collapsed()));

            List<Map.Entry<String, String>> values = new ArrayList<>(node.values().entrySet());
            props.setProperty(base + "value.count", Integer.toString(values.size()));
            for (int j = 0; j < values.size(); j++) {
                Map.Entry<String, String> entry = values.get(j);
                String valueBase = base + "value." + j + ".";
                props.setProperty(valueBase + "key", entry.getKey());
                props.setProperty(valueBase + "data", entry.getValue() == null ? "" : entry.getValue());
            }
        }

        props.setProperty("connection.count", Integer.toString(projectData.connections().size()));
        for (int i = 0; i < projectData.connections().size(); i++) {
            ProjectData.ConnectionData connection = projectData.connections().get(i);
            String base = "connection." + i + ".";
            props.setProperty(base + "fromNode", connection.fromNodeKey());
            props.setProperty(base + "fromPort", connection.fromPortName());
            props.setProperty(base + "toNode", connection.toNodeKey());
            props.setProperty(base + "toPort", connection.toPortName());
        }

        props.setProperty("group.count", Integer.toString(projectData.groups().size()));
        for (int i = 0; i < projectData.groups().size(); i++) {
            ProjectData.GroupData group = projectData.groups().get(i);
            String base = "group." + i + ".";
            props.setProperty(base + "name", group.name());
            props.setProperty(base + "x", Double.toString(group.x()));
            props.setProperty(base + "y", Double.toString(group.y()));
            props.setProperty(base + "width", Double.toString(group.width()));
            props.setProperty(base + "height", Double.toString(group.height()));
            props.setProperty(base + "collapsed", Boolean.toString(group.collapsed()));
            props.setProperty(base + "node.count", Integer.toString(group.nodeKeys().size()));
            for (int j = 0; j < group.nodeKeys().size(); j++) {
                props.setProperty(base + "node." + j, group.nodeKeys().get(j));
            }
        }

        try {
            Files.createDirectories(projectDataDir);
            Path file = projectDataDir.resolve(projectData.projectId() + ".properties");
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Veltrix project data");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save project data", ex);
        }
    }

    public Optional<ProjectData> loadProjectData(String projectId) {
        Path file = projectDataDir.resolve(projectId + ".properties");
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read project data", ex);
        }

        String name = props.getProperty("project.name", "").trim();
        String projectPath = props.getProperty("project.path", "").trim();
        String packageName = props.getProperty("project.package", "").trim();
        long updatedAt = parseLong(props.getProperty("project.updatedAt"), 0L);
        if (name.isEmpty() || packageName.isEmpty()) {
            return Optional.empty();
        }

        int nodeCount = parseInt(props.getProperty("node.count"), 0);
        List<ProjectData.NodeData> nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            String base = "node." + i + ".";
            String key = props.getProperty(base + "key", "");
            String type = props.getProperty(base + "type", "");
            if (key.isBlank() || type.isBlank()) {
                continue;
            }
            double x = parseDouble(props.getProperty(base + "x"), 0.0);
            double y = parseDouble(props.getProperty(base + "y"), 0.0);
            boolean collapsed = Boolean.parseBoolean(props.getProperty(base + "collapsed", "false"));

            int valueCount = parseInt(props.getProperty(base + "value.count"), 0);
            java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
            for (int j = 0; j < valueCount; j++) {
                String valueBase = base + "value." + j + ".";
                String valueKey = props.getProperty(valueBase + "key", "");
                if (!valueKey.isBlank()) {
                    values.put(valueKey, props.getProperty(valueBase + "data", ""));
                }
            }
            nodes.add(new ProjectData.NodeData(key, type, x, y, collapsed, values));
        }

        int connectionCount = parseInt(props.getProperty("connection.count"), 0);
        List<ProjectData.ConnectionData> connections = new ArrayList<>();
        for (int i = 0; i < connectionCount; i++) {
            String base = "connection." + i + ".";
            String fromNode = props.getProperty(base + "fromNode", "");
            String fromPort = props.getProperty(base + "fromPort", "");
            String toNode = props.getProperty(base + "toNode", "");
            String toPort = props.getProperty(base + "toPort", "");
            if (fromNode.isBlank() || fromPort.isBlank() || toNode.isBlank() || toPort.isBlank()) {
                continue;
            }
            connections.add(new ProjectData.ConnectionData(fromNode, fromPort, toNode, toPort));
        }

        int groupCount = parseInt(props.getProperty("group.count"), 0);
        List<ProjectData.GroupData> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            String base = "group." + i + ".";
            String groupName = props.getProperty(base + "name", "");
            if (groupName.isBlank()) {
                continue;
            }
            double x = parseDouble(props.getProperty(base + "x"), 0.0);
            double y = parseDouble(props.getProperty(base + "y"), 0.0);
            double width = parseDouble(props.getProperty(base + "width"), 300.0);
            double height = parseDouble(props.getProperty(base + "height"), 200.0);
            boolean collapsed = Boolean.parseBoolean(props.getProperty(base + "collapsed", "false"));
            int groupNodeCount = parseInt(props.getProperty(base + "node.count"), 0);
            List<String> nodeKeys = new ArrayList<>();
            for (int j = 0; j < groupNodeCount; j++) {
                String nodeKey = props.getProperty(base + "node." + j, "");
                if (!nodeKey.isBlank()) {
                    nodeKeys.add(nodeKey);
                }
            }
            groups.add(new ProjectData.GroupData(groupName, x, y, width, height, collapsed, nodeKeys));
        }

        return Optional.of(new ProjectData(projectId, name, packageName, projectPath, nodes, connections, groups, updatedAt));
    }

    private void upsertProjectMetadata(ProjectRecord project) {
        Properties props = load();
        ProjectRecord updated = new ProjectRecord(
            project.id(),
            project.name(),
            project.packageName(),
            project.projectPath(),
            System.currentTimeMillis()
        );

        List<String> ids = readIds(props);
        ids.remove(updated.id());
        ids.add(updated.id());
        writeIds(props, ids);
        writeProject(props, updated);
        save(props);
    }

    private Optional<ProjectRecord> findById(Properties props, String id) {
        String name = props.getProperty(key(id, "name"));
        String packageName = props.getProperty(key(id, "package"));
        String projectPath = props.getProperty(key(id, "path"));
        if (name == null || packageName == null) {
            return Optional.empty();
        }
        if (projectPath == null || projectPath.isBlank()) {
            projectPath = defaultProjectsRoot().resolve(sanitizeDirectoryName(name)).toString();
        }
        long updatedAt = parseLong(props.getProperty(key(id, "updatedAt")), 0L);
        return Optional.of(new ProjectRecord(id, name, packageName, projectPath, updatedAt));
    }

    private List<String> readIds(Properties props) {
        String raw = props.getProperty(PROJECT_IDS, "").trim();
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> ids = new ArrayList<>();
        for (String id : raw.split(",")) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) {
                ids.add(trimmed);
            }
        }
        return ids;
    }

    private void writeIds(Properties props, List<String> ids) {
        props.setProperty(PROJECT_IDS, String.join(",", ids));
    }

    private void writeProject(Properties props, ProjectRecord project) {
        props.setProperty(key(project.id(), "name"), project.name());
        props.setProperty(key(project.id(), "package"), project.packageName());
        props.setProperty(key(project.id(), "path"), project.projectPath());
        props.setProperty(key(project.id(), "updatedAt"), Long.toString(project.updatedAt()));
    }

    private void updateSavedProjectHeader(String projectId, String name, String packageName, String projectPath, long updatedAt) {
        Path file = projectDataDir.resolve(projectId + ".properties");
        if (!Files.exists(file)) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to update project data", ex);
        }

        props.setProperty("project.name", name);
        props.setProperty("project.package", packageName);
        props.setProperty("project.path", projectPath);
        props.setProperty("project.updatedAt", Long.toString(updatedAt));

        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "Veltrix project data");
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to update project data", ex);
        }
    }

    private String nextDuplicateName(String baseName) {
        List<ProjectRecord> projects = listProjects();
        String candidate = baseName + " Copy";
        int index = 2;
        while (containsProjectName(projects, candidate)) {
            candidate = baseName + " Copy " + index;
            index++;
        }
        return candidate;
    }

    private String nextDuplicatePath(String sourcePath, String duplicateName) {
        if (sourcePath == null || sourcePath.isBlank()) {
            return defaultProjectsRoot().resolve(sanitizeDirectoryName(duplicateName)).toString();
        }

        Path source = Path.of(sourcePath);
        Path parent = source.getParent() == null ? defaultProjectsRoot() : source.getParent();
        String base = sanitizeDirectoryName(duplicateName);
        List<ProjectRecord> projects = listProjects();
        Path candidate = parent.resolve(base);
        int index = 2;
        while (Files.exists(candidate) || containsProjectPath(projects, candidate.toString())) {
            candidate = parent.resolve(base + "-" + index);
            index++;
        }
        return candidate.toString();
    }

    private boolean containsProjectName(List<ProjectRecord> projects, String name) {
        for (ProjectRecord project : projects) {
            if (project.name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsProjectPath(List<ProjectRecord> projects, String path) {
        for (ProjectRecord project : projects) {
            if (project.projectPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    private String key(String id, String field) {
        return "project." + id + "." + field;
    }

    private Properties load() {
        Properties props = new Properties();
        if (!Files.exists(projectsFile)) {
            return props;
        }
        try (InputStream in = Files.newInputStream(projectsFile)) {
            props.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read saved projects", ex);
        }
        return props;
    }

    private void save(Properties props) {
        try {
            Files.createDirectories(projectsFile.getParent());
            try (OutputStream out = Files.newOutputStream(projectsFile)) {
                props.store(out, "Veltrix projects");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save projects", ex);
        }
    }

    private Path resolveDataDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path home = Path.of(System.getProperty("user.home"));

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData).resolve("Veltrix");
            }
            return home.resolve("AppData").resolve("Roaming").resolve("Veltrix");
        }

        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Application Support").resolve("Veltrix");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Path.of(xdgDataHome).resolve("veltrix");
        }
        return home.resolve(".local").resolve("share").resolve("veltrix");
    }

    private long parseLong(String value, long fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean isValidPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return false;
        }
        return packageName.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }

    private String sanitizeDirectoryName(String rawName) {
        String sanitized = rawName == null ? "" : rawName.trim().replaceAll("[^a-zA-Z0-9._-]+", "-");
        sanitized = sanitized.replaceAll("-+", "-").replaceAll("^-|-$", "");
        return sanitized.isBlank() ? "untitled" : sanitized;
    }
}
