package com.uni.javacraft.inventory;

import com.uni.javacraft.world.BlockType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftedInventory implements Serializable {
    private final List<BlockType> crafted = new ArrayList<>();

    public boolean isEmpty() { return crafted.isEmpty(); }
    public List<BlockType> all() { return Collections.unmodifiableList(crafted); }
    public void add(BlockType t) { crafted.add(t); }
    public boolean removeOne(BlockType t) { return crafted.remove(t); }

    public List<String> displayNames() {
        return crafted.stream().map(BlockType::displayName).toList();
    }
}
