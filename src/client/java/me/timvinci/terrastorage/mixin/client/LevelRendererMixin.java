package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.render.NametagRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

/**
 * A mixin of the LevelRenderer class, adds nametag rendering for block entities with custom names.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Unique
    private static NametagRenderer nametagRenderer;
    @Final
    @Shadow
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Shadow
    @Nullable
    private ClientLevel level;

    /**
     * Initiates the nametag renderer.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Minecraft minecraft,
                        EntityRenderDispatcher entityRenderDispatcher,
                        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
                        RenderBuffers renderBuffers,
                        GameRenderState gameRenderState,
                        FeatureRenderDispatcher featureRenderDispatcher,
                        CallbackInfo ci) {
        nametagRenderer = new NametagRenderer(Minecraft.getInstance().font);
    }

    /**
     * Adds nametag rendering after block entity rendering takes place.
     */
    @Inject(
            method = "submitBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void afterRenderBlockEntity(
            PoseStack poseStack,
            LevelRenderState levelRenderState,
            SubmitNodeStorage submitNodeStorage,
            CallbackInfo ci,
            Vec3 vec3d,
            double d,
            double e,
            double f,
            Iterator<BlockEntityRenderState> iterator,
            BlockEntityRenderState blockEntityRenderState
    ) {
        if (level == null)
            return;

        BlockPos blockPos = blockEntityRenderState.blockPos;

        // Don't like having to do this after the rendering pipeline update, but it is what it is
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        // Return if the NametagRenderer has nothing to do here
        if (!(blockEntity instanceof RandomizableContainerBlockEntity container)
                || !nametagRenderer.hasLabel(container, Minecraft.getInstance().hitResult))
            return;

        nametagRenderer.renderNametag(blockPos, container.getCustomName(), level, poseStack, submitNodeStorage, levelRenderState.cameraRenderState);
    }
}
