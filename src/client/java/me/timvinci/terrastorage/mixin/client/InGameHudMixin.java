package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.util.BorderVisibility;
import me.timvinci.terrastorage.util.Reference;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    private final Identifier favoriteBorder = new Identifier(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");
    @Shadow
    private MinecraftClient client;

    /**
     * Draws the favorite border on hotbar slots that hold favorite item stacks.
     */
    @Inject(method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
                    shift = At.Shift.BEFORE))
    private void onRenderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (!ItemFavoritingUtils.isFavorite(stack)) {
            return;
        }

        BorderVisibility borderVisibility = ClientConfigManager.getInstance().getConfig().getBorderVisibility();

        if (borderVisibility == BorderVisibility.NEVER || borderVisibility == BorderVisibility.NON_HOTBAR ||
            borderVisibility == BorderVisibility.ON_PRESS_NON_HOTBAR) {
            return;
        }

        // If border visibility is set to ON_PRESS, only render if the key is pressed
        if (borderVisibility == BorderVisibility.ON_PRESS &&
            !InputUtil.isKeyPressed(client.getWindow().getHandle(),
                        KeyBindingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getCode())) {
            return;
        }

        context.drawTexture(favoriteBorder, x, y, 0, 0, 16, 16, 16, 16);
    }
}
