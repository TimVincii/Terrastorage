package me.timvinci.terrastorage.render;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders nametags for block entities.
 */
public class NametagRenderer {
    // Define a render dispatcher and a text renderer.
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font textRenderer;

    public NametagRenderer(BlockEntityRenderDispatcher dispatcher, Font textRenderer) {
        this.dispatcher = dispatcher;
        this.textRenderer = textRenderer;
    }

    /**
     * Calculates the position for rendering the nametag and delegates the actual rendering to a private method.
     * @param entity The block entity for which the nametag is rendered.
     * @param customName The custom name to display on the nametag.
     * @param matrices The matrix stack used for positioning transformations (passed to the rendering method).
     * @param vertexConsumers The vertex consumer provider for rendering (passed to the rendering method).
     * @param light The light level for the rendering (passed to the rendering method).
     */
    public void renderNametag(BlockEntity entity, Component customName, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        BlockPos entityPos = entity.getBlockPos();
        Vec3 renderPos = Vec3.atCenterOf(entityPos);

        // Check if the block above the block entity isn't air.
        if (!dispatcher.level.isEmptyBlock(entityPos.above())) {
            Vec3 direction = dispatcher.camera.getPosition().subtract(renderPos).normalize();
            // Set the render pos to a position towards the player.
            renderPos = renderPos.add(direction);
        } else { // Set the render pos to a single block above the block entity.
            renderPos = renderPos.subtract(0, -1, 0);
        }

        this.renderNametag(entity, customName, matrices, vertexConsumers, renderPos, light);
    }

    /**
     * Calculates the position for rendering the nametag, as well as the light level in that position, and delegates the
     * actual rendering to a private method.
     * @param entity The block entity for which the nametag is rendered.
     * @param customName The custom name to display on the nametag.
     * @param matrices The matrix stack used for positioning transformations.
     * @param vertexConsumers The vertex consumer provider for rendering.
     */
    public void renderBlockNametag(BlockEntity entity, Component customName, PoseStack matrices, MultiBufferSource vertexConsumers) {
        BlockPos entityPos = entity.getBlockPos();
        Vec3 renderPos = Vec3.atCenterOf(entityPos);
        int light;

        // Check if the block above the block entity isn't air.
        if (!dispatcher.level.isEmptyBlock(entityPos.above())) {
            Vec3 direction = dispatcher.camera.getPosition().subtract(renderPos).normalize();
            // Set the render pos to a position towards the player.
            renderPos = renderPos.add(direction);

            BlockPos lightPos = new BlockPos(
                    (int)Math.floor(renderPos.x),
                    (int)Math.floor(renderPos.y),
                    (int)Math.floor(renderPos.z)
            );

            light = LevelRenderer.getLightColor(dispatcher.level, lightPos);

        } else { // Set the render pos to a single block above the block entity.
            renderPos = renderPos.subtract(0, -1, 0);
            light = LevelRenderer.getLightColor(dispatcher.level, entityPos.above());
        }

        this.renderNametag(entity, customName, matrices, vertexConsumers, renderPos, light);
    }

    /**
     * Renders the nametag.
     * @param entity The block entity for which the nametag is rendered.
     * @param customName The custom name to display on the nametag.
     * @param matrices The matrix stack used for positioning transformations.
     * @param vertexConsumers The vertex consumer provider for rendering.
     * @param renderPos The position of the nametag.
     * @param light The light level for the rendering.
     */
    private void renderNametag(BlockEntity entity, Component customName, PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 renderPos, int light) {
        double distanceToCamera = getSquaredDistanceToCamera(entity);
        // Warning - yapping session:
        // The check makes no sense, the only way a nametag will be rendered is if the player is looking at the block
        // entity, that means the player is at max 4 blocks away from the block entity, so why the hell is checking
        // if the distance from the camera is under 64 blocks necessary?!
        // Then again, this exact check was made in the EntityRenderer class when rendering a nametag for an entity...
        // hopefully Mojang had a valid reason to do that.
        if (distanceToCamera <= 4096.0) {
            matrices.pushPose();

            matrices.translate(
                    renderPos.x - entity.getBlockPos().getX(),
                    renderPos.y - entity.getBlockPos().getY(),
                    renderPos.z - entity.getBlockPos().getZ()
            );

            matrices.mulPose(getRotation());
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrices.last().pose();
            float xPos = (float)(-textRenderer.width(customName) / 2);

            textRenderer.drawInBatch8xOutline(customName.getVisualOrderText(), xPos, 0, 0xFFFFFFFF, 0xFF000000, matrix4f, vertexConsumers, light);

            matrices.popPose();
        }
    }

    /**
     * Checks if a nametag should be rendered for a block entity.
     * @param entity The block entity as a LootableContainerBlockEntity.
     * @return True if the entity has a custom name and is targeted by the player, false otherwise.
     */
    public boolean hasLabel(RandomizableContainerBlockEntity entity) {
        if (!entity.hasCustomName()) {
            return false;
        }

        if (dispatcher.cameraHitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(entity.getBlockPos());
        }

        return false;
    }

    private double getSquaredDistanceToCamera(BlockEntity entity) {
        return dispatcher.camera.getPosition().distanceToSqr(entity.getBlockPos().getX(), entity.getBlockPos().getY(), entity.getBlockPos().getZ());
    }

    private Quaternionf getRotation() {
        return dispatcher.camera.rotation();
    }
}
