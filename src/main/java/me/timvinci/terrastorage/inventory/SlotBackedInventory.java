package me.timvinci.terrastorage.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * A slot backed inventory, used for creating a working Inventory object from "broken" screen handlers.
 */
public class SlotBackedInventory implements Container {
    private final List<Slot> slots;

    public SlotBackedInventory(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public int getContainerSize() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : slots) {
            if (slot.hasItem()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slots.get(slot).getItem();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = slots.get(slot).getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return stack.split(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = slots.get(slot).getItem();
        slots.get(slot).setByPlayer(ItemStack.EMPTY);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.slots.get(slot).setByPlayer(stack);
    }

    @Override
    public void setChanged() {
        for (Slot slot : slots) {
            slot.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }
}
