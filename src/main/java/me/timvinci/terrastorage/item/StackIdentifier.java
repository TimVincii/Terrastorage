package me.timvinci.terrastorage.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Compact record used for identifying item stacks by both their item and nbt data.
 * @param item The item of the stack.
 * @param nbt The nbt data of the stack.
 */
public record StackIdentifier(Item item, @Nullable NbtCompound nbt) {

    public StackIdentifier(ItemStack stack) {
        this(stack.getItem(), stack.hasNbt() ? stack.getNbt().copy() : null);
    }
}
