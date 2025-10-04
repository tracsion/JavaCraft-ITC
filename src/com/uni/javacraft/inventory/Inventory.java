package com.uni.javacraft.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Inventory implements Serializable {
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
