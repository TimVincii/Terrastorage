package me.timvinci.terrastorage.api;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * A compact class containing core Item Favoriting utility methods.
 */
public class ItemFavoritingUtils {
    public static ComponentType<Boolean> FAVORITE;

    public static void initializeComponentType() {
        FAVORITE = Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of("terrastorage", "favorite_item"),
                ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
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

    public static void unFavorite(MergedComponentMap componentMap) {
        componentMap.remove(FAVORITE);
    }
}
