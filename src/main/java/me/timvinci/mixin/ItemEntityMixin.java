package me.timvinci.mixin;

import me.timvinci.config.ConfigManager;
import me.timvinci.api.ItemFavoritingUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A mixin of the ItemEntity class, provides the ability to remove the favorite status of an item stack once it becomes
 * an item entity.
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    /**
     * Based on the keep favorites on drop option, removes the favorite status of an item stack before it is used by
     * the item entity.
     */
    @ModifyVariable(
            method = "setStack",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifySetStack(ItemStack stack) {
        if (ConfigManager.getInstance().getConfig().getKeepFavoritesOnDrop()) {
            return stack;
        }

        if (ItemFavoritingUtils.isFavorite(stack)) {
            ItemFavoritingUtils.setFavorite(stack, false);
        }
        return stack;
    }
}
