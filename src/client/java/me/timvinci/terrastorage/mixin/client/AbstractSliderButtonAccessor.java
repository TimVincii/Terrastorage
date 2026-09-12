package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * A mixin accessor for the AbstractSliderButton class.
 */
@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonAccessor {

    @Invoker("setValue")
    void invokeSetValue(double value);
}
