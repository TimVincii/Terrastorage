package me.timvinci.terrastorage.render;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer returned for it will be processed by the LevelRenderer, which will in turn
 * give the NametagRenderer a chance to render a nametag for it.
 * A single instance is shared, as the BlockEntityRenderDispatcherMixin hands it out from chunk building threads, and
 * shouldRenderOffScreen is deliberately left at its default of false, since block entities whose renderer renders off
 * screen are excluded from the renderable block entities of a section.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<RandomizableContainerBlockEntity, BlockEntityRenderState> {
    public static final BlockNametagRenderer INSTANCE = new BlockNametagRenderer();

    private BlockNametagRenderer() {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {}
}
