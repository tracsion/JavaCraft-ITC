package com.uni.javacraft.world;

import java.io.Serializable;

public class World implements Serializable {
    private final int w, h;
    private final int[][] grid; // store ids

    public World(int w, int h) {
        this.w = w; this.h = h;
        this.grid = new int[w][h];
    }

    public int width() { return w; }
    public int height() { return h; }

    public boolean inBounds(int x, int y) { return x >= 0 && y >= 0 && x < w && y < h; }

    public BlockType get(int x, int y) { return BlockType.fromId(grid[x][y]); }
    public void set(int x, int y, BlockType t) { grid[x][y] = t.id; }

    public int[][] raw() { return grid; } // for serialization if needed
}
