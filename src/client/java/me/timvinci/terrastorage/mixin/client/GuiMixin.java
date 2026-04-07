package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.util.BorderVisibility;
import me.timvinci.terrastorage.util.Reference;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the Gui class, used for adding item favoriting support.
 */
@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private final Identifier favoriteBorder = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");
    @Shadow
    private Minecraft minecraft;

    /**
     * Draws the favorite border on hotbar slots that hold favorite item stacks.
     */
    @Inject(method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
                    shift = At.Shift.BEFORE))
    private void onRenderHotbarItem(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
        if (!ItemFavoritingUtils.isFavorite(itemStack)) {
            return;
        }

        BorderVisibility borderVisibility = ClientConfigManager.getInstance().getConfig().getBorderVisibility();

        if (borderVisibility == BorderVisibility.NEVER || borderVisibility == BorderVisibility.NON_HOTBAR ||
            borderVisibility == BorderVisibility.ON_PRESS_NON_HOTBAR) {
            return;
        }

        // If border visibility is set to ON_PRESS, only render if the key is pressed
        if (borderVisibility == BorderVisibility.ON_PRESS &&
            !InputConstants.isKeyDown(minecraft.getWindow(),
                        KeyMappingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getValue())) {
            return;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, favoriteBorder, x, y, 0, 0, 16, 16, 16, 16);
    }
}
