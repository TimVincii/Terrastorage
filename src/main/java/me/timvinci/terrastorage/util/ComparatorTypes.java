package me.timvinci.terrastorage.util;

import me.timvinci.terrastorage.item.ItemGroupCache;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/**
 * Holds the comparators used for sorting items in storages and player inventories.
 */
public class ComparatorTypes {

    public static final Comparator<ItemStack> BY_ID =
            Comparator.comparingInt(stack -> Item.getId(stack.getItem()));

    public static  final Comparator<ItemStack> BY_NAME =
            Comparator.comparing((ItemStack itemStack) -> itemStack.getHoverName().getString())
                    .thenComparing(BY_ID);

    public static final Comparator<ItemStack> BY_GROUP =
            Comparator.<ItemStack, String>comparing(
                    itemStack -> {
                        CreativeModeTab group = ItemGroupCache.getGroup(itemStack.getItem());
                        return group != null ? group.getDisplayName().getString() : "";
                    }
            ).thenComparing(BY_NAME);

    public static final Comparator<ItemStack> BY_COUNT =
            Comparator.comparingInt(ItemStack::getCount).reversed()
                    .thenComparing(BY_NAME);

    public static final Comparator<ItemStack> BY_RARITY =
            Comparator.comparingInt((ItemStack itemStack) -> itemStack.getRarity().ordinal()).reversed()
                    .thenComparing(BY_ID);
}
