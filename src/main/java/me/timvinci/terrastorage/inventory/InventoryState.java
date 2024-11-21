package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.item.StackIdentifier;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

/**
 * Defines a structure for tracking slot types in an inventory.
 */
public interface InventoryState {
    Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots();
    Queue<Integer> getEmptySlots();
    void setModified();
    boolean wasModified();
}
