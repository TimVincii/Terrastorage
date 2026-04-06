package me.timvinci.terrastorage.mixin;

import me.timvinci.terrastorage.mixin.BaseContainerBlockEntityAccessor;
import me.timvinci.terrastorage.network.NetworkHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
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
     * Transfers the custom name from a single chest to a newly formed double chest block entity.
     */
    @Inject(method = "updateShape", at = @At("RETURN"))
    protected void getStateForNeighborUpdate(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        // Ignore the call of this method on the client side, since the server sided WorldAccess is needed.
        if (world.isClientSide()) {
            return;
        }

        BlockState returnState = cir.getReturnValue();
        if (state.getValue(ChestBlock.TYPE) == ChestType.SINGLE && returnState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BaseContainerBlockEntity chestBlockEntity = (BaseContainerBlockEntity) world.getBlockEntity(pos);

            if (chestBlockEntity.hasCustomName()) {
                ServerLevel serverWorld = (ServerLevel) world;
                serverWorld.getServer().execute(() -> {
                    BaseContainerBlockEntity chestNeighborBlockEntity = (BaseContainerBlockEntity) serverWorld.getBlockEntity(neighborPos);
                    ((BaseContainerBlockEntityAccessor)chestNeighborBlockEntity).setName(chestBlockEntity.getCustomName());

                    chestNeighborBlockEntity.setChanged();
                    NetworkHandler.sendGlobalBlockRenamedPayload(serverWorld, neighborPos, chestBlockEntity.getCustomName().getString());
                });
            }
        }
    }
}
