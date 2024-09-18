package me.timvinci.render;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/**
 * A custom barrel block entity renderer, adds nametag support to barrel blocks.
 */
public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity> {
    // Define a nametag renderer instance.
    private final NametagRenderer nametagRenderer;

    /**
     * Instantiates the nametag renderer once the barrel block entity renderer is instantiated.
     * @param ctx The block entity renderer factory context.
     */
    public BarrelBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.nametagRenderer = new NametagRenderer(ctx.getRenderDispatcher(), ctx.getTextRenderer());
    }

    /**
     * Adds nametag rendering.
     */
    @Override
    public void render(BarrelBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (nametagRenderer.hasLabel(entity)) {
            nametagRenderer.renderBarrelNametag(entity, entity.getCustomName(), matrices, vertexConsumers);
        }
    }
}
