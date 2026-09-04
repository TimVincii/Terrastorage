package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.render.BlockNametagRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mixin of the BlockEntityRenderDispatcher class, provides a nametag renderer for storages that don't have a
 * dedicated block entity renderer.
 * Block entities only reach the rendering pipeline if BlockEntityRenderDispatcher.getRenderer returns a renderer for
 * them, so returning the empty BlockNametagRenderer is what gives the NametagRenderer a chance to render a nametag for
 * them.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    /**
     * Provides the nametag renderer for renameable storages that vanilla has no renderer for.
     * The check is performed on the block entity itself rather than on its type, since the size of a storage can
     * differ between block entities of the same type, for example when a mod's storage can be upgraded.
     */
    @Inject(method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;", at = @At("RETURN"), cancellable = true)
    private void provideNametagRenderer(BlockEntity blockEntity, CallbackInfoReturnable<BlockEntityRenderer<?, ?>> cir) {
        if (cir.getReturnValue() != null) {
            return;
        }

        if (blockEntity instanceof RandomizableContainerBlockEntity container && container.getContainerSize() >= 27) {
            cir.setReturnValue(BlockNametagRenderer.INSTANCE);
        }
    }
}
