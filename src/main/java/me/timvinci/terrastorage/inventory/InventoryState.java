package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.item.StackIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Defines a structure for tracking slot types in an inventory.
 * The empty slots are a list rather than a queue because a slot must only leave the pool once it has actually
 * received items, otherwise a single rejected item starves every later item in the same operation.
 */
public interface InventoryState {
    Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots();
    List<Integer> getEmptySlots();
    void setModified();
    boolean wasModified();
}
