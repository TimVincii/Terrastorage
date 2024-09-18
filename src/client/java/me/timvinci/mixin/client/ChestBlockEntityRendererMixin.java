package me.timvinci.mixin.client;

import me.timvinci.render.NametagRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the ChestBlockEntityRenderer class, adds nametag support to chest block entities.
 * @param <T> The block entity type.
 */
@Mixin(ChestBlockEntityRenderer.class)
public class ChestBlockEntityRendererMixin<T extends BlockEntity & LidOpenable> {
    // Add a nametag renderer instance.
    @Unique
    private NametagRenderer nametagRenderer;

    /**
     * Instantiates the nametag renderer once the chest block entity renderer is instantiated.
     * @param ctx The block entity renderer factory context.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(BlockEntityRendererFactory.Context ctx, CallbackInfo ci) {
        nametagRenderer = new NametagRenderer(ctx.getRenderDispatcher(), ctx.getTextRenderer());
    }

    /**
     * Adds nametag rendering.
     */
    @Inject(method = "render", at = @At("TAIL"))
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (entity instanceof LockableContainerBlockEntity storageEntity) {
            if (nametagRenderer.hasLabel(storageEntity)) {
                nametagRenderer.renderNametag(entity, storageEntity.getCustomName(), matrices, vertexConsumers, light);
            }
        }
    }
}