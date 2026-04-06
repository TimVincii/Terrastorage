package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * A mixin of the ItemStack class, used for adding item favoriting support.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow
    private int count;

    /**
     * Adds the "Marked as favorite" tooltip if the item stack is favorite.
     */
    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    public void onGetTooltipReturn(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        if (!ItemFavoritingUtils.isFavorite(((ItemStack) (Object) this))) {
            return;
        }

        List<Component> tooltips = cir.getReturnValue();
        tooltips.add(Component.translatable("terrastorage.item.tooltip.favorite").withStyle(ChatFormatting.GOLD));
        cir.setReturnValue(tooltips);
    }
}
