package me.timvinci.terrastorage.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * A compact class containing core Item Favoriting utility methods.
 */
public class ItemFavoritingUtils {
    public static String KEY = "terrastorage_favorite";

    public static void setFavorite(ItemStack stack, boolean value) {
        if (value) {
            CompoundTag stackNbt = stack.getOrCreateTag();
            stackNbt.putBoolean(KEY, true);
        }
        else if (stack.hasTag()) {
            CompoundTag stackNbt = stack.getTag();
            stackNbt.remove(KEY);
            if (stackNbt.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    public static boolean isFavorite(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(KEY);
    }

    public static void unFavorite(CompoundTag compound) {
        compound.remove(KEY);
    }
}
