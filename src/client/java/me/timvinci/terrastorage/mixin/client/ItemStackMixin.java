package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * Adds the "Marked as favorite" tooltip if the item stack is favorite.
     */
    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    public void onGetTooltipReturn(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (!ItemFavoritingUtils.isFavorite(((ItemStack) (Object) this))) {
            return;
        }

        List<Text> tooltips = cir.getReturnValue();
        tooltips.add(Text.translatable("terrastorage.item.tooltip.favorite").formatted(Formatting.GOLD));
        cir.setReturnValue(tooltips);
    }
}
