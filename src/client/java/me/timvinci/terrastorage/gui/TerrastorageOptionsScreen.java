package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.List;

/**
 * The options screen.
 */
public class TerrastorageOptionsScreen extends OptionsSubScreen {

    public TerrastorageOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("terrastorage.button.options"));
    }

    /**
     * Adding the option buttons to the screen.
     */
    @Override
    protected void init() {
        GridLayout grid = new GridLayout();
        grid.defaultCellSetting()
                .paddingRight(10)
                .paddingBottom(5)
                .alignHorizontallyCenter();

        GridLayout.RowHelper adder = grid.createRowHelper(2);
        List<Tuple<AbstractWidget, Boolean>> options = ClientConfigManager.getInstance().asOptions();
        options.add(3, new Tuple<>(getButtonsCustomizationButton(), false));
        for (Tuple<AbstractWidget, Boolean> option : options) {
            if (!option.getB()) {
                adder.addChild(option.getA());
            }
            else {
                AbstractWidget optionWidget = option.getA();
                optionWidget.setWidth(310);
                adder.addChild(optionWidget, 2);
            }
        }

        grid.arrangeElements();

        FrameLayout.alignInRectangle(
                grid,
                0,
                this.height / 6 - 12,
                this.width,
                this.height,
                0.5F,
                0.0F
        );

        grid.visitWidgets(this::addRenderableWidget);

        this.addRenderableWidget(Button.builder(
                        CommonComponents.GUI_DONE, (button) -> this.onClose()).pos(this.width / 2 - 100, this.height - 27)
                .size(200, 20).build());
    }

    private Button getButtonsCustomizationButton() {
        return Button.builder(Component.translatable("terrastorage.button.buttons_customization"),
                    onPress -> Minecraft.getInstance().setScreen(new ButtonsCustomizationScreen(Minecraft.getInstance().screen)))
        .tooltip(Tooltip.create(Component.translatable("terrastorage.button.tooltip.buttons_customization"))).build();
    }

    /**
     * Adds the rendering of the background and the title.
     */
    @Override
    public void render(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(DrawContext);
        DrawContext.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xffffff);
        super.render(DrawContext, mouseX, mouseY, delta);
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
