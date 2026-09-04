package me.timvinci.terrastorage.render;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer returned for it will be processed by the BlockEntityRenderDispatcher, which
 * will in turn give the NametagRenderer a chance to render a nametag for it.
 * A single instance is shared.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<RandomizableContainerBlockEntity> {
    public static final BlockNametagRenderer INSTANCE = new BlockNametagRenderer();

    private BlockNametagRenderer() {}

    @Override
    public void render(RandomizableContainerBlockEntity entity, float tickProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Vec3 cameraPos) {}
}
