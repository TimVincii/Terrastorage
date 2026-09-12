package me.timvinci.terrastorage.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A mixin of the BlockEntity class, used for adding the custom name of container block entities to the update tag
 * that is sent to clients.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    /**
     * Modifies the return value of {@link BlockEntity#getUpdateTag} at RETURN.
     * Adds the custom name of container block entities to the tag, so that clients receive it alongside the rest of
     * the block entity data.
     */
    @ModifyReturnValue(method = "getUpdateTag", at = @At("RETURN"))
    private CompoundTag modifyGetUpdateTagReturn(CompoundTag original) {
        if ((BlockEntity) (Object) this instanceof BaseContainerBlockEntity baseContainerBlockEntity) {
            Component customName = baseContainerBlockEntity.getCustomName();
            if (customName != null) {
                original.putString("CustomName", Component.Serializer.toJson(customName));
            }
        }

        return original;
    }
}
