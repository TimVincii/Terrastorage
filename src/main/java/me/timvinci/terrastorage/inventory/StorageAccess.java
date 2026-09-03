package me.timvinci.terrastorage.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A rule-aware, index-based view of a storage, targeted by all of Terrastorage's storage operations.
 * Every write goes through take/insert, which persist the change to whatever backs the storage, instead of mutating
 * stacks in place (which only works when the storage hands back live stack references).
 * Implemented by {@link SlotStorageAccess} (the primary, slot backed path) and {@link ContainerStorageAccess}.
 */
public interface StorageAccess {

    /**
     * @return The number of slots in this storage.
     */
    int size();

    /**
     * Returns the stack in the given slot, which must never be mutated; copy it first if a working stack is needed.
     * @param index The slot index.
     * @return The stack in the slot (possibly empty).
     */
    ItemStack get(int index);

    /**
     * @param index The slot index.
     * @param player The player performing the operation.
     * @return Whether items may be taken from this slot by the player.
     */
    boolean canTake(int index, Player player);

    /**
     * Removes up to {@code amount} items from the slot, persisting the removal to the backing storage.
     * @param index The slot index.
     * @param amount The maximum number of items to remove.
     * @param player The player performing the operation.
     * @return The removed stack (possibly empty).
     */
    ItemStack take(int index, int amount, Player player);

    /**
     * Inserts as much of the stack as the slot's rules and capacity allow, persisting the insertion.
     * The provided stack is shrunk by the amount inserted, so the caller can inspect what's left.
     * @param index The slot index.
     * @param stack The stack to insert; mutated in place.
     */
    void insert(int index, ItemStack stack);

    /**
     * @param index The slot index.
     * @param stack The stack being considered for this slot.
     * @return The maximum number of items of {@code stack} the slot can hold.
     */
    int maxStackSize(int index, ItemStack stack);
}
