package me.timvinci.terrastorage.mixin.client;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * A mixin accessor for the BlockEntityRenderers class.
 */
@Mixin(BlockEntityRenderers.class)
public interface BlockEntityRenderersMixin {

    @Accessor("PROVIDERS")
    static Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> getFactories() {
        throw new AssertionError();
    }
}
