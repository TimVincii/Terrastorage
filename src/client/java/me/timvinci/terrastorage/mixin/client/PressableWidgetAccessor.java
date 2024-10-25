package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A mixin accessor for the PressableWidget class.
 */
@Mixin(PressableWidget.class)
public interface PressableWidgetAccessor {

    @Accessor("TEXTURES")
    static ButtonTextures getTextures() {
        throw new AssertionError();
    }
}
