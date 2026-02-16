package me.timvinci.terrastorage.render;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Renders nametags for block entities.
 */
public class NametagRenderer {
    private final TextRenderer textRenderer;

    public NametagRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    /**
     * Renders the nametag for a block entity.
     * @param blockPos Block position of the block entity.
     * @param customName The custom name to be rendered.
     * @param world The ClientWorld instance the block resides in.
     * @param matrices The matrix stack used for positioning transformations.
     * @param queue The render command queue to which text rendering is submitted to.
     * @param cameraRenderState A CameraRenderState instance.
     */
    public void renderNametag(
            BlockPos blockPos,
            Text customName,
            ClientWorld world,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraRenderState) {
        if (!inRenderingRange(cameraRenderState.pos, blockPos))
            return;

         Vec3d renderPos = Vec3d.ofCenter(blockPos);


         // Check if the block above the block entity isn't air
        if (!world.isAir(blockPos.up())) {
             Vec3d direction = cameraRenderState.pos.subtract(renderPos).normalize();

             // Set the render pos towards the player.
             renderPos = renderPos.add(direction);
         }
        else
            renderPos = renderPos.add(0, 1, 0);


        int light = WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(renderPos));

        matrices.push();
        matrices.translate(
                renderPos.x - blockPos.getX(),
                renderPos.y - blockPos.getY(),
                renderPos.z - blockPos.getZ()
        );

        matrices.multiply(cameraRenderState.orientation);
        matrices.scale(0.025F, -0.025F, 0.025F);
        float x = (float)(-textRenderer.getWidth(customName) / 2);

        queue.submitText(
                matrices,
                x,
                0,
                customName.asOrderedText(),
                true,
                TextRenderer.TextLayerType.NORMAL,
                light,
                0xFFFFFFFF,
                0,
                0xFF000000
        );

        matrices.pop();
    }

    /**
     * Checks if a nametag should be rendered for a block entity.
     * @param entity The block entity as a LootableContainerBlockEntity.
     * @return True if the entity has a custom name and is targeted by the player, false otherwise.
     */
    public boolean hasLabel(LootableContainerBlockEntity entity, HitResult crosshairTarget) {
        if (!entity.hasCustomName()) {
            return false;
        }

        if (crosshairTarget instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(entity.getPos());
        }

        return false;
    }

    /**
     * Checks if the provided block position is in rendering range from the camera.
     * @param cameraPos The camera position.
     * @param blockPos The block position.
     * @return True if the block position is in range, false otherise.
     */
    private boolean inRenderingRange(Vec3d cameraPos, BlockPos blockPos) {
        return cameraPos.squaredDistanceTo(Vec3d.ofCenter(blockPos)) <= 4096.0;
    }
}
