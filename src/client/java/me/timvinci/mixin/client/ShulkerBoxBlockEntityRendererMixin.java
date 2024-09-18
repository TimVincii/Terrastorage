package me.timvinci.mixin.client;

import me.timvinci.render.NametagRenderer;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the ShulkerBoxBlockEntityRenderer class, adds nametag support to shulker box block entities.
 */
@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {
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
    public void render(ShulkerBoxBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (nametagRenderer.hasLabel(entity)) {
            nametagRenderer.renderNametag(entity, entity.getCustomName(), matrices, vertexConsumers, light);
        }
    }
}
