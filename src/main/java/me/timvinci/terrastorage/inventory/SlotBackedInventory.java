package me.timvinci.terrastorage.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;

/**
 * A slot backed inventory, used for creating a working Inventory object from "broken" screen handlers.
 */
public class SlotBackedInventory implements Inventory {
    private final List<Slot> slots;

    public SlotBackedInventory(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public int size() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : slots) {
            if (slot.hasStack()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slots.get(slot).getStack();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = slots.get(slot).getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return stack.split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = slots.get(slot).getStack();
        slots.get(slot).setStack(ItemStack.EMPTY);
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.slots.get(slot).setStack(stack);
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size(); i++) {
            setStack(i, ItemStack.EMPTY);
        }
    }
}
