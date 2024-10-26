package me.timvinci.terrastorage.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * A mixin accessor for the BlockEntityType class.
 * Not using the net.fabricmc.fabric.mixin.lookup BlockEntityTypeAccessor as it causes issues wih Sinytra Connector.
 */
@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {

    @Accessor("blocks")
    Set<Block> blocks();
}
