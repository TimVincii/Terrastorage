package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the MultiPlayerGameMode class, used for adding item favoriting support.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    /**
     * Injects into {@link MultiPlayerGameMode#dropItem} at HEAD.
     * Stops the player from dropping favorite items.
     */
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void onDropItemHead(LocalPlayer player, boolean all, CallbackInfo ci) {
        if (ItemFavoritingUtils.isFavorite(player.getMainHandItem())) {
            ci.cancel();
        }
    }
}
