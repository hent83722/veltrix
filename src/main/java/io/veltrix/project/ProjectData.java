package io.veltrix.project;

import java.util.List;
import java.util.Map;

public record ProjectData(
    String projectId,
    String name,
    String packageName,
    String projectPath,
    List<NodeData> nodes,
    List<ConnectionData> connections,
    List<GroupData> groups,
    long updatedAt
) {
    public record NodeData(
        String key,
        String type,
        double x,
        double y,
        boolean collapsed,
        Map<String, String> values
    ) {}

    public record ConnectionData(
        String fromNodeKey,
        String fromPortName,
        String toNodeKey,
        String toPortName
    ) {}

    public record GroupData(
        String name,
        double x,
        double y,
        double width,
        double height,
        boolean collapsed,
        List<String> nodeKeys
    ) {}
}
