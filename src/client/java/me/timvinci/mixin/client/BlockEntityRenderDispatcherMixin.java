package me.timvinci.mixin.client;

import me.timvinci.render.BlockNametagRenderer;
import me.timvinci.render.NametagRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void onInit(TextRenderer textRenderer, EntityModelLoader entityModelLoader, Supplier<BlockRenderManager> blockRenderManager, Supplier<ItemRenderer> itemRenderer, Supplier<EntityRenderDispatcher> entityRenderDispatcher, CallbackInfo ci) {
        BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher) (Object) this;
        nametagRenderer = new NametagRenderer(dispatcher, textRenderer);
    }

    /**
     * Adds nametag rendering after the block entity rendering has finished.
     */
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        World world = blockEntity.getWorld();
        int i;
        if (world != null) {
            i = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos());
        } else {
            i = 15728880;
        }

        renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, i, OverlayTexture.DEFAULT_UV);
        if (blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity && nametagRenderer.hasLabel(lootableContainerBlockEntity)) {
            if (renderer instanceof BlockNametagRenderer) {
                nametagRenderer.renderBlockNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers);
            }
            else {
                nametagRenderer.renderNametag(blockEntity, lootableContainerBlockEntity.getCustomName(), matrices, vertexConsumers, i);
            }
        }
        ci.cancel();
    }
}