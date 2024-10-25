package me.timvinci.terrastorage.render;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer registered to it, will then be passed to the BlockEntityRenderDispatcher for
 * rendering, which will in turn render the nametag for the block entity.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<LootableContainerBlockEntity> {

    public BlockNametagRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(LootableContainerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {}
}