package me.timvinci.terrastorage.render;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
    private final Font font;

    public NametagRenderer(Font font) {
        this.font = font;
    }

    /**
     * Renders the nametag for a block entity.
     * @param blockPos Block position of the block entity.
     * @param customName The custom name to be rendered.
     * @param level The ClientLevel instance the block resides in.
     * @param poseStack The pose stack used for positioning transformations.
     * @param submitNodeStorage The node storage to which text rendering is submitted to.
     * @param cameraRenderState A CameraRenderState instance.
     */
    public void renderNametag(
            BlockPos blockPos,
            Component customName,
            ClientLevel level,
            PoseStack poseStack,
            SubmitNodeStorage submitNodeStorage,
            CameraRenderState cameraRenderState) {
        if (!inRenderingRange(cameraRenderState.pos, blockPos))
            return;

        Vec3 renderPos = Vec3.atCenterOf(blockPos);


        // Check if the block above the block entity isn't air
        if (!level.isEmptyBlock(blockPos.above())) {
            Vec3 direction = cameraRenderState.pos.subtract(renderPos).normalize();

            // Set the render pos towards the player.
            renderPos = renderPos.add(direction);
        }
        else
            renderPos = renderPos.add(0, 1, 0);


        int light = LevelRenderer.getLightCoords(level, BlockPos.containing(renderPos));

        poseStack.pushPose();
        poseStack.translate(
                renderPos.x - blockPos.getX(),
                renderPos.y - blockPos.getY(),
                renderPos.z - blockPos.getZ()
        );

        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.scale(0.025F, -0.025F, 0.025F);
        float x = (float)(-font.width(customName) / 2);

        submitNodeStorage.submitText(
                poseStack,
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

        poseStack.popPose();
    }

    /**
     * Checks if a nametag should be rendered for a block entity.
     * @param entity The block entity as a LootableContainerBlockEntity.
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
