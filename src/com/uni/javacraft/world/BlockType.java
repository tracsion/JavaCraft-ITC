package com.uni.javacraft.world;

// premium minecraft copy ngl lawsuit incoming

public enum BlockType {
    AIR(0, "Empty Block"),
    WOOD(1, "Wood"),
    LEAVES(2, "Leaves"),
    STONE(3, "Stone"),
    IRON_ORE(4, "Iron Ore"),
    GOLD(5, "Gold"),
    DIAMOND(6, "Diamond"),
    PLANKS(7, "Wooden Planks"),
    STICK(8, "Stick"),
    IRON_INGOT(9, "Iron Ingot"),
    GOLDCHAIN(10, "Gold chain");

    public final int id;
    private final String display;

    BlockType(int id, String display) { this.id = id; this.display = display; }

    public static BlockType fromId(int id) {
        for (var b : values()) if (b.id == id) return b;
        return AIR;
    }

    public String displayName() { return display; }
}