package me.timvinci.render;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/**
 * An empty custom barrel block entity renderer.
 * This renderer makes the barrel block entity get passed to the BlockEntityRenderDispatcher, which then renders the
 * nametag above it.
 * TODO - this is a temporary solution, and will be improved on.
 */
public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    public BarrelBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(BarrelBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {}
}