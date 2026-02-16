package me.timvinci.terrastorage.render;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

/**
 * An empty custom block entity renderer.
 * Any block entity that has this renderer registered to it will be processed by the WorldRenderer, which will in turn
 * give the NametagRenderer a chance to render a nametag for it.
 */
public class BlockNametagRenderer implements BlockEntityRenderer<LootableContainerBlockEntity, BlockEntityRenderState> {

    public BlockNametagRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void render(BlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {}
}