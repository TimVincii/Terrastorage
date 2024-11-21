package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Represents the state of an inventory.
 * Stores a hashmap in which the key is a stack identifier, and the value is an array of the positions of non-full stacks of that
 * stack identifier in the inventory.
 */
public class CompactInventoryState implements InventoryState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private boolean modified = false;

    /**
     * Instantiates a new CompactInventoryState of an inventory.
     * Iterates over the inventory's slots and adds them to the itemSlots map accordingly.
     * @param inventory The storage's inventory.
     */
    public CompactInventoryState(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inventoryStack = inventory.getStack(i);
            if (inventoryStack.isEmpty() || inventoryStack.getCount() == inventoryStack.getMaxCount()) {
                continue;
            }

            nonFullItemSlots.computeIfAbsent(new StackIdentifier(inventoryStack), k -> new ArrayList<>()).add(i);
        }
    }

    /**
     * Instantiates a new CompactInventoryState of a player inventory.
     * Iterates over the player's slots and adds them to the itemSlots map accordingly.
     * @param playerInventory The player's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public CompactInventoryState(PlayerInventory playerInventory, boolean hotbarProtection) {
        for (int i = PlayerInventory.getHotbarSize(); i < playerInventory.main.size(); i++) {
            ItemStack playerStack = playerInventory.getStack(i);
            if (playerStack.isEmpty() || playerStack.getCount() == playerStack.getMaxCount()) {
                continue;
            }

            StackIdentifier stackIdentifier;
            if (!ItemFavoritingUtils.isFavorite(playerStack)) {
                stackIdentifier = new StackIdentifier(playerStack);
            }
            else {
                // Remove the item favorite nbt data from the stack identifier.
                NbtCompound copiedCompound = playerStack.getNbt().copy();
                ItemFavoritingUtils.unFavorite(copiedCompound);
                if (copiedCompound.isEmpty()) {
                    copiedCompound = null;
                }

                stackIdentifier = new StackIdentifier(playerStack.getItem(), copiedCompound);
            }

            nonFullItemSlots.computeIfAbsent(stackIdentifier, k -> new ArrayList<>()).add(i);
        }

        // Check if hotbar protection is disabled, and if that is the case, iterate over the hotbar slots as well.
        if (!hotbarProtection) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);
                if (playerStack.isEmpty() || playerStack.getCount() == playerStack.getMaxCount()) {
                    continue;
                }

                StackIdentifier stackIdentifier;
                if (!ItemFavoritingUtils.isFavorite(playerStack)) {
                    stackIdentifier = new StackIdentifier(playerStack);
                }
                else {
                    // Remove the item favorite nbt data from the stack identifier.
                    NbtCompound copiedCompound = playerStack.getNbt().copy();
                    ItemFavoritingUtils.unFavorite(copiedCompound);
                    if (copiedCompound.isEmpty()) {
                        copiedCompound = null;
                    }

                    stackIdentifier = new StackIdentifier(playerStack.getItem(), copiedCompound);
                }

                nonFullItemSlots.computeIfAbsent(stackIdentifier, k -> new ArrayList<>()).add(i);
            }
        }
    }

    @Override
    public Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    @Override
    public Queue<Integer> getEmptySlots() {
        return null;
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
