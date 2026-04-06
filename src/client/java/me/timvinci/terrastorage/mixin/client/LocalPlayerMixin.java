package me.timvinci.terrastorage.mixin.client;

import com.mojang.authlib.GameProfile;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mixin of the LocalPlayer class, used for adding item favoriting support.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * Stops the player from dropping favorite items.
     */
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void onDropSelectedSlot(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (ItemFavoritingUtils.isFavorite(this.getMainHandItem())) {
            cir.cancel();
        }
    }
}
