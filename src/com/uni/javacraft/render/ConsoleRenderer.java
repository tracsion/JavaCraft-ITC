package com.uni.javacraft.render;

import com.uni.javacraft.core.GameState;
import com.uni.javacraft.world.BlockType;

public class ConsoleRenderer {

    public void welcome() {
        System.out.println(AnsiPalette.GREEN + "Welcome to Simple Minecraft!" + AnsiPalette.RESET);
        System.out.println("Instructions:");
        System.out.println(" - Use 'W', 'A', 'S', 'D', or arrow keys to move.");
        System.out.println(" - 'M' mine | 'P' place | 'C' craft | 'I' interact");
        System.out.println(" - 'Save' / 'Load' game | 'Unlock' then 'Open' secret");
        System.out.println(" - 'Look' around | 'Chat' to open chat | 'Exit' to quit");
        System.out.println();
    }

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void draw(GameState s) {
        drawLegend();
        drawWorld(s);
        drawInventory(s);
    }

    public void warn(String msg) {
        System.out.println(AnsiPalette.YELLOW + msg + AnsiPalette.RESET);
    }

    public void pausePrompt() {
        System.out.println("Press Enter to continue...");
    }

    public void lookAround(GameState s) {
        System.out.println("You look around and see:");
        int px = s.player().x();
        int py = s.player().y();
        var w = s.world();
        for (int y = Math.max(0, py - 1); y <= Math.min(py + 1, w.height() - 1); y++) {
            for (int x = Math.max(0, px - 1); x <= Math.min(px + 1, w.width() - 1); x++) {
                if (x == px && y == py) {
                    System.out.print((s.inSecretArea() ? AnsiPalette.BLUE : AnsiPalette.GREEN) + "P " + AnsiPalette.RESET);
                } else {
                    System.out.print(symbol(w.get(x, y)) + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private void drawLegend() {
        System.out.println(AnsiPalette.BLUE + "Legend:");
        System.out.println(AnsiPalette.WHITE + "-- - Empty");
        System.out.println(AnsiPalette.RED   + "\u2592\u2592 - Wood");
        System.out.println(AnsiPalette.GREEN + "\u00A7\u00A7 - Leaves");
        System.out.println(AnsiPalette.BLUE  + "\u2593\u2593 - Stone");
        System.out.println(AnsiPalette.WHITE + "\u00B0\u00B0 - Iron ore");
        System.out.println(AnsiPalette.YELLOW + "\u00D7\u00D7 - Gold");
        System.out.println(AnsiPalette.CYAN + "\u06DE\u06DE - Diamond");
        System.out.println(AnsiPalette.BLUE  + "P - Player" + AnsiPalette.RESET);
    }

    private void drawWorld(GameState s) {
        System.out.println(AnsiPalette.CYAN + "World Map:" + AnsiPalette.RESET);
        int W = s.world().width();
        System.out.println("╔══" + "═".repeat(W * 2 - 2) + "╗");
        for (int y = 0; y < s.world().height(); y++) {
            System.out.print("║");
            for (int x = 0; x < W; x++) {
                if (x == s.player().x() && y == s.player().y()) {
                    System.out.print((s.inSecretArea() ? AnsiPalette.BLUE : AnsiPalette.GREEN) + "P " + AnsiPalette.RESET);
                } else {
                    System.out.print(symbol(s.world().get(x, y)) + " ");
                }
            }
            System.out.println("║");
        }
        System.out.println("╚══" + "═".repeat(W * 2 - 2) + "╝");
    }

    private void drawInventory(GameState s) {
        System.out.println("Inventory:");
        if (s.inv().isEmpty()) {
            System.out.println(AnsiPalette.YELLOW + "Empty" + AnsiPalette.RESET);
        } else {
            int maxId = 7;
            int[] counts = new int[maxId + 1];
            for (int id : s.inv().items()) {
                if (id >= 0 && id <= maxId) counts[id]++;
            }
            for (int id = 1; id <= maxId; id++) {
                if (counts[id] > 0) {
                    System.out.println(BlockType.fromId(id).displayName() + " - " + counts[id]);
                }
            }
        }
        System.out.println("Crafted Items:");
        if (s.crafted().isEmpty()) {
            System.out.println(AnsiPalette.YELLOW + "None" + AnsiPalette.RESET);
        } else {
            var names = s.crafted().displayNames();
            System.out.println(AnsiPalette.BROWN + String.join(", ", names) + AnsiPalette.RESET);
        }
        System.out.println();
    }

    private String symbol(BlockType t) {
        switch (t) {
            case AIR:       return AnsiPalette.RESET + "-";
            case WOOD:      return AnsiPalette.RED    + "\u2592";
            case LEAVES:    return AnsiPalette.GREEN  + "\u00A7";
            case STONE:     return AnsiPalette.BLUE   + "\u2593";
            case IRON_ORE:  return AnsiPalette.WHITE  + "\u00B0";
            case PLANKS:    return AnsiPalette.BROWN  + "W";
            case STICK:     return AnsiPalette.BROWN  + "T";
            case IRON_INGOT:return AnsiPalette.YELLOW + "I";
            case DIAMOND:   return AnsiPalette.CYAN   + "\u06DE";
            case GOLD:      return AnsiPalette.YELLOW + "\u00D7";
            case GOLDCHAIN: return AnsiPalette.YELLOW + "G";
            default:        return AnsiPalette.RESET  + "-";
        }
    }
}
