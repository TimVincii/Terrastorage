package me.timvinci.terrastorage.mixin;

import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A mixin accessor for the BaseContainerBlockEntity class.
 */
@Mixin(BaseContainerBlockEntity.class)
public interface BaseContainerBlockEntityAccessor {
    @Accessor("name")
    void setName(Component customName);

    @Invoker("getDefaultName")
    Component invokeGetDefaultName();
}
