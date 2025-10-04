package com.uni.javacraft.render;

public final class AnsiPalette {
    private AnsiPalette() {}

    public static final String RESET  = "\u001B[0m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN   = "\u001B[36m";
    public static final String RED    = "\u001B[31m";
    public static final String PURPLE = "\u001B[35m";
    public static final String BLUE   = "\u001B[34m";
    public static final String GRAY   = "\u001B[37m";
    public static final String WHITE  = "\u001B[97m";

    // "Brown" — many terms map it to yellow; 256-color safe-ish:
    public static final String BROWN  = "\u001B[38;5;94m";
}
