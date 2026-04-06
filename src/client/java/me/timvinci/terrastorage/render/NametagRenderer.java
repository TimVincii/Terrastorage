package me.timvinci.terrastorage.render;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Renders nametags for block entities.
 */
public class NametagRenderer {
    private final Font textRenderer;

    public NametagRenderer(Font textRenderer) {
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
            Component customName,
            ClientLevel world,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraRenderState) {
        if (!inRenderingRange(cameraRenderState.pos, blockPos))
            return;

         Vec3 renderPos = Vec3.atCenterOf(blockPos);


         // Check if the block above the block entity isn't air
        if (!world.isEmptyBlock(blockPos.above())) {
             Vec3 direction = cameraRenderState.pos.subtract(renderPos).normalize();

             // Set the render pos towards the player.
             renderPos = renderPos.add(direction);
         }
        else
            renderPos = renderPos.add(0, 1, 0);


        int light = LevelRenderer.getLightColor(world, BlockPos.containing(renderPos));

        matrices.pushPose();
        matrices.translate(
                renderPos.x - blockPos.getX(),
                renderPos.y - blockPos.getY(),
                renderPos.z - blockPos.getZ()
        );

        matrices.mulPose(cameraRenderState.orientation);
        matrices.scale(0.025F, -0.025F, 0.025F);
        float x = (float)(-textRenderer.width(customName) / 2);

        queue.submitText(
                matrices,
                x,
                0,
                customName.getVisualOrderText(),
                true,
                Font.DisplayMode.NORMAL,
                light,
                0xFFFFFFFF,
                0,
                0xFF000000
        );

        matrices.popPose();
    }

    /**
     * Checks if a nametag should be rendered for a block entity.
     * @param entity The block entity as a RandomizableContainerBlockEntity.
     * @return True if the entity has a custom name and is targeted by the player, false otherwise.
     */
    public boolean hasLabel(RandomizableContainerBlockEntity entity, HitResult crosshairTarget) {
        if (!entity.hasCustomName()) {
            return false;
        }

        if (crosshairTarget instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(entity.getBlockPos());
        }

        return false;
    }

    /**
     * Checks if the provided block position is in rendering range from the camera.
     * @param cameraPos The camera position.
     * @param blockPos The block position.
     * @return True if the block position is in range, false otherise.
     */
    private boolean inRenderingRange(Vec3 cameraPos, BlockPos blockPos) {
        return cameraPos.distanceToSqr(Vec3.atCenterOf(blockPos)) <= 4096.0;
    }
}
