package me.timvinci.terrastorage.render;

import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.mixin.client.BlockEntityRendererFactoriesMixin;
import me.timvinci.terrastorage.mixin.client.BlockEntityTypeAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * Manages the registering of block nametag renderers to children of the LootableContainerBlockEntity class.
 */
public class BlockEntityRendererManager {

    /**
     * Registers a BloockNametagRenderer to every child of the LootableContainerBlockEntity class that doesn't have
     * its own BlockEntityRenderer and has an inventory with at least 27 slots.
     */
    @SuppressWarnings("unchecked")
    public static void registerLootableRenderers() {
        Registries.BLOCK_ENTITY_TYPE.forEach(blockEntityType -> {
            if (hasRenderer(blockEntityType)) {
                return;
            }

            BlockState state = getBlockState(blockEntityType);
            if (state == null) {
                return;
            }

            try {
                BlockEntity blockEntity = blockEntityType.instantiate(BlockPos.ORIGIN, state);
                if (blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity && lootableContainerBlockEntity.size() >= 27) {
                    BlockEntityType<LootableContainerBlockEntity> lootableType = (BlockEntityType<LootableContainerBlockEntity>) blockEntityType;
                    BlockEntityRendererFactories.register(lootableType, BlockNametagRenderer::new);
                    TerrastorageClient.CLIENT_LOGGER.info("Registered a block nametag renderer to '{}' and its block entity type.", state.getBlock().getName().getString());
                }
            }
            catch (Exception e) {
                TerrastorageClient.CLIENT_LOGGER.error("Failed to instantiate a block entity instance for the '{}' block, skipping block nametag renderer registration check.", state.getBlock().getName().getString(), e);
            }
        });
    }

    /**
     * Gets the block state of the first block from the block entity type's supported blocks set.
     * @param blockEntityType The block entity type.
     * @return The block state of the first block, or null if the blocks set is empty.
     */
    private static BlockState getBlockState(BlockEntityType<?> blockEntityType) {
        Set<Block> blocks = ((BlockEntityTypeAccessor) blockEntityType).blocks();
        return blocks.stream().findFirst().map(Block::getDefaultState).orElse(null);
    }

    /**
     * Checks if a block entity type has a dedicated BlockEntityRenderer class.
     * @param blockEntityType The block entity type.
     * @return True if it has, false otherwise.
     */
    private static boolean hasRenderer(BlockEntityType<?> blockEntityType) {
        return BlockEntityRendererFactoriesMixin.getFactories().containsKey(blockEntityType);
    }
}
