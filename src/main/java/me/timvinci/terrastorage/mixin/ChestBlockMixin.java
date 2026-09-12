package me.timvinci.terrastorage.mixin;

import me.timvinci.terrastorage.network.NetworkHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mixin of the ChestBlock class, used to detect the creation of double chests.
 */
@Mixin(ChestBlock.class)
public class ChestBlockMixin {

    /**
     * Injects into {@code ChestBlock#updateShape} at RETURN.
     * Transfers the custom name from a single chest to a newly formed double chest block entity.
     */
    @Inject(method = "updateShape", at = @At("RETURN"))
    private void onUpdateShapeReturn(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        // Ignore the call of this method on the client side, since the server sided WorldAccess is needed.
        if (world.isClientSide()) {
            return;
        }

        BlockState returnState = cir.getReturnValue();
        if (state.getValue(ChestBlock.TYPE) == ChestType.SINGLE && returnState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BaseContainerBlockEntity chestBlockEntity = (BaseContainerBlockEntity) world.getBlockEntity(pos);

            if (chestBlockEntity.hasCustomName()) {
                ServerLevel serverWorld = (ServerLevel) world;
                MinecraftServer server = serverWorld.getServer();
                Component customName = chestBlockEntity.getCustomName();

                // The transfer is queued rather than passed to MinecraftServer#execute, which runs a task in place
                // whenever the server thread isn't already inside one. It has to run after the placement is over,
                // or the item components of the placed chest are applied to its block entity and clear the name.
                server.schedule(server.wrapRunnable(() -> {
                    // The newly formed half may be gone by the time the task runs.
                    if (!(serverWorld.getBlockEntity(neighborPos) instanceof BaseContainerBlockEntity chestNeighborBlockEntity)) {
                        return;
                    }

                    ((BaseContainerBlockEntityAccessor) chestNeighborBlockEntity).setName(customName);

                    chestNeighborBlockEntity.setChanged();
                    NetworkHandler.sendGlobalBlockRenamedPayload(serverWorld, neighborPos, customName.getString());
                }));
            }
        }
    }
}
