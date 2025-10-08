import java.io.*;
import java.net.*;
import java.util.*;

/*

    Combined all of the files together everything is still the same.
    Original github link https://github.com/tracsion/JavaCraft-ITC
    We had the code seperated into sections originaly on our github project so the team can work more effectively on it.
    A lot of original code has been optimized in the proccess of development.
    You can see the commits on github.
    Hopefuly we did a good job
    Hector, Jan, Theodor, Nikos

*/

// this is the entrypoint
public class JavaCraft {
    public static void main(String[] args) {
        // start the gameloop
        new GameController().run();
    }
}

// chat section (configuration of the chat server connection)
final class ChatConfig {
    private ChatConfig() {}
    //chat server hostname and the port
    public static final String HOST = "chat.bcs1110.svc.leastfixedpoint.nl";
    public static final int PORT = 5999;
    // shared encryption key for beaufort encryption
    public static final String KEY = "IAMAFRIENDLYPERSONRLLY"; // dont change this pls its rlly nice passkey
    // password to auth with the server
    public static final String PASSWORD = "F4EF9A36-5FCD-4D27-8A0A-FC7C77D3DBB2";
}

// implementation of beaufort encryption
// used to encrypt and decrypt messages
final class BeaufortCipher {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // encryption alphabet

    // encryption and decryption are symetric
    public static String encrypt(String text, String key) { return process(text, key); }
    public static String decrypt(String text, String key) { return process(text, key); }

    // core cipher logic
    private static String process(String text, String key) {
        text = text.toUpperCase();
        key = key.toUpperCase();
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;
        for (char c : text.toCharArray()) {
            // only encrypt letters
            if (Character.isLetter(c)) {
                int p = ALPHABET.indexOf(c);
                int k = ALPHABET.indexOf(key.charAt(keyIndex));
                // beaufort formula k - p = c
                int cpos = (k - p + 26) % 26;
                result.append(ALPHABET.charAt(cpos));
                keyIndex = (keyIndex + 1) % key.length();
            } else {
                // non letters will stay the same
                result.append(c);
            }
        }
        return result.toString();
    }
}

// the message validator was weird and hard to make and idk if this is the correct way
// it checks if a messages follows this format @username: text

final class MessageValidator {
    private MessageValidator() {}

    public static boolean isValid(String s) {
        if (s == null) return false;
        int i = 0, n = s.length();

        if (n == 0 || s.charAt(i) != '@') return false; // '@'
        i++;

        if (i >= n || !isLetter(s.charAt(i))) return false; // first letter
        i++;
        while (i < n && isNameChar(s.charAt(i))) i++;

        if (i >= n || s.charAt(i) != ':') return false; // ':'
        i++;

        if (i >= n || s.charAt(i) != ' ') return false; // space
        i++;

        if (i >= n) return false; // text must exist

        for (; i < n; i++) { // printable
            char c = s.charAt(i);
            if (Character.isISOControl(c)) return false;
        }
        return true;
    }

    private static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    private static boolean isNameChar(char c) {
        return isLetter(c) || (c >= '0' && c <= '9') || c == '_';
    }
}

// this section has been optimized
// chat client handles the server connection also sendying encrypted messages and read incoming messages
class ChatClient {
    public void start(InputReader in) {
        System.out.println("------ Chat Client ------");
        System.out.print("Enter your username (without @): ");
        String username = in.nextLine("").trim();
        if (username.isEmpty()) username = "player"; // default name if user dont provide

        final String key = ChatConfig.KEY;
        System.out.println("Using shared key: " + key);
        System.out.println("\nConnected as @" + username);
        System.out.println("Message format: @" + username + ": your message");
        System.out.println("Type '/quit' to exit.\n");

        try (Socket socket = new Socket(ChatConfig.HOST, ChatConfig.PORT)) {
            socket.setSoTimeout(2500);
            var inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var output = new PrintWriter(socket.getOutputStream(), true);

            // authenticate server
            output.println(ChatConfig.PASSWORD);

            while (true) {
                output.println(""); // ping server

                // read incomsing messages from server
                while (true) {
                    String line;
                    try {
                        line = inputFromServer.readLine();
                    } catch (java.net.SocketTimeoutException timeout) {
                        break;
                    }
                    if (line == null) {
                        System.out.println("Server closed connection.");
                        return;
                    }
                    if (line.equals("+")) break;

                    if (line.startsWith("* ") || line.endsWith("connected") || line.endsWith("disconnected")) {
                        System.out.println(line);
                        continue;
                    }

                    // extracts the encrypted message after >>>
                    String encryptedPart = line;
                    int arrowIndex = line.indexOf(">>> ");
                    if (arrowIndex != -1 && arrowIndex + 4 < line.length()) {
                        encryptedPart = line.substring(arrowIndex + 4).trim();
                    }

                    // and decrypts the message
                    String decrypted = BeaufortCipher.decrypt(encryptedPart, key);

                    // it also only shows valid messages
                    if (MessageValidator.isValid(decrypted)) {
                        System.out.println(decrypted);
                    }
                }

                // user input so it looks beautiful
                System.out.print("> ");
                String msg = in.nextLine("").trim();

                if (msg.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting chat.");
                    return;
                }

                //validates the formal
                if (!MessageValidator.isValid(msg)) {
                    System.out.println("Invalid format. Use: @" + username + ": your message");
                    continue;
                }

                // enforces the user to tag himself correctly
                if (!msg.startsWith("@" + username + ":")) {
                    System.out.println("You must tag yourself as @" + username + ":");
                    continue;
                }

                // encrypts and sends
                String enc = BeaufortCipher.encrypt(msg, key);
                output.println(enc);
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("Unexpected error: " + t.getMessage());
        }
    }
}

// core section
final class Config {
    private Config() {}
    public static final int DEFAULT_W = 25;
    public static final int DEFAULT_H = 15;
    public static final int INVENTORY_SIZE = 100; // after secret door inventory make it as big as you want
}

class GameController {
    private final InputReader in = new InputReader();
    private final ConsoleRenderer ui = new ConsoleRenderer();
    private final SaveLoadService save = new SaveLoadService();
    private final ChatClient chat = new ChatClient();

    private GameState state = GameState.createDefault();

    public void run() {
        ui.welcome();
        if (!in.confirm("Start the game? (Y/N): ")) {
            System.out.println("Game not started. Goodbye!");
            return;
        }

        while (!state.isExitRequested()) {
            ui.clear();
            ui.draw(state);
            String token = in.nextTokenLower(
                    "Enter action: WASD move | M mine | P place | C craft | I interact | Save | Load | Unlock | Open | Look | Chat | Exit: "
            );

            switch (token) {
                case "w": case "up":    state.move(0,-1); break;
                case "s": case "down":  state.move(0, 1); break;
                case "a": case "left":  state.move(-1,0); break;
                case "d": case "right": state.move(1, 0); break;

                case "m": state.mine(); pause(); break;

                case "p": {
                    int id = in.nextInt("Enter block id (0–10) to place: ");
                    state.place(id);
                    pause();
                    break;
                }

                case "c": {
                    System.out.println("Crafting Recipes:\n1) 2 Wood -> Planks\n2) 1 Wood -> Stick\n3) 3 Iron Ore -> Ingot" +
                            "\n4) 2 Gold and 1 Diamond -> Gold Chain");
                    int recipe = in.nextInt("Recipe number: ");
                    state.craft(recipe);
                    pause();
                    break;
                }

                case "i": state.interact(); pause(); break;

                case "save": {
                    String fn = in.nextToken("Filename: ");
                    save.save(state, fn);
                    pause();
                    break;
                }

                case "load": {
                    String fn = in.nextToken("Filename: ");
                    GameState loaded = save.load(fn);
                    if (loaded != null) {
                        state = loaded;
                        state.afterLoadClamp();
                        System.out.println("Game loaded from: " + fn);
                    }
                    pause();
                    break;
                }

                case "unlock": state.armUnlock(); break;
                case "open":   state.openSecret(); pause(); break;
                case "look":   ui.lookAround(state); pause(); break;
                case "chat":   chat.start(in); break;
                case "exit":   state.requestExit(); break;

                default: ui.warn("Invalid input. Please try again."); pause(); break;
            }
        }

        System.out.println("Exiting the game. Goodbye!");
    }

    private void pause() {
        ui.pausePrompt();
        in.waitEnter();
    }
}

class GameState implements Serializable {
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

    public World world() { return world; }
    public Player player() { return player; }
    public Inventory inv() { return inv; }
    public CraftedInventory crafted() { return crafted; }
    public boolean inSecretArea() { return inSecret; }
    public boolean isExitRequested() { return exit; }

    public void setWorld(World w) {
        this.world = w;
        player.clamp(world);
    }

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
        if (id < 0 || id > 10) {
            System.out.println("Invalid block number (0–10).");
            return;
        }
        BlockType t = BlockType.fromId(id);
        if (id <= 6) {
            if (inv.contains(id)) {
                inv.remove(id, 1);
                world.set(player.x(), player.y(), t);
                System.out.println("Placed " + t.displayName() + ".");
            } else {
                System.out.println("You don't have " + t.displayName() + ".");
            }
        } else {
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
            case GOLD -> { System.out.println("You mine gold"); inv.add(BlockType.GOLD.id); }
            case DIAMOND -> { System.out.println("You mine diamond"); inv.add(BlockType.DIAMOND.id); }
            default -> System.out.println("Nothing to interact with here.");
        }
    }

    public void craft(int recipe) {
        switch (recipe) {
            case 1 -> {
                if (inv.contains(BlockType.WOOD.id, 2)) {
                    inv.remove(BlockType.WOOD.id, 2);
                    crafted.add(BlockType.PLANKS);
                    System.out.println("Crafted Wooden Planks.");
                } else System.out.println("Need 2 Wood.");
            }
            case 2 -> {
                if (inv.contains(BlockType.WOOD.id, 1)) {
                    inv.remove(BlockType.WOOD.id, 1);
                    crafted.add(BlockType.STICK);
                    System.out.println("Crafted Stick.");
                } else System.out.println("Need 1 Wood.");
            }
            case 3 -> {
                if (inv.contains(BlockType.IRON_ORE.id, 3)) {
                    inv.remove(BlockType.IRON_ORE.id, 3);
                    crafted.add(BlockType.IRON_INGOT);
                    System.out.println("Crafted Iron Ingot.");
                } else System.out.println("Need 3 Iron Ore.");
            }
            case 4 -> {
                if ((inv.contains(BlockType.GOLD.id, 2)) && inv.contains(BlockType.DIAMOND.id , 1)) {
                    inv.remove(BlockType.GOLD.id, 2);
                    inv.remove(BlockType.DIAMOND.id, 1);
                    crafted.add(BlockType.GOLDCHAIN);
                    System.out.println("Crafted Gold Chain");
                } else System.out.println("Need 2 Gold and 1 Diamond");
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
        for (int id = 1; id <= 6; id++) {
            for (int i = 0; i < Config.INVENTORY_SIZE; i++) inv.add(id);
        }
    }

    public void centerPlayer() { player.center(world); }
    public void afterLoadClamp() { player.clamp(world); }
    public void requestExit() { exit = true; }
}

// inventory section
class Inventory implements Serializable {
    private final List<Integer> items = new ArrayList<>();

    public boolean isEmpty() { return items.isEmpty(); }
    public List<Integer> items() { return Collections.unmodifiableList(items); }

    public void add(int id) { items.add(id); }

    public boolean contains(int id) { return items.contains(id); }

    public boolean contains(int id, int count) {
        int c = 0;
        for (int v : items) if (v == id && ++c >= count) return true;
        return false;
    }

    public void remove(int id, int count) {
        int removed = 0;
        for (Iterator<Integer> it = items.iterator(); it.hasNext();) {
            if (it.next() == id) { it.remove(); if (++removed == count) break; }
        }
    }

    public void clear() { items.clear(); }
}

class CraftedInventory implements Serializable {
    private final List<BlockType> crafted = new ArrayList<>();

    public boolean isEmpty() { return crafted.isEmpty(); }
    public List<BlockType> all() { return Collections.unmodifiableList(crafted); }
    public void add(BlockType t) { crafted.add(t); }
    public boolean removeOne(BlockType t) { return crafted.remove(t); }

    public List<String> displayNames() {
        return crafted.stream().map(BlockType::displayName).toList();
    }
}

// io section
class InputReader {
    private final Scanner sc = new Scanner(System.in);

    public boolean confirm(String prompt) {
        System.out.print(prompt);
        String s = sc.next().trim().toUpperCase();
        sc.nextLine();
        return s.equals("Y");
    }

    public String nextToken(String prompt) {
        System.out.print(prompt);
        String t = sc.next();
        sc.nextLine();
        return t;
    }

    public String nextTokenLower(String prompt) {
        return nextToken(prompt).toLowerCase();
    }

    public int nextInt(String prompt) {
        System.out.print(prompt);
        int v = sc.nextInt();
        sc.nextLine();
        return v;
    }

    public String nextLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public void waitEnter() {
        sc.nextLine();
    }
}

class SaveLoadService {
    public void save(GameState state, String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(state);
            System.out.println("Game saved to: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    public void loadInto(GameState into, String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            GameState loaded = (GameState) in.readObject();
            throw new UnsupportedOperationException("Controller replaces state instance; see GameController.loadState().");
        } catch (UnsupportedOperationException uoe) {
            throw uoe;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading: " + e.getMessage());
        }
    }

    public GameState load(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading: " + e.getMessage());
            return null;
        }
    }
}

// player section
class Player implements Serializable {
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

// render section
final class AnsiPalette {
    private AnsiPalette() {}

    public static final String RESET  = "\u001B[0m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN   = "\u001B[36m";
    public static final String RED    = "\u001B[31m";
    public static final String PURPLE = "\u001B[35m"; // not used honestly i should remove this but iam scared maybe we will use them
    public static final String BLUE   = "\u001B[34m";
    public static final String GRAY   = "\u001B[37m"; // not used honestly i should remove this but iam scared maybe we will use them
    public static final String WHITE  = "\u001B[97m";
    public static final String BROWN  = "\u001B[38;5;94m";
}

class ConsoleRenderer {
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
            case AIR:        return AnsiPalette.RESET + "-";
            case WOOD:       return AnsiPalette.RED    + "\u2592";
            case LEAVES:     return AnsiPalette.GREEN  + "\u00A7";
            case STONE:      return AnsiPalette.BLUE   + "\u2593";
            case IRON_ORE:   return AnsiPalette.WHITE  + "\u00B0";
            case PLANKS:     return AnsiPalette.BROWN  + "W";
            case STICK:      return AnsiPalette.BROWN  + "T";
            case IRON_INGOT: return AnsiPalette.YELLOW + "I";
            case DIAMOND:    return AnsiPalette.CYAN   + "\u06DE";
            case GOLD:       return AnsiPalette.YELLOW + "\u00D7";
            case GOLDCHAIN:  return AnsiPalette.YELLOW + "G";
            default:         return AnsiPalette.RESET  + "-";
        }
    }
}

// world section
enum BlockType {
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
    GOLDCHAIN(10, "Gold chain"); // how did u come up with this theodor golden chain bro???

    public final int id;
    private final String display;

    BlockType(int id, String display) { this.id = id; this.display = display; }

    public static BlockType fromId(int id) {
        for (var b : values()) if (b.id == id) return b;
        return AIR;
    }

    public String displayName() { return display; }
}

class World implements Serializable {
    private final int w, h;
    private final int[][] grid;

    public World(int w, int h) {
        this.w = w; this.h = h;
        this.grid = new int[w][h];
    }

    public int width() { return w; }
    public int height() { return h; }

    public boolean inBounds(int x, int y) { return x >= 0 && y >= 0 && x < w && y < h; }

    public BlockType get(int x, int y) { return BlockType.fromId(grid[x][y]); }
    public void set(int x, int y, BlockType t) { grid[x][y] = t.id; }

    public int[][] raw() { return grid; }
}

final class WorldGenerator {
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
                        : (r < 80) ? BlockType.GOLD
                        : (r < 85) ? BlockType.DIAMOND
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
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.WOOD);
        for (int y = stripe; y < 2*stripe; y++)
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.IRON_ORE);
        for (int y = 2*stripe; y < h; y++)
            for (int x = 0; x < w; x++) world.set(x, y, BlockType.STONE);
        return world;
    }
}

//finally
