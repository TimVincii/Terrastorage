package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GenericContainerScreen.class)
public interface GenericContainerScreenAccessor {

    @Accessor("TEXTURE")
    static Identifier TEXTURE() {
        throw new AssertionError();
    }
}
