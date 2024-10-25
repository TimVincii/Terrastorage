package me.timvinci.terrastorage.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * A mixin of the LockableContainerBlockEntity class, used for adding the custom name of certain block entities to
 * their initial chunk data.
 */
@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin extends BlockEntity {
    @Shadow @Nullable
    private Text customName;

    private LockableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Add the custom name to the initial chunk nbt data.
     * @param registryLookup A registry lookup wrapper.
     * @return The initial chunk data nbt with the added custom name.
     */
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        if (customName != null) {
            nbt.putString("CustomName", Text.Serialization.toJsonString(customName, registryLookup));
        }

        return nbt;
    }

}
