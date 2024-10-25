package me.timvinci.terrastorage.mixin.client;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockEntityRendererFactories.class)
public interface BlockEntityRendererFactoriesMixin {

    @Accessor("FACTORIES")
    static Map<BlockEntityType<?>, BlockEntityRendererFactory<?>> getFactories() {
        throw new AssertionError();
    }
}
