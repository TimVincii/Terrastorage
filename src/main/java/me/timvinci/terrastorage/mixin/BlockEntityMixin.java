package me.timvinci.terrastorage.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A mixin of the BlockEntity class, used for adding the custom name of lockable container block entities to
 * their initial chunk data.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    /**
     * Add the custom name to the initial chunk nbt data.
     */
    @ModifyReturnValue(method = "toInitialChunkDataNbt", at = @At("RETURN"))
    private NbtCompound toInitialChunkDataNbt(NbtCompound original) {
        if ((BlockEntity) (Object) this instanceof LockableContainerBlockEntity lockableContainerBlockEntity) {
            Text customName = lockableContainerBlockEntity.getCustomName();
            if (customName != null) {
                original.putString("CustomName", Text.Serialization.toJsonString(customName));
            }
        }

        return original;
    }
}
