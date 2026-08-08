package me.timvinci.terrastorage.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A rule-aware view of the player's inventory, and the player side counterpart to {@link StorageAccess}, used both
 * when the player is the source of a transfer (Deposit All, Quick Stack) and when it is the receiver (Loot All,
 * Restock).
 * <p>
 * This deliberately isn't a {@link StorageAccess}: inserting through Slot.safeInsert would compare components
 * exactly, and so refuse to stack a looted item onto an existing favorited one. The slot rules are exposed here,
 * while the favorite agnostic merging stays in {@link InventoryUtils}.
 * <p>
 * Indexes with no matching menu slot are treated as permitted, so menus that only expose some of the player's slots
 * keep working.
 */
public class PlayerSlotAccess {
    private final Player player;
    private final Inventory inventory;
    private final Slot[] slotsByIndex;

    /**
     * Builds a view of the player's inventory as exposed by the given menu.
     * @param menu The player's open menu.
     * @param player The player.
     */
    public PlayerSlotAccess(AbstractContainerMenu menu, Player player) {
        this.player = player;
        this.inventory = player.getInventory();
        this.slotsByIndex = new Slot[inventory.getContainerSize()];

        for (Slot slot : menu.slots) {
            if (slot.container != inventory) {
                continue;
            }

            int index = slot.getContainerSlot();
            // If a menu registers the same inventory index more than once, the first slot wins.
            if (index >= 0 && index < slotsByIndex.length && slotsByIndex[index] == null) {
                slotsByIndex[index] = slot;
            }
        }
    }

    /**
     * @return The player's inventory this view is backed by.
     */
    public Inventory inventory() {
        return inventory;
    }

    /**
     * @param index The player inventory index.
     * @return The menu slot backing the index, or null if the menu doesn't expose it.
     */
    private Slot slotAt(int index) {
        return index >= 0 && index < slotsByIndex.length ? slotsByIndex[index] : null;
    }

    /**
     * The main example of an index that can't be taken from is a mod marking the slot of the currently open storage
     * item as un-pickable, so that it can't be placed inside itself.
     * @param index The player inventory index.
     * @return Whether items may be taken from this index.
     */
    public boolean canTake(int index) {
        Slot slot = slotAt(index);
        return slot == null || slot.mayPickup(player);
    }

    /**
     * Unlike canTake this takes the incoming stack, since a slot may accept one item while rejecting another, and so
     * placement can't be precomputed the way pickup can.
     * @param index The player inventory index.
     * @param stack The stack being considered for this index.
     * @return Whether the stack may be placed at this index.
     */
    public boolean canPlace(int index, ItemStack stack) {
        Slot slot = slotAt(index);
        return slot == null ? inventory.canPlaceItem(index, stack) : slot.mayPlace(stack);
    }

    /**
     * @param index The player inventory index.
     * @param stack The stack being considered for this index.
     * @return The maximum number of items of {@code stack} the index can hold.
     */
    public int maxStackSize(int index, ItemStack stack) {
        Slot slot = slotAt(index);
        return slot == null ? inventory.getMaxStackSize(stack) : slot.getMaxStackSize(stack);
    }
}
