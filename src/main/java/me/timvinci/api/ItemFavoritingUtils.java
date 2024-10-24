package me.timvinci.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * A compact class containing core Item Favoriting utility methods.
 */
public class ItemFavoritingUtils {
    public static String KEY = "terrastorage_favorite";

    public static void setFavorite(ItemStack stack, boolean value) {
        if (value) {
            NbtCompound stackNbt = stack.getOrCreateNbt();
            stackNbt.putBoolean(KEY, true);
        }
        else if (stack.hasNbt()) {
            NbtCompound stackNbt = stack.getNbt();
            stackNbt.remove(KEY);
            if (stackNbt.isEmpty()) {
                stack.setNbt(null);
            }
        }
    }

    public static boolean isFavorite(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains(KEY);
    }

    public static void unFavorite(NbtCompound compound) {
        compound.remove(KEY);
    }
}
