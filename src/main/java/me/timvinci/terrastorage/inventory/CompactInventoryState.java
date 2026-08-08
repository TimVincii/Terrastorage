package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Represents the state of an inventory.
 * Stores a hashmap in which the key is an Item, and the value is an array of the positions of non-full stacks of that
 * item in the inventory.
 */
public class CompactInventoryState implements InventoryState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private boolean modified = false;

    /**
     * Instantiates a new CompactInventoryState of an inventory.
     * Iterates over the inventory's slots and adds them to the itemSlots map accordingly.
     * @param inventory The storage's inventory.
     */
    public CompactInventoryState(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack inventoryStack = inventory.getItem(i);
            if (inventoryStack.isEmpty() || inventoryStack.getCount() == inventoryStack.getMaxStackSize()) {
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
    public CompactInventoryState(Inventory playerInventory, boolean hotbarProtection) {
        for (int i = Inventory.getSelectionSize(); i < playerInventory.getNonEquipmentItems().size(); i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty() || playerStack.getCount() == playerStack.getMaxStackSize()) {
                continue;
            }

            StackIdentifier stackIdentifier;
            if (!ItemFavoritingUtils.isFavorite(playerStack)) {
                stackIdentifier = new StackIdentifier(playerStack);
            }
            else {
                // Remove the item favorite component data from the stack identifier.
                PatchedDataComponentMap components = new PatchedDataComponentMap(playerStack.getComponents());
                ItemFavoritingUtils.unFavorite(components);
                stackIdentifier = new StackIdentifier(playerStack.getItem(), components);
            }

            nonFullItemSlots.computeIfAbsent(stackIdentifier, k -> new ArrayList<>()).add(i);

        }

        // Check if hotbar protection is disabled, and if that is the case, iterate over the hotbar slots as well.
        if (!hotbarProtection) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack playerStack = playerInventory.getItem(i);
                if (playerStack.isEmpty() || playerStack.getCount() == playerStack.getMaxStackSize()) {
                    continue;
                }

                StackIdentifier stackIdentifier;
                if (!ItemFavoritingUtils.isFavorite(playerStack)) {
                    stackIdentifier = new StackIdentifier(playerStack);
                }
                else {
                    // Remove the item favorite component data from the stack identifier.
                    PatchedDataComponentMap components = new PatchedDataComponentMap(playerStack.getComponents());
                    ItemFavoritingUtils.unFavorite(components);
                    stackIdentifier = new StackIdentifier(playerStack.getItem(), components);
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
