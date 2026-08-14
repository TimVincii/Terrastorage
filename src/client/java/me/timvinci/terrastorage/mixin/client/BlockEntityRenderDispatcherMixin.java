package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.render.BlockNametagRenderer;
import me.timvinci.terrastorage.render.NametagRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.item.ItemModelResolver;
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
 * A mixin of the BlockEntityRenderDispatcher class, adds support for nametag rendering of block entities that have
 * a registered block entity renderer.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Unique
    private static NametagRenderer nametagRenderer;

    /**
     * Initiates the nametag renderer.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Font textRenderer, Supplier<EntityModelSet> entityModelsGetter, BlockRenderDispatcher blockRenderManager, ItemModelResolver itemModelManager, ItemRenderer itemRenderer, EntityRenderDispatcher entityRenderDispatcher, CallbackInfo ci) {
        BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher) (Object) this;
        nametagRenderer = new NametagRenderer(dispatcher, textRenderer);
    }


    /**
     * Adds nametag rendering after the block entity rendering has finished.
     */
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;setupAndRender(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
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
}
