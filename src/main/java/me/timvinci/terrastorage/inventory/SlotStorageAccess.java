package me.timvinci.terrastorage.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The primary {@link StorageAccess} implementation, backed by the slots of an open menu.
 * Item-handler backed slots override the vanilla slot API to route through their real storage, which is why this
 * path persists correctly where poking the backing Container does not.
 */
public class SlotStorageAccess implements StorageAccess {
    public final List<Slot> slots;

    public SlotStorageAccess(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public int size() {
        return slots.size();
    }

    @Override
    public ItemStack get(int index) {
        return slots.get(index).getItem();
    }

    @Override
    public boolean canTake(int index, Player player) {
        return slots.get(index).mayPickup(player);
    }

    @Override
    public ItemStack take(int index, int amount, Player player) {
        Slot slot = slots.get(index);
        if (amount <= 0 || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        // Slot.safeTake is deliberately avoided so that slot side effects (Slot.onTake) aren't fired.
        return slot.remove(amount);
    }

    @Override
    public void insert(int index, ItemStack stack) {
        // Slot.safeInsert enforces mayPlace and the slot's max stack size, shrinks the stack, and persists the change.
        slots.get(index).safeInsert(stack);
    }

    @Override
    public int maxStackSize(int index, ItemStack stack) {
        return slots.get(index).getMaxStackSize(stack);
    }
}
