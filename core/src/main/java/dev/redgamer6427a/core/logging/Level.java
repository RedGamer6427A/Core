package dev.redgamer6427a.core.logging;

import lombok.Getter;

@Getter
public enum Level {

    FINEST("dark_blue", -2),
    FINE("light_blue", -1),
    INFO("green", 0),
    WARNING("yellow", 1),
    ERROR("red", 2),
    CRITICAL("dark_red", 3)
    ;
    private final String color;
    private final int severity;

    Level(String color, int severity) {
        this.color = color;
        this.severity = severity;
    }
}
