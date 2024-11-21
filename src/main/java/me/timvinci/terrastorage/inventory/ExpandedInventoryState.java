package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Represents the state of an inventory.
 * Stores a hashmap in which the key is a stack identifier, and the value is an array of the positions of non-full stacks of that
 * stack identifier in the inventory, a Queue of integers representing the empty slot indexes in the inventory, and a set of
 * StackIdentifiers storing which items exist in the inventory.
 */
public class ExpandedInventoryState implements InventoryState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private final Queue<Integer> emptySlots = new ArrayDeque<>();
    private final Set<StackIdentifier> storedItems = new HashSet<>();
    private boolean modified = false;

    /**
     * Instantiates a new ExpandedInventoryState of an inventory.
     * Iterates over the inventory's slots and adds them to the nonFullItemSlots and emptySlots maps, as well as the
     * storedItems set.
     * @param inventory The storage's inventory.
     */
    public ExpandedInventoryState(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (inventoryStack.isEmpty()) {
                emptySlots.add(i);
            }
            else {
                StackIdentifier stackIdentifier = new StackIdentifier(inventoryStack);
                if (inventoryStack.getCount() != inventoryStack.getMaxCount()) {
                    nonFullItemSlots.computeIfAbsent(stackIdentifier, k -> new ArrayList<>()).add(i);
                }

                if (inventoryStack.isStackable()) {
                    storedItems.add(stackIdentifier);
                }
            }
        }
    }

    @Override
    public Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    @Override
    public Queue<Integer> getEmptySlots() {
        return emptySlots;
    }

    public Set<StackIdentifier> getStoredItems() {
        return storedItems;
    }

    @Override
    public void setModified() {
        modified = true;
    }

    @Override
    public boolean wasModified() {
        return modified;
    }
}
