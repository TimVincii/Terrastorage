package me.timvinci.mixin.client;

import me.timvinci.api.ItemFavoritingUtils;
import me.timvinci.util.Reference;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the InGameHud class, used for adding item favoriting support.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private final Identifier favoriteBorder = Identifier.of(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");

    /**
     * Draws the favorite border on hotbar slots that hold favorite item stacks.
     */
    @Inject(method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
                    shift = At.Shift.BEFORE))
    private void onRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (!ItemFavoritingUtils.isFavorite(stack)) {
            return;
        }

        context.drawTexture(favoriteBorder, x, y, 0, 0, 16, 16, 16, 16);
    }
}
