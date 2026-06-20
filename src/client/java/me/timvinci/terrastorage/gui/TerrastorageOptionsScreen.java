package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.TextStyler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import com.mojang.datafixers.util.Pair;

import java.util.Iterator;
import java.util.List;

/**
 * The options screen.
 */
public class TerrastorageOptionsScreen extends OptionsSubScreen {

    public TerrastorageOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("terrastorage.title.options_screen"));
    }

    /**
     * Adding the option buttons to the screen.
     */
    @Override
    protected void addOptions() {
        if (this.list != null) {
            List<Pair<AbstractWidget, Boolean>> options = ClientConfigManager.getInstance().asOptions();
            options.add(3, Pair.of(getButtonsCustomizationButton(), false));
            Iterator<Pair<AbstractWidget, Boolean>> iterator = options.iterator();

            while (iterator.hasNext()) {
                Pair<AbstractWidget, Boolean> current = iterator.next();

                if (current.getSecond()) {
                    current.getFirst().setWidth(310);
                    this.list.addSmall(current.getFirst(), null);
                } else {
                    AbstractWidget secondWidget = iterator.hasNext() ? iterator.next().getFirst() : null;
                    this.list.addSmall(current.getFirst(), secondWidget);
                }
            }
        }
    }

    private Button getButtonsCustomizationButton() {
        return Button.builder(Component.translatable("terrastorage.button.buttons_customization"),
                onPress -> Minecraft.getInstance().setScreen(new ButtonsCustomizationScreen(Minecraft.getInstance().screen)))
        .tooltip(Tooltip.create(Component.translatable("terrastorage.button.tooltip.buttons_customization"))).build();
    }


    /**
     * Saving the changes when the screen is closed.
     */
    @Override
    public void onClose() {
        if (!ClientConfigManager.getInstance().saveConfig() && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(TextStyler.error("terrastorage.message.client_saving_error"));
        }
        this.minecraft.setScreen(this.lastScreen);
    }
}
