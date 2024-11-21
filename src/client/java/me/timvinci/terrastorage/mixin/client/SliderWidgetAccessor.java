package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SliderWidget.class)
public interface SliderWidgetAccessor {

    @Invoker("setValue")
    void invokeSetValue(double value);
}
