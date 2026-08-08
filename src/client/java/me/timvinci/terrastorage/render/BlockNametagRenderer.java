package me.timvinci.terrastorage.render;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer registered to it, will then be passed to the BlockEntityRenderDispatcher for
 * rendering, which will in turn render the nametag for the block entity.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<RandomizableContainerBlockEntity> {

    public BlockNametagRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(RandomizableContainerBlockEntity entity, float tickProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Vec3 cameraPos) {
    }
}