package com.uni.javacraft.player;

import com.uni.javacraft.world.World;

import java.io.Serializable;

public class Player implements Serializable {
    private int x;
    private int y;

    public int x() { return x; }
    public int y() { return y; }

    public void set(int x, int y) { this.x = x; this.y = y; }

    public void center(World w) {
        this.x = w.width() / 2;
        this.y = w.height() / 2;
    }

    public void move(World w, int dx, int dy) {
        int nx = x + dx, ny = y + dy;
        if (w.inBounds(nx, ny)) { x = nx; y = ny; }
    }

    public void clamp(World w) {
        x = Math.max(0, Math.min(x, w.width()-1));
        y = Math.max(0, Math.min(y, w.height()-1));
    }
}
