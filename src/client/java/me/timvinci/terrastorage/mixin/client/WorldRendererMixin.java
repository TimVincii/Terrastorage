package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.render.NametagRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
 * A mixin of the WorldRenderer class, adds nametag rendering for block entities with custom names.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Unique
    private static NametagRenderer nametagRenderer;
    @Final
    @Shadow
    private BlockEntityRenderManager blockEntityRenderManager;
    @Shadow
    @Nullable
    private ClientWorld world;

    /**
     * Initiates the nametag renderer.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftClient client,
                        EntityRenderManager entityRenderManager,
                        BlockEntityRenderManager blockEntityRenderManager,
                        BufferBuilderStorage bufferBuilders,
                        WorldRenderState worldRenderState,
                        RenderDispatcher entityRenderDispatcher,
                        CallbackInfo ci) {
        nametagRenderer = new NametagRenderer(MinecraftClient.getInstance().textRenderer);
    }

    /**
     * Adds nametag rendering after block entity rendering takes place.
     */
    @Inject(
            method = "renderBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderManager;render(Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void afterRenderBlockEntity(
            MatrixStack matrices,
            WorldRenderState renderStates,
            OrderedRenderCommandQueueImpl queue,
            CallbackInfo ci,
            Vec3d vec3d,
            double d,
            double e,
            double f,
            Iterator<BlockEntityRenderState> iterator,
            BlockEntityRenderState blockEntityRenderState
    ) {
        if (world == null)
            return;

        BlockPos blockPos = blockEntityRenderState.pos;

        // Don't like having to do this after the rendering pipeline update, but it is what it is
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        // Return if the NametagRenderer has nothing to do here
        if (!(blockEntity instanceof LootableContainerBlockEntity container)
                || !nametagRenderer.hasLabel(container, MinecraftClient.getInstance().crosshairTarget))
            return;

        nametagRenderer.renderNametag(blockPos, container.getCustomName(), world, matrices, queue, renderStates.cameraRenderState);
    }
}
