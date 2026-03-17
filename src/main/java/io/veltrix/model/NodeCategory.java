package io.veltrix.model;

public enum NodeCategory {
    EVENT("event", "#d64545"),
    LOGIC("logic", "#8b5cf6"),
    ACTION("action", "#3b82f6"),
    DATA("data", "#22c55e");

    private final String key;
    private final String colorHex;

    NodeCategory(String key, String colorHex) {
        this.key = key;
        this.colorHex = colorHex;
    }

    public String key() {
        return key;
    }

    public String colorHex() {
        return colorHex;
    }
}
