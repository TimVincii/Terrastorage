package me.timvinci.terrastorage.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.timvinci.terrastorage.render.BlockNametagRenderer;
import me.timvinci.terrastorage.render.NametagRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

/**
 * A mixin of the BlockEntityRenderDispatcher class, adds support for nametag rendering of block entities.
 * Block entities only reach the rendering pipeline if getRenderer returns a renderer for them, so storages that
 * vanilla has no renderer for are given the empty BlockNametagRenderer, which is what gives the NametagRenderer a
 * chance to render a nametag for them.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Unique
    private static NametagRenderer nametagRenderer;

    /**
     * Initiates the nametag renderer.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Font textRenderer, EntityModelSet entityModelLoader, Supplier<BlockRenderDispatcher> blockRenderManager, Supplier<ItemRenderer> itemRenderer, Supplier<EntityRenderDispatcher> entityRenderDispatcher, CallbackInfo ci) {
        BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher) (Object) this;
        nametagRenderer = new NametagRenderer(dispatcher, textRenderer);
    }

    /**
     * Adds nametag rendering after the block entity rendering has finished.
     */
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Runnable;)V",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private <E extends BlockEntity> void afterRender(
            E blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci,
            BlockEntityRenderer<E> blockEntityRenderer) {
        if (blockEntity instanceof RandomizableContainerBlockEntity lootableContainerBlockEntity && nametagRenderer.hasLabel(lootableContainerBlockEntity)) {

            if (blockEntityRenderer instanceof BlockNametagRenderer) {
                nametagRenderer.renderBlockNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers);
            } else {
                Level world = blockEntity.getLevel();
                int i = world != null ? LevelRenderer.getLightColor(world, blockEntity.getBlockPos()) : 15728880;
                nametagRenderer.renderNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers, i);
            }
        }
    }


    /**
     * Provides the nametag renderer for renameable storages that vanilla has no renderer for.
     * The check is performed on the block entity itself rather than on its type, since the size of a storage can
     * differ between block entities of the same type, for example when a mod's storage can be upgraded.
     */
    @ModifyReturnValue(method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;", at = @At("RETURN"))
    private BlockEntityRenderer<?> provideNametagRenderer(BlockEntityRenderer<?> original, BlockEntity blockEntity) {
        if (original == null && blockEntity instanceof RandomizableContainerBlockEntity container && container.getContainerSize() >= 27) {
            return BlockNametagRenderer.INSTANCE;
        }

        return original;
    }
}
