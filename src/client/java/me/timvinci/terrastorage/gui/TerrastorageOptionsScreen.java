package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * The options screen.
 */
public class TerrastorageOptionsScreen extends GameOptionsScreen {
    private OptionListWidget options;

    public TerrastorageOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("terrastorage.button.options"));
    }

    /**
     * Adding the option buttons to the screen.
     */
    @Override
    protected void init() {
        options = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        options.addAll(ClientConfigManager.getInstance().asOption());
        this.addSelectableChild(options);
        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
                            this.close();
                        }).position(this.width / 2 - 100, this.height - 27)
                        .size(200, 20)
                        .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(context);
        options.render(context, mouseX, mouseY, tickDelta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(context, mouseX, mouseY, tickDelta);
    }

    /**
     * Saving the changes when the screen is closed.
     */
    @Override
    public void close() {
        if (!ClientConfigManager.getInstance().saveConfig() && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(TextStyler.styleError("terrastorage.message.client_saving_error"));
        }
        this.client.setScreen(this.parent);
    }
}
