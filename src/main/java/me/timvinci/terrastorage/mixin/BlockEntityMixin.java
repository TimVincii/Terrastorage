package me.timvinci.terrastorage.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

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
    @ModifyReturnValue(method = "getUpdateTag", at = @At("RETURN"))
    private CompoundTag toInitialChunkDataNbt(CompoundTag original, HolderLookup.Provider registryLookup) {
        if ((BlockEntity) (Object) this instanceof BaseContainerBlockEntity baseContainerBlockEntity) {
            Component customName = baseContainerBlockEntity.getCustomName();
            if (customName != null) {
                JsonElement json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, customName).getOrThrow();
                original.putString("CustomName", json.toString());
            }
        }

        return original;
    }
}
