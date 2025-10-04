package com.uni.javacraft.core;

import com.uni.javacraft.inventory.CraftedInventory;
import com.uni.javacraft.inventory.Inventory;
import com.uni.javacraft.player.Player;
import com.uni.javacraft.world.BlockType;
import com.uni.javacraft.world.World;
import com.uni.javacraft.world.WorldGenerator;

import java.io.Serializable;

public class GameState implements Serializable {
    private World world;
    private final Player player = new Player();
    private final Inventory inv = new Inventory();
    private final CraftedInventory crafted = new CraftedInventory();

    private boolean unlockArmed = false;
    private boolean secretDoorUnlocked = false;
    private boolean inSecret = false;
    private boolean exit = false;

    public static GameState createDefault() {
        GameState s = new GameState();
        s.world = WorldGenerator.random(Config.DEFAULT_W, Config.DEFAULT_H);
        s.player.center(s.world);
        return s;
    }

    // getters
    public World world() { return world; }
    public Player player() { return player; }
    public Inventory inv() { return inv; }
    public CraftedInventory crafted() { return crafted; }
    public boolean inSecretArea() { return inSecret; }
    public boolean isExitRequested() { return exit; }

    // secret area or loading (gucci flip flops)
    public void setWorld(World w) {
        this.world = w;
        player.clamp(world);
    }

    // actions
    public void move(int dx, int dy) { player.move(world, dx, dy); }

    public void mine() {
        BlockType t = world.get(player.x(), player.y());
        if (t != BlockType.AIR) {
            inv.add(t.id);
            world.set(player.x(), player.y(), BlockType.AIR);
            System.out.println("Mined " + t.displayName() + ".");
        } else {
            System.out.println("No block to mine here.");
        }
    }

    public void place(int id) {
        if (id < 0 || id > 7) {
            System.out.println("Invalid block number (0–7).");
            return;
        }
        BlockType t = BlockType.fromId(id);
        if (id <= 4) {
            if (inv.contains(id)) {
                inv.remove(id, 1);
                world.set(player.x(), player.y(), t);
                System.out.println("Placed " + t.displayName() + ".");
            } else {
                System.out.println("You don't have " + t.displayName() + ".");
            }
        } else {
            // crafted items
            if (crafted.removeOne(t)) {
                world.set(player.x(), player.y(), t);
                System.out.println("Placed " + t.displayName() + ".");
            } else {
                System.out.println("You don't have " + t.displayName() + " in crafted items.");
            }
        }
    }

    public void interact() {
        BlockType t = world.get(player.x(), player.y());
        switch (t) {
            case WOOD -> { System.out.println("You gather wood."); inv.add(BlockType.WOOD.id); }
            case LEAVES -> { System.out.println("You gather leaves."); inv.add(BlockType.LEAVES.id); }
            case STONE -> { System.out.println("You gather stones."); inv.add(BlockType.STONE.id); }
            case IRON_ORE -> { System.out.println("You mine iron ore."); inv.add(BlockType.IRON_ORE.id); }
            default -> System.out.println("Nothing to interact with here.");
        }
    }

    public void craft(int recipe) {
        switch (recipe) {
            case 1 -> { // 2 wood -> planks
                if (inv.contains(BlockType.WOOD.id, 2)) {
                    inv.remove(BlockType.WOOD.id, 2);
                    crafted.add(BlockType.PLANKS);
                    System.out.println("Crafted Wooden Planks.");
                } else System.out.println("Need 2 Wood.");
            }
            case 2 -> { // 1 wood -> stick
                if (inv.contains(BlockType.WOOD.id, 1)) {
                    inv.remove(BlockType.WOOD.id, 1);
                    crafted.add(BlockType.STICK);
                    System.out.println("Crafted Stick.");
                } else System.out.println("Need 1 Wood.");
            }
            case 3 -> { // 3 iron ore -> ingot
                if (inv.contains(BlockType.IRON_ORE.id, 3)) {
                    inv.remove(BlockType.IRON_ORE.id, 3);
                    crafted.add(BlockType.IRON_INGOT);
                    System.out.println("Crafted Iron Ingot.");
                } else System.out.println("Need 3 Iron Ore.");
            }
            default -> System.out.println("Invalid recipe number.");
        }
    }

    public void armUnlock() { unlockArmed = true; }

    public void openSecret() {
        if (unlockArmed) {
            secretDoorUnlocked = true;
            inSecret = true;
            setWorld(WorldGenerator.tricolorSecret(Config.DEFAULT_W, Config.DEFAULT_H));
            fillSecretInventory();
            System.out.println("Secret door unlocked! You entered the secret area.");
        } else {
            System.out.println("Invalid passkey. Try again!");
        }
        unlockArmed = false;
    }

    private void fillSecretInventory() {
        inv.clear();
        for (int id = 1; id <= 4; id++) {
            for (int i = 0; i < Config.INVENTORY_SIZE; i++) inv.add(id);
        }
    }

    public void centerPlayer() { player.center(world); }

    public void afterLoadClamp() { player.clamp(world); }

    public void requestExit() { exit = true; }
}
