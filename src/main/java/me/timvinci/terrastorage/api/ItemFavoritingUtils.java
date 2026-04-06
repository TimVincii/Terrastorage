package me.timvinci.terrastorage.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/**
 * A compact class containing core Item Favoriting utility methods.
 */
public class ItemFavoritingUtils {
    public static DataComponentType<Boolean> FAVORITE;

    public static void initializeComponentType() {
        FAVORITE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("terrastorage", "favorite_item"),
                DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
        );
    }

    public static boolean isFavorite(ItemStack stack) {
        return stack.getOrDefault(FAVORITE, false);
    }

    public static void setFavorite(ItemStack stack, boolean value) {
        if (value) {
            stack.set(FAVORITE, true);
        }
        else {
            stack.remove(FAVORITE);
        }
    }

    public static void unFavorite(PatchedDataComponentMap componentMap) {
        componentMap.remove(FAVORITE);
    }
}
