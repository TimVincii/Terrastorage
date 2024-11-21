package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;

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
    protected void init() {
        GridWidget grid = new GridWidget();
        grid.getMainPositioner()
                .marginRight(10)
                .marginBottom(5)
                .alignHorizontalCenter();

        GridWidget.Adder adder = grid.createAdder(2);
        List<Pair<ClickableWidget, Boolean>> options = ClientConfigManager.getInstance().asOptions();
        options.add(3, new Pair<>(getButtonsCustomizationButton(), false));
        for (Pair<ClickableWidget, Boolean> option : options) {
            if (!option.getRight()) {
                adder.add(option.getLeft());
            }
            else {
                ClickableWidget optionWidget = option.getLeft();
                optionWidget.setWidth(310);
                adder.add(optionWidget, 2);
            }
        }

        grid.refreshPositions();

        SimplePositioningWidget.setPos(
                grid,
                0,
                this.height / 6 - 12,
                this.width,
                this.height,
                0.5F,
                0.0F
        );

        grid.forEachChild(this::addDrawableChild);

        this.addDrawableChild(ButtonWidget.builder(
                        ScreenTexts.DONE, (button) -> this.close()).position(this.width / 2 - 100, this.height - 27)
                .size(200, 20).build());
    }

    private ButtonWidget getButtonsCustomizationButton() {
        return ButtonWidget.builder(Text.translatable("terrastorage.button.buttons_customization"),
                    onPress -> MinecraftClient.getInstance().setScreen(new ButtonsCustomizationScreen(MinecraftClient.getInstance().currentScreen)))
        .tooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.buttons_customization"))).build();
    }

    /**
     * Adds the rendering of the background and the title.
     */
    @Override
    public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(DrawContext);
        DrawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        super.render(DrawContext, mouseX, mouseY, delta);
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
