package me.timvinci.terrastorage.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Represents the state of an inventory.
 * Stores a hashmap in which the key is an Item, and the value is an array of the positions of non-full stacks of that
 * item in the inventory.
 * And a Queue of integers representing the empty slot indexes in the inventory.
 */
public class CompleteInventoryState implements InventoryState {
    private final Map<Item, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private final Queue<Integer> emptySlots = new ArrayDeque<>();
    private boolean modified = false;

    /**
     * Instantiates a new CompleteInventoryState of an inventory.
     * Iterates over the inventory's slots and adds them to the nonFullItemSlots and emptySlots maps accordingly.
     * @param inventory The storage's inventory.
     */
    public CompleteInventoryState(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (inventoryStack.isEmpty()) {
                emptySlots.add(i);
            }
            else if (inventoryStack.getCount() != inventoryStack.getMaxCount()) {
                nonFullItemSlots.computeIfAbsent(inventoryStack.getItem(), k -> new ArrayList<>()).add(i);
            }
        }
    }

    /**
     * Instantiates a new CompleteInventoryState of a player inventory.
     * Iterates over the player's slots and adds them to the nonFullItemSlots and emptySlots maps accordingly.
     * @param playerInventory The player's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public CompleteInventoryState(PlayerInventory playerInventory, boolean hotbarProtection) {
        for (int i = PlayerInventory.getHotbarSize(); i < playerInventory.main.size(); i++) {
            ItemStack playerStack = playerInventory.getStack(i);
            if (playerStack.isEmpty()) {
                emptySlots.add(i);
            }
            else if (playerStack.getCount() != playerStack.getMaxCount()) {
                nonFullItemSlots.computeIfAbsent(playerStack.getItem(), k -> new ArrayList<>()).add(i);
            }
        }

        // Check if hotbar protection is disabled, and if that is the case, iterate over the hotbar slots as well.
        if (!hotbarProtection) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);
                if (playerStack.isEmpty()) {
                    emptySlots.add(i);
                }
                else if (playerStack.getCount() != playerStack.getMaxCount()) {
                    nonFullItemSlots.computeIfAbsent(playerStack.getItem(), k -> new ArrayList<>()).add(i);
                }
            }
        }
    }

    @Override
    public Map<Item, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    public Queue<Integer> getEmptySlots() {
        return emptySlots;
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