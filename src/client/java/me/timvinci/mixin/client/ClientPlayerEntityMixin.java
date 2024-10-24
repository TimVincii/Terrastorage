package me.timvinci.mixin.client;

import com.mojang.authlib.GameProfile;
import me.timvinci.api.ItemFavoritingUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mixin of the ClientPlayerEntity class, used for adding item favoriting support.
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * Stops the player from dropping favorite items.
     */
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropSelectedSlot(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (ItemFavoritingUtils.isFavorite(this.getMainHandStack())) {
            cir.cancel();
        }
    }
}
