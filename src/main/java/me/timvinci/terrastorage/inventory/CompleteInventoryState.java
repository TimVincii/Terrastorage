package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Represents the state of a player's inventory (the receiver of Loot All).
 * Stores a hashmap in which the key is a stack identifier, and the value is an array of the positions of non-full
 * stacks of that stack identifier in the inventory, as well as a list of the empty slot indexes.
 * Whether a slot accepts an item is not precomputed here, as that depends on the incoming stack.
 */
public class CompleteInventoryState implements InventoryState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private final List<Integer> emptySlots = new ArrayList<>();
    private boolean modified = false;

    /**
     * Instantiates a new CompleteInventoryState of a player inventory.
     * Iterates over the player's slots and adds them to the nonFullItemSlots and emptySlots collections accordingly.
     * @param access The rule aware view of the player's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public CompleteInventoryState(PlayerSlotAccess access, boolean hotbarProtection) {
        Inventory playerInventory = access.inventory();
        index(access, playerInventory, Inventory.getSelectionSize(), playerInventory.items.size());

        // Check if hotbar protection is disabled, and if that is the case, iterate over the hotbar slots as well.
        if (!hotbarProtection) {
            index(access, playerInventory, 0, Inventory.getSelectionSize());
        }
    }

    /**
     * Indexes a range of the player's inventory into the empty and non-full collections.
     */
    private void index(PlayerSlotAccess access, Inventory playerInventory, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty()) {
                emptySlots.add(i);
            }
            else if (playerStack.getCount() < access.maxStackSize(i, playerStack)) {
                nonFullItemSlots.computeIfAbsent(InventoryUtils.favoriteAgnosticIdentifier(playerStack), k -> new ArrayList<>()).add(i);
            }
        }
    }

    @Override
    public Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    @Override
    public List<Integer> getEmptySlots() {
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
