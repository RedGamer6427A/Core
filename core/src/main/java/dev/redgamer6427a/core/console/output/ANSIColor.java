package dev.redgamer6427a.core.console.output;

import lombok.Getter;

@Getter
public enum ANSIColor {
    BLACK("&black", 30),
    RED("&red", 31),
    GREEN("&green", 32),
    YELLOW("&yellow", 33),
    BLUE("&blue", 34),
    MAGENTA("&magenta", 35),
    CYAN("&cyan", 36),
    WHITE("&white", 37),
    BRIGHT_BLACK("&dark_gray", 90),
    BRIGHT_RED("&bright_red", 91),
    BRIGHT_GREEN("&bright_green", 92),
    BRIGHT_YELLOW("&bright_yellow", 93),
    BRIGHT_BLUE("&bright_blue", 94),
    BRIGHT_MAGENTA("&bright_magenta", 95),
    BRIGHT_CYAN("&bright_cyan", 96),
    BRIGHT_WHITE("&bright_white", 97);

    private final String name;
    private final int ansi;

    ANSIColor(String name, int ansi) {
        this.name = name;
        this.ansi = ansi;
    }
}