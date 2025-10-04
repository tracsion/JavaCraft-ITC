package com.uni.javacraft.io;

import com.uni.javacraft.core.GameState;

import java.io.*;

public class SaveLoadService {

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
            // Copy fields from loaded to into (simple swap is okay if you return loaded instead)
            // Here we just return the loaded instance to the caller (see controller).
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
