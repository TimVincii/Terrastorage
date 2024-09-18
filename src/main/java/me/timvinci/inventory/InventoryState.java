package me.timvinci.inventory;

import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Map;

/**
 * Defines a structure for tracking non-full item stacks in an inventory.
 */
public interface InventoryState {
    Map<Item, ArrayList<Integer>> getNonFullItemSlots();
    void setModified();
    boolean wasModified();
}
