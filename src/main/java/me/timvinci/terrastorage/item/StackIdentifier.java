package me.timvinci.terrastorage.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Compact record used for identifying item stacks by both their item and nbt data.
 * @param item The item of the stack.
 * @param nbt The nbt data of the stack.
 */
public record StackIdentifier(Item item, @Nullable CompoundTag nbt) {

    public StackIdentifier(ItemStack stack) {
        this(stack.getItem(), stack.hasTag() ? stack.getTag().copy() : null);
    }
}
