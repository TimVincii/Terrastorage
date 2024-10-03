package me.timvinci.mixin;

import me.timvinci.network.NetworkHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
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
    @Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        // Ignore the call of this method on the client side, since the server sided WorldAccess is needed.
        if (world.isClient()) {
            return cir.getReturnValue();
        }

        BlockState returnState = cir.getReturnValue();
        if (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE && returnState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            LockableContainerBlockEntity chestBlockEntity = (LockableContainerBlockEntity) world.getBlockEntity(pos);

            if (chestBlockEntity.hasCustomName()) {
                ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.getServer().execute(() -> {
                    LockableContainerBlockEntity chestNeighborBlockEntity = (LockableContainerBlockEntity) serverWorld.getBlockEntity(neighborPos);
                    ((LockableContainerBlockEntityAccessor)chestNeighborBlockEntity).setCustomName(chestBlockEntity.getCustomName());

                    chestNeighborBlockEntity.markDirty();
                    NetworkHandler.sendGlobalBlockRenamedPacket(serverWorld, neighborPos, chestBlockEntity.getCustomName().getString());
                });
            }
        }
        return returnState;
    }
}
