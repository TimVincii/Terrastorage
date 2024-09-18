package me.timvinci.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders nametags for block entities.
 */
public class NametagRenderer {
    // Define a render dispatcher and a text renderer.
    private final BlockEntityRenderDispatcher dispatcher;
    private final TextRenderer textRenderer;

    public NametagRenderer(BlockEntityRenderDispatcher dispatcher, TextRenderer textRenderer) {
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
    public void renderNametag(BlockEntity entity, Text customName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        BlockPos entityPos = entity.getPos();
        Vec3d renderPos = Vec3d.ofCenter(entityPos);

        // Check if the block above the block entity isn't air.
        if (!dispatcher.world.isAir(entityPos.up())) {
            Vec3d direction = dispatcher.camera.getPos().subtract(renderPos).normalize();
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
    public void renderBarrelNametag(BlockEntity entity, Text customName, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        BlockPos entityPos = entity.getPos();
        Vec3d renderPos = Vec3d.ofCenter(entityPos);
        int light;

        // Check if the block above the block entity isn't air.
        if (!dispatcher.world.isAir(entityPos.up())) {
            Vec3d direction = dispatcher.camera.getPos().subtract(renderPos).normalize();
            // Set the render pos to a position towards the player.
            renderPos = renderPos.add(direction);

            BlockPos lightPos = new BlockPos(
                    (int)Math.floor(renderPos.x),
                    (int)Math.floor(renderPos.y),
                    (int)Math.floor(renderPos.z)
            );

            light = WorldRenderer.getLightmapCoordinates(dispatcher.world, lightPos);

        } else { // Set the render pos to a single block above the block entity.
            renderPos = renderPos.subtract(0, -1, 0);
            light = WorldRenderer.getLightmapCoordinates(dispatcher.world, entity.getCachedState(), entityPos.up());
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
    private void renderNametag(BlockEntity entity, Text customName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d renderPos, int light) {
        double distanceToCamera = getSquaredDistanceToCamera(entity);
        // Warning - yapping session:
        // The check makes no sense, the only way a nametag will be rendered is if the player is looking at the block
        // entity, that means the player is at max 4 blocks away from the block entity, so why the hell is checking
        // if the distance from the camera is under 64 blocks necessary?!
        // Then again, this exact check was made in the EntityRenderer class when rendering a nametag for an entity...
        // hopefully Mojang had a valid reason to do that.
        if (distanceToCamera <= 4096.0) {
            matrices.push();

            matrices.translate(
                    renderPos.x - entity.getPos().getX(),
                    renderPos.y - entity.getPos().getY(),
                    renderPos.z - entity.getPos().getZ()
            );

            matrices.multiply(getRotation());
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            float xPos = (float)(-textRenderer.getWidth(customName) / 2);

            textRenderer.drawWithOutline(customName.asOrderedText(), xPos, 0, -1, 0, matrix4f, vertexConsumers, light);

            matrices.pop();
        }
    }

    /**
     * Checks if a nametag should be rendered for a block entity.
     * @param entity The block entity as a LockableContainerBlockEntity.
     * @return True if the entity has a custom name and is targeted by the player, false otherwise.
     */
    public boolean hasLabel(LockableContainerBlockEntity entity) {
        if (!entity.hasCustomName()) {
            return false;
        }

        if (dispatcher.crosshairTarget instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(entity.getPos());
        }

        return false;
    }

    private double getSquaredDistanceToCamera(BlockEntity entity) {
        return dispatcher.camera.getPos().squaredDistanceTo(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ());
    }

    private Quaternionf getRotation() {
        return dispatcher.camera.getRotation();
    }
}
