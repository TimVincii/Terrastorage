package me.timvinci.terrastorage.render;

import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.mixin.client.BlockEntityRenderersMixin;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Set;

/**
 * Manages the registering of block nametag renderers to children of the RandomizableContainerBlockEntity class.
 */
public class BlockEntityRendererManager {

    /**
     * Registers a BloockNametagRenderer to every child of the RandomizableContainerBlockEntity class that doesn't have
     * its own BlockEntityRenderer and has an inventory with at least 27 slots.
     */
    @SuppressWarnings("unchecked")
    public static void registerLootableRenderers() {
        BuiltInRegistries.BLOCK_ENTITY_TYPE.forEach(blockEntityType -> {
            if (hasRenderer(blockEntityType)) {
                return;
            }

            BlockState state = getBlockState(blockEntityType);
            if (state == null) {
                return;
            }

            try {
                BlockEntity blockEntity = blockEntityType.create(BlockPos.ZERO, state);
                if (blockEntity instanceof RandomizableContainerBlockEntity randomizableContainerBlockEntity && randomizableContainerBlockEntity.getContainerSize() >= 27) {
                    BlockEntityType<RandomizableContainerBlockEntity> lootableType = (BlockEntityType<RandomizableContainerBlockEntity>) blockEntityType;
                    BlockEntityRenderers.register(lootableType, BlockNametagRenderer::new);
                    TerrastorageClient.CLIENT_LOGGER.info("Registered a block nametag renderer to '{}' and its block entity type.", state.getBlock().getName().getString());
                }
            }
            catch (Exception e) {
                TerrastorageClient.CLIENT_LOGGER.warn("Failed to instantiate a block entity instance for the '{}' block, skipping block nametag renderer registration check, you can safely ignore this.", state.getBlock().getName().getString());
            }
        });
    }

    /**
     * Gets the block state of the first block from the block entity type's supported blocks set.
     * @param blockEntityType The block entity type.
     * @return The block state of the first block, or null if the blocks set is empty.
     */
    private static BlockState getBlockState(BlockEntityType<?> blockEntityType) {
        Set<Block> blocks = ((BlockEntityTypeAccessor) blockEntityType).getBlocks();
        return blocks.stream().findFirst().map(Block::defaultBlockState).orElse(null);
    }

    /**
     * Checks if a block entity type has a dedicated BlockEntityRenderer class.
     * @param blockEntityType The block entity type.
     * @return True if it has, false otherwise.
     */
    private static boolean hasRenderer(BlockEntityType<?> blockEntityType) {
        return BlockEntityRenderersMixin.getFactories().containsKey(blockEntityType);
    }
}
