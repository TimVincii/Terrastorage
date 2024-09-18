package me.timvinci.mixin;

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A mixin accessor for the LockableContainerBlockEntity class.
 */
@Mixin(LockableContainerBlockEntity.class)
public interface LockableContainerBlockEntityAccessor {
    @Accessor("customName")
    void setCustomName(Text customName);

    @Invoker("getContainerName")
    Text invokeGetContainerName();
}
