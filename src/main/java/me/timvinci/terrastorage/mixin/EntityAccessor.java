package me.timvinci.terrastorage.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A mixin accessor for the Entity class.
 */
@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker("getDefaultName")
    Text invokeGetDefaultName();
}
