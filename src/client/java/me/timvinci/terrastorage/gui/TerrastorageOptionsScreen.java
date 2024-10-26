package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.text.Text;

/**
 * The options screen.
 */
public class TerrastorageOptionsScreen extends GameOptionsScreen {

    public TerrastorageOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("terrastorage.button.options"));
    }

    /**
     * Adding the option buttons to the screen.
     */
    @Override
    protected void addOptions() {
        if (this.body != null) {
            this.body.addAll(ClientConfigManager.getInstance().asOptions());
        }
    }

    /**
     * Saving the changes when the screen is closed.
     */
    @Override
    public void close() {
        if (!ClientConfigManager.getInstance().saveConfig() && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(TextStyler.styleError("terrastorage.message.client_saving_error"), false);
        }
        this.client.setScreen(this.parent);
    }
}
