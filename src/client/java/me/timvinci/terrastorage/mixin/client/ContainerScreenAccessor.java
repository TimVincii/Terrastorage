package me.timvinci.terrastorage.mixin.client;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerScreen.class)
public interface ContainerScreenAccessor {

    @Accessor("CONTAINER_BACKGROUND")
    static ResourceLocation CONTAINER_BACKGROUND() {
        throw new AssertionError();
    }
}
