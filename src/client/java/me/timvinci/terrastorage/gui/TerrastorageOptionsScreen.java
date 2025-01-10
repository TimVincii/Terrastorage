package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.TextStyler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.Iterator;
import java.util.List;

/**
 * The options screen.
 */
public class TerrastorageOptionsScreen extends GameOptionsScreen {

    public TerrastorageOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("terrastorage.title.options_screen"));
    }

    /**
     * Adding the option buttons to the screen.
     */
    @Override
    protected void addOptions() {
        if (this.body != null) {
            List<Pair<ClickableWidget, Boolean>> options = ClientConfigManager.getInstance().asOptions();
            options.add(3, new Pair<>(getButtonsCustomizationButton(), false));
            Iterator<Pair<ClickableWidget, Boolean>> iterator = options.iterator();

            while (iterator.hasNext()) {
                Pair<ClickableWidget, Boolean> current = iterator.next();

                if (current.getRight()) {
                    current.getLeft().setWidth(310);
                    this.body.addWidgetEntry(current.getLeft(), null);
                } else {
                    ClickableWidget secondWidget = iterator.hasNext() ? iterator.next().getLeft() : null;
                    this.body.addWidgetEntry(current.getLeft(), secondWidget);
                }
            }
        }
    }

    private ButtonWidget getButtonsCustomizationButton() {
        return ButtonWidget.builder(Text.translatable("terrastorage.button.buttons_customization"),
                        onPress -> MinecraftClient.getInstance().setScreen(new ButtonsCustomizationScreen(MinecraftClient.getInstance().currentScreen)))
        .tooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.buttons_customization"))).build();
    }


    /**
     * Saving the changes when the screen is closed.
     */
    @Override
    public void close() {
        if (!ClientConfigManager.getInstance().saveConfig() && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(TextStyler.error("terrastorage.message.client_saving_error"));
        }
        this.client.setScreen(this.parent);
    }
}
