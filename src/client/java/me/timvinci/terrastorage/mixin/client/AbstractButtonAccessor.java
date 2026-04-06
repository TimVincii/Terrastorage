package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A mixin accessor for the AbstractButton class.
 */
@Mixin(AbstractButton.class)
public interface AbstractButtonAccessor {

    @Accessor("SPRITES")
    static WidgetSprites getTextures() {
        throw new AssertionError();
    }
}
