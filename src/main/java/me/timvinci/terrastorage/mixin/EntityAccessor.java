package me.timvinci.terrastorage.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A mixin accessor for the Entity class.
 */
@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("getTypeName")
    Component invokeGetTypeName();
}
