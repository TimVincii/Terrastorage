package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.render.BlockNametagRenderer;
import me.timvinci.terrastorage.render.NametagRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
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
    private void onInit(TextRenderer textRenderer,
                        Supplier<LoadedEntityModels> entityModelsGetter,
                        BlockRenderManager blockRenderManager,
                        ItemModelManager itemModelManager,
                        ItemRenderer itemRenderer,
                        EntityRenderDispatcher entityRenderDispatcher,
                        CallbackInfo ci) {
        BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher) (Object) this;
        nametagRenderer = new NametagRenderer(dispatcher, textRenderer);
    }


    /**
     * Adds nametag rendering after the block entity rendering has finished.
     */
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/Vec3d;)V",
                shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private <E extends BlockEntity> void afterRender(
            E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci,
            BlockEntityRenderer<E> blockEntityRenderer) {
        if (blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity && nametagRenderer.hasLabel(lootableContainerBlockEntity)) {
            if (blockEntityRenderer instanceof BlockNametagRenderer) {
                nametagRenderer.renderBlockNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers);
            } else {
                World world = blockEntity.getWorld();
                int i = world != null ? WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos()) : 15728880;
                nametagRenderer.renderNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers, i);
            }
        }
    }
}
