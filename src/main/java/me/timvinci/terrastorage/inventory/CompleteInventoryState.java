package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Represents the state of an inventory.
 * Stores a hashmap in which the key is an Item, and the value is an array of the positions of non-full stacks of that
 * item in the inventory.
 * And a Queue of integers representing the empty slot indexes in the inventory.
 */
public class CompleteInventoryState implements InventoryState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private final Queue<Integer> emptySlots = new ArrayDeque<>();
    private boolean modified = false;

    /**
     * Instantiates a new CompleteInventoryState of an inventory.
     * Iterates over the inventory's slots and adds them to the nonFullItemSlots and emptySlots maps accordingly.
     * @param inventory The storage's inventory.
     */
    public CompleteInventoryState(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack inventoryStack = inventory.getItem(i);
            if (inventoryStack.isEmpty()) {
                emptySlots.add(i);
            }
            else if (inventoryStack.getCount() != inventoryStack.getMaxStackSize()) {
                nonFullItemSlots.computeIfAbsent(new StackIdentifier(inventoryStack), k -> new ArrayList<>()).add(i);
            }
        }
    }

    /**
     * Instantiates a new CompleteInventoryState of a player inventory.
     * Iterates over the player's slots and adds them to the nonFullItemSlots and emptySlots maps accordingly.
     * @param playerInventory The player's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public CompleteInventoryState(Inventory playerInventory, boolean hotbarProtection) {
        for (int i = Inventory.getSelectionSize(); i < playerInventory.getNonEquipmentItems().size(); i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty()) {
                emptySlots.add(i);
            }
            else if (playerStack.getCount() != playerStack.getMaxStackSize()) {
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

        // Check if hotbar protection is disabled, and if that is the case, iterate over the hotbar slots as well.
        if (!hotbarProtection) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack playerStack = playerInventory.getItem(i);
                if (playerStack.isEmpty()) {
                    emptySlots.add(i);
                }
                else if (playerStack.getCount() != playerStack.getMaxStackSize()) {
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
    }

    @Override
    public Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    @Override
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