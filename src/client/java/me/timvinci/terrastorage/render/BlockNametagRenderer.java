package me.timvinci.terrastorage.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer registered to it will be processed by the WorldRenderer, which will in turn
 * give the NametagRenderer a chance to render a nametag for it.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<RandomizableContainerBlockEntity, BlockEntityRenderState> {

    public BlockNametagRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    }
}