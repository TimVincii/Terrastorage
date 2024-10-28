package me.timvinci.terrastorage.item;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Compact record used for identifying item stacks by both their item and nbt data.
 * @param item The item of the stack.
 * @param components The component data of the stack.
 */
public record StackIdentifier(Item item, @Nullable ComponentMap components) {

    public StackIdentifier(ItemStack stack) {
        this(stack.getItem(), new ComponentMapImpl(stack.getComponents()));
    }
}
