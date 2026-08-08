package me.timvinci.terrastorage.inventory;

import me.timvinci.terrastorage.item.StackIdentifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the state of a storage that is the receiver of an operation (Deposit All, Quick Stack).
 * Stores the positions of non-full stacks keyed by their stack identifier, the empty slot indexes, and the set of
 * stackable items already stored, which Smart Deposit uses to decide whether an item may occupy an empty slot.
 * Fullness uses each slot's real max stack size, so storages with per-slot stack limits are handled correctly.
 */
public class StorageState {
    private final Map<StackIdentifier, ArrayList<Integer>> nonFullItemSlots = new HashMap<>();
    private final List<Integer> emptySlots = new ArrayList<>();
    private final Set<StackIdentifier> storedItems = new HashSet<>();
    private boolean modified = false;

    public StorageState(StorageAccess access) {
        for (int i = 0; i < access.size(); i++) {
            ItemStack stack = access.get(i);
            if (stack.isEmpty()) {
                emptySlots.add(i);
                continue;
            }

            StackIdentifier identifier = new StackIdentifier(stack);
            if (stack.getCount() < access.maxStackSize(i, stack)) {
                nonFullItemSlots.computeIfAbsent(identifier, k -> new ArrayList<>()).add(i);
            }

            if (stack.isStackable()) {
                storedItems.add(identifier);
            }
        }
    }

    public Map<StackIdentifier, ArrayList<Integer>> getNonFullItemSlots() {
        return nonFullItemSlots;
    }

    public List<Integer> getEmptySlots() {
        return emptySlots;
    }

    public Set<StackIdentifier> getStoredItems() {
        return storedItems;
    }

    public void setModified() {
        modified = true;
    }

    public boolean wasModified() {
        return modified;
    }
}
