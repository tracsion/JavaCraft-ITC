package com.uni.javacraft.core;

import com.uni.javacraft.chat.ChatClient;
import com.uni.javacraft.io.InputReader;
import com.uni.javacraft.io.SaveLoadService;
import com.uni.javacraft.render.ConsoleRenderer;
import com.uni.javacraft.world.BlockType;
import com.uni.javacraft.world.WorldGenerator;

public class GameController {
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
                    int id = in.nextInt("Enter block id (0–7) to place: ");
                    state.place(id);
                    pause();
                    break;
                }

                case "c": {
                    System.out.println("Crafting Recipes:\n1) 2 Wood -> Planks\n2) 1 Wood -> Stick\n3) 3 Iron Ore -> Ingot");
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

                case "open": state.openSecret(); pause(); break;

                case "look": ui.lookAround(state); pause(); break;

                case "chat": chat.start(in); break;

                case "exit": state.requestExit(); break;

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
