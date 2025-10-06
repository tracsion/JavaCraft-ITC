package com.uni.javacraft.world;

import java.util.Random;

public final class WorldGenerator {
    private WorldGenerator() {}

    public static World random(int w, int h) {
        World world = new World(w, h);
        Random rand = new Random();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = rand.nextInt(100);
                BlockType t = (r < 20) ? BlockType.WOOD
                        : (r < 35) ? BlockType.LEAVES
                        : (r < 50) ? BlockType.STONE
                        : (r < 70) ? BlockType.IRON_ORE
                        : BlockType.AIR;
                world.set(x, y, t);
            }
        }
        return world;
    }

    public static World tricolorSecret(int w, int h) {
        World world = new World(w, h);
        int stripe = h / 3;
        for (int y = 0; y < stripe; y++)
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.WOOD);      // redish
        for (int y = stripe; y < 2*stripe; y++)
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.IRON_ORE);  // whiteish
        for (int y = 2*stripe; y < h; y++)
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.STONE);     // blueish
        return world;
    }
}
