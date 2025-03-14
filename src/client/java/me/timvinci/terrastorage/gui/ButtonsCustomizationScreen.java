package me.timvinci.terrastorage.gui;

import com.mojang.serialization.Codec;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.config.TerrastorageClientConfig;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.gui.widget.StorageButtonWidget;
import me.timvinci.terrastorage.mixin.client.GenericContainerScreenAccessor;
import me.timvinci.terrastorage.mixin.client.SliderWidgetAccessor;
import me.timvinci.terrastorage.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The buttons customization screen.
 */
public class ButtonsCustomizationScreen extends Screen {
    private static final Identifier TEXTURE = GenericContainerScreenAccessor.TEXTURE();
    private final Screen parent;
    private final Text storagePreviewTitle = Text.translatable("terrastorage.title.buttons_customization_screen.storage");

    private int x;
    private int y;
    private final int backgroundWidth = 176;
    private int backgroundHeight = 166;
    private final int storagePreviewTitleX = 8;
    private final int storagePreviewTitleY = 6;

    private final Text playerInventoryTitle = Text.translatable("container.inventory");
    protected int playerInventoryTitleX = 8;
    protected int playerInventoryTitleY = backgroundHeight - 94;

    private int rows = 6;
    private boolean widgetsInitialized = false;
    private int buttonActionsLength;
    private final List<ClickableWidget> customizationWidgets;
    private final int customizationWidgetsWidth = 150;
    private final int customizationWidgetsHeight = 20;
    private final int customizationWidgetsSpacing = 5;

    private final List<ButtonWidget> actionWidgets;

    private final List<StorageButtonWidget> storageOptionsButtons;
    private boolean storageOptionsEnabled;
    private boolean storageOptionsTooltip;
    private ButtonsStyle storageOptionsStyle;
    private ButtonsPlacement storageOptionsPlacement;
    private int storageOptionsXOffset;
    private int storageOptionsYOffset;
    private int storageOptionsWidth;
    private int storageOptionsHeight;
    private int storageOptionsSpacing;

    public ButtonsCustomizationScreen(Screen parent) {
        super(Text.translatable("terrastorage.title.buttons_customization_screen"));
        this.parent = parent;
        updateBackgroundHeight();

        customizationWidgets = new ArrayList<>();
        actionWidgets = new ArrayList<>();
        storageOptionsButtons = new ArrayList<>();

        TerrastorageClientConfig config = ClientConfigManager.getInstance().getConfig();
        storageOptionsEnabled = config.getButtonsEnabled();
        storageOptionsTooltip = config.getButtonsTooltip();
        storageOptionsStyle = config.getButtonsStyle();
        storageOptionsPlacement = config.getButtonsPlacement();
        storageOptionsXOffset = config.getButtonsXOffset();
        storageOptionsYOffset = config.getButtonsYOffset();
        storageOptionsWidth = config.getButtonsWidth();
        storageOptionsHeight = config.getButtonsHeight();
        storageOptionsSpacing = config.getButtonsSpacing();
    }

    private void updateBackgroundHeight() {
        this.backgroundHeight = 114 + this.rows * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private void updateXAndY() {
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    /**
     * Initializes the widgets if they aren't initialized yet, and positions them properly on the screen.
     */
    @Override
    protected void init() {
        updateXAndY();

        if (!widgetsInitialized) {
            initializeWidgets();
            widgetsInitialized = true;
        }

        if (storageOptionsEnabled) {
            updateStorageOptionsEnabled();
        }

        // Positioning the customization widgets.
        int buttonX = storageOptionsPlacement == ButtonsPlacement.RIGHT ?
                (this.x - customizationWidgetsWidth) / 2 :
                (this.x + this.backgroundWidth) + ((this.width - (this.x + backgroundWidth) - customizationWidgetsWidth) / 2);

        int buttonSectionHeight = customizationWidgets.size() * customizationWidgetsHeight + (customizationWidgets.size() - 1) * customizationWidgetsSpacing;
        // Account for the triple spacing on the row preview button.
        buttonSectionHeight += 2 * customizationWidgetsSpacing;
        int buttonY = (this.height - buttonSectionHeight) / 2;


        for (int i = 0; i < customizationWidgets.size(); i++) {
            customizationWidgets.get(i).setPosition(buttonX, buttonY);
            this.addDrawableChild(customizationWidgets.get(i));

            buttonY += customizationWidgetsHeight + (i == 0 ? 3 : 1) * customizationWidgetsSpacing;
        }

        buttonX = (this.width - customizationWidgetsWidth) / 2;
        buttonSectionHeight = actionWidgets.size() * customizationWidgetsHeight + (actionWidgets.size() - 1) * customizationWidgetsSpacing;
        buttonY = (this.height - 7) - buttonSectionHeight;

        // Positioning the action buttons.
        for (int i = 0; i < actionWidgets.size() - 1; i++) {
            actionWidgets.get(i).setPosition(buttonX, buttonY);
            this.addDrawableChild(actionWidgets.get(i));

            buttonY += customizationWidgetsHeight + customizationWidgetsSpacing;
        }

        actionWidgets.getLast().setPosition(this.width / 2 - 100, buttonY);
        this.addDrawableChild(actionWidgets.getLast());
    }

    /**
     * Initializes the storage option buttons and the customization widgets, adding them to their respective lists.
     */
    private void initializeWidgets() {
        StorageAction[] buttonActions = StorageAction.getButtonsActions(false);
        buttonActionsLength = buttonActions.length;

        // Storage option buttons.
        if (storageOptionsTooltip) {
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                Tooltip buttonTooltip = LocalizedTextProvider.buttonTooltipCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createDummyStorageButton(storageOptionsWidth, storageOptionsHeight, buttonText, storageOptionsStyle);
                storageButton.setTooltip(buttonTooltip);

                storageOptionsButtons.add(storageButton);
            }
        }
        else {
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createDummyStorageButton(storageOptionsWidth, storageOptionsHeight, buttonText, storageOptionsStyle);

                storageOptionsButtons.add(storageButton);
            }
        }

        // [0] Preview row count button.
        customizationWidgets.add(ButtonWidget.builder(Text.translatable("terrastorage.option.preview_row_count").append(": " + rows), onPress -> {
            this.rows = (this.rows == 6) ? 3 : 6;
            customizationWidgets.get(0).setMessage(Text.translatable("terrastorage.option.preview_row_count").append(": " + rows));  // Update the button text
            updateBackgroundHeight();
            this.x = (this.width - this.backgroundWidth) / 2;
            this.y = (this.height - this.backgroundHeight) / 2;
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).build());

        // [1] Buttons enabled button.
        customizationWidgets.add(ButtonWidget.builder(LocalizedTextProvider.getBooleanOptionText("buttons_enabled", storageOptionsEnabled), onPress -> {
            storageOptionsEnabled = !storageOptionsEnabled;
            customizationWidgets.get(1).setMessage(LocalizedTextProvider.getBooleanOptionText("buttons_enabled", storageOptionsEnabled));
            updateStorageOptionsEnabled();
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).build());

        // [2] Buttons tooltip button.
        customizationWidgets.add(ButtonWidget.builder(LocalizedTextProvider.getBooleanOptionText("buttons_tooltip", storageOptionsTooltip), onPress -> {
            storageOptionsTooltip = !storageOptionsTooltip;
            customizationWidgets.get(2).setMessage(LocalizedTextProvider.getBooleanOptionText("buttons_tooltip", storageOptionsTooltip));
            updateStorageOptionsTooltip();
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).build());

        // [3] Buttons style button.
        customizationWidgets.add(ButtonWidget.builder(LocalizedTextProvider.getEnumOptionText("buttons_style", storageOptionsStyle), onPress -> {
            storageOptionsStyle = ButtonsStyle.next(storageOptionsStyle);
            customizationWidgets.get(3).setMessage(LocalizedTextProvider.getEnumOptionText("buttons_style", storageOptionsStyle));
            updateStorageOptionsStyle();
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).build());

        // [4] Buttons placement button.
        customizationWidgets.add(ButtonWidget.builder(LocalizedTextProvider.getEnumOptionText("buttons_placement", storageOptionsPlacement), onPress -> {
            storageOptionsPlacement = ButtonsPlacement.next(storageOptionsPlacement);
            customizationWidgets.get(4).setMessage(LocalizedTextProvider.getEnumOptionText("buttons_placement", storageOptionsPlacement));
            updateStorageOptionsX();
            updateCustomizationWidgetsX();
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).build());

        // [5] Buttons x offset slider.
        customizationWidgets.add(new SimpleOption<>(
                "terrastorage.option.buttons_x_offset",
                SimpleOption.emptyTooltip(),
                (text, value) -> Text.of(text.getString() + ": " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(-100, 100),
                Codec.INT,
                storageOptionsXOffset,
                newValue -> {
                    storageOptionsXOffset = newValue;
                    updateStorageOptionsX();
                }
        ).createWidget(this.client.options, 0, 0, customizationWidgetsWidth));

        // [6] Buttons y offset slider.
        customizationWidgets.add(new SimpleOption<>(
                "terrastorage.option.buttons_y_offset",
                SimpleOption.emptyTooltip(),
                (text, value) -> Text.of(text.getString() + ": " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(-100, 100),
                Codec.INT,
                storageOptionsYOffset,
                newValue -> {
                    storageOptionsYOffset = newValue;
                    updateStorageOptionsY();
                }
        ).createWidget(this.client.options, 0, 0, customizationWidgetsWidth));

        // [7] Buttons width slider.
        customizationWidgets.add(new SimpleOption<>(
                "terrastorage.option.buttons_width",
                SimpleOption.emptyTooltip(),
                (text, value) -> Text.of(text.getString() + ": " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(20, 150),
                Codec.INT,
                storageOptionsWidth,
                newValue -> {
                    storageOptionsWidth = newValue;
                    updateStorageOptionsWidth();
                }
        ).createWidget(this.client.options, 0, 0, customizationWidgetsWidth));
        if (storageOptionsStyle == ButtonsStyle.TEXT_ONLY) {
            setWidthCustomizationEnabled(false);
        }

        // [8] Buttons height slider.
        customizationWidgets.add(new SimpleOption<>(
                "terrastorage.option.buttons_height",
                SimpleOption.emptyTooltip(),
                (text, value) -> Text.of(text.getString() + ": " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(5, 50),
                Codec.INT,
                storageOptionsHeight,
                newValue -> {
                    storageOptionsHeight = newValue;
                    updateStorageOptionsHeight();
                }
        ).createWidget(this.client.options, 0, 0, customizationWidgetsWidth));

        // [9] Buttons spacing slider.
        customizationWidgets.add(new SimpleOption<>(
                "terrastorage.option.buttons_spacing",
                SimpleOption.emptyTooltip(),
                (text, value) -> Text.of(text.getString() + ": " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(0, 20),
                Codec.INT,
                storageOptionsSpacing,
                newValue -> {
                    storageOptionsSpacing = newValue;
                    updateStorageOptionsY();
                }
        ).createWidget(this.client.options, 0, 0, customizationWidgetsWidth));

        // [0] Reset To Default button.
        actionWidgets.add(ButtonWidget.builder(Text.translatable("terrastorage.option.reset_to_default"), onPress -> {
            applyFromConfig(ClientConfigManager.getInstance().getDefaultConfig());
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).tooltip(Tooltip.of(Text.translatable("terrastorage.option.tooltip.reset_to_default"))).build());

        // [1] Undo Changes button.
        actionWidgets.add(ButtonWidget.builder(Text.translatable("terrastorage.option.undo_changes"), onPress -> {
            applyFromConfig(ClientConfigManager.getInstance().getConfig());
        }).size(customizationWidgetsWidth, customizationWidgetsHeight).tooltip(Tooltip.of(Text.translatable("terrastorage.option.tooltip.undo_changes"))).build());

        // [2] Save Changes button.
        actionWidgets.add(ButtonWidget.builder(Text.translatable("terrastorage.option.save_and_exit"), onPress -> {
            TerrastorageClientConfig config = ClientConfigManager.getInstance().getConfig();
            config.setButtonsEnabled(storageOptionsEnabled);
            config.setButtonsTooltip(storageOptionsTooltip);
            config.setButtonsStyle(storageOptionsStyle);
            config.setButtonsPlacement(storageOptionsPlacement);
            config.setButtonsXOffset(storageOptionsXOffset);
            config.setButtonsYOffset(storageOptionsYOffset);
            config.setButtonsWidth(storageOptionsWidth);
            config.setButtonsHeight(storageOptionsHeight);
            config.setButtonsSpacing(storageOptionsSpacing);

            if (!ClientConfigManager.getInstance().saveConfig() && MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(TextStyler.error("terrastorage.message.client_saving_error"), false);
            }
            close();
        }).size(200, customizationWidgetsHeight).tooltip(Tooltip.of(Text.translatable("terrastorage.option.tooltip.save_and_exit"))).build());
    }

    /**
     * Updates the x of the customization widgets.
     */
    private void updateCustomizationWidgetsX() {
        int customizationWidgetsX = storageOptionsPlacement == ButtonsPlacement.RIGHT ?
                (this.x - customizationWidgetsWidth) / 2 :
                (this.x+ this.backgroundWidth) + ((this.width - (this.x + backgroundWidth) - customizationWidgetsWidth) / 2);

        customizationWidgets.forEach(button -> button.setX(customizationWidgetsX));
    }

    private void updateStorageOptionsEnabled() {
        if (storageOptionsEnabled) {
            // Positioning the storage option buttons.
            int buttonX = storageOptionsPlacement == ButtonsPlacement.RIGHT ?
                    this.x + this.backgroundWidth + 5 + storageOptionsXOffset :
                    this.x - ((storageOptionsStyle == ButtonsStyle.DEFAULT ? storageOptionsWidth : 70) + 5) + storageOptionsXOffset;
            int buttonSectionHeight = buttonActionsLength * storageOptionsHeight + (buttonActionsLength - 1) * storageOptionsSpacing;
            int buttonY = this.y - (buttonSectionHeight - this.playerInventoryTitleY) / 2 + storageOptionsYOffset;

            for (ButtonWidget storageOptionButton : storageOptionsButtons) {
                storageOptionButton.setPosition(buttonX, buttonY);
                this.addDrawableChild(storageOptionButton);

                buttonY += storageOptionsHeight + storageOptionsSpacing;
            }
        }
        else {
            for (ButtonWidget storageOptionButton : storageOptionsButtons) {
                this.remove(storageOptionButton);
            }
        }
    }

    private void updateStorageOptionsTooltip() {
        if (storageOptionsTooltip) {
            StorageAction[] buttonActions = StorageAction.getButtonsActions(false);
            for (int i = 0; i < buttonActions.length; i++) {
                storageOptionsButtons.get(i).setTooltip(LocalizedTextProvider.buttonTooltipCache.get(buttonActions[i]));
            }
        }
        else {
            storageOptionsButtons.forEach(button -> button.setTooltip(Tooltip.of(Text.empty())));
        }
    }

    /**
     * Updates the style of the storage options.
     */
    private void updateStorageOptionsStyle() {
        boolean isTextOnly = storageOptionsStyle == ButtonsStyle.TEXT_ONLY;
        storageOptionsButtons.forEach(button -> {
            if (isTextOnly) {
                button.setWidth(MinecraftClient.getInstance().textRenderer.getWidth(button.getMessage()) + 6);
            }
            else {
                button.setWidth(storageOptionsWidth);
            }
            button.setButtonStyle(storageOptionsStyle);
        });
        setWidthCustomizationEnabled(!isTextOnly);
        if (storageOptionsPlacement == ButtonsPlacement.LEFT && storageOptionsWidth != 70) {
            updateStorageOptionsX();
        }
    }

    /**
     * Updates the x of the storage options.
     */
    private void updateStorageOptionsX() {
        int newX = storageOptionsPlacement == ButtonsPlacement.RIGHT?
                this.x + this.backgroundWidth + 5 + storageOptionsXOffset :
                this.x - ((storageOptionsStyle == ButtonsStyle.DEFAULT ? storageOptionsWidth : 70) + 5) + storageOptionsXOffset;
        storageOptionsButtons.forEach(button -> button.setX(newX));
    }

    /**
     * Updates the y of the storage options.
     */
    private void updateStorageOptionsY() {
        int buttonSectionHeight = buttonActionsLength * storageOptionsHeight + (buttonActionsLength-1) * storageOptionsSpacing;
        int buttonY = this.y - (buttonSectionHeight - this.playerInventoryTitleY) / 2 + storageOptionsYOffset;

        for (StorageButtonWidget storageOptionButton : storageOptionsButtons) {
            storageOptionButton.setY(buttonY);
            buttonY += storageOptionsHeight + storageOptionsSpacing;
        }
    }

    /**
     * Updates the width of the storage options.
     */
    private void updateStorageOptionsWidth() {
        storageOptionsButtons.forEach(button -> button.setWidth(storageOptionsWidth));
        if (storageOptionsPlacement == ButtonsPlacement.LEFT) {
            updateStorageOptionsX();
        }
    }

    /**
     * Updates the height of the storage options.
     */
    private void updateStorageOptionsHeight() {
        storageOptionsButtons.forEach(button -> button.setHeight(storageOptionsHeight));
        updateStorageOptionsY();
    }

    /**
     * Updates the state of the width customization slider.
     * @param enabled Whether it is enabled or not.
     */
    private void setWidthCustomizationEnabled(boolean enabled) {
        customizationWidgets.get(7).active = enabled;
        customizationWidgets.get(7).setTooltip(Tooltip.of(enabled ? Text.empty() : Text.translatable("terrastorage.option.tooltip.button_width_disabled")));
    }

    /**
     * Applies the values from the given config.
     * @param config The config instance.
     */
    private void applyFromConfig(TerrastorageClientConfig config) {
        if (storageOptionsEnabled != config.getButtonsEnabled()) {
            storageOptionsEnabled = config.getButtonsEnabled();
            updateStorageOptionsEnabled();
            customizationWidgets.get(1).setMessage(LocalizedTextProvider.getBooleanOptionText("buttons_enabled", storageOptionsEnabled));
        }

        if (storageOptionsTooltip != config.getButtonsTooltip()) {
            storageOptionsTooltip = config.getButtonsTooltip();
            updateStorageOptionsTooltip();
            customizationWidgets.get(2).setMessage(LocalizedTextProvider.getBooleanOptionText("buttons_tooltip", storageOptionsTooltip));
        }

        if (storageOptionsStyle != config.getButtonsStyle()) {
            storageOptionsStyle = config.getButtonsStyle();
            updateStorageOptionsStyle();
            customizationWidgets.get(3).setMessage(LocalizedTextProvider.getEnumOptionText("buttons_style", storageOptionsStyle));
        }

        if (storageOptionsPlacement != config.getButtonsPlacement()) {
            storageOptionsPlacement = config.getButtonsPlacement();
            updateStorageOptionsX();
            updateCustomizationWidgetsX();
            customizationWidgets.get(4).setMessage(LocalizedTextProvider.getEnumOptionText("buttons_placement", storageOptionsPlacement));
        }

        if (storageOptionsXOffset != config.getButtonsXOffset()) {
            storageOptionsXOffset = config.getButtonsXOffset();
            updateStorageOptionsX();
            if (customizationWidgets.get(5) instanceof OptionSliderWidget optionSliderWidget) {
                double normalizedValue = (storageOptionsXOffset + 100) / 200.0;
                ((SliderWidgetAccessor)optionSliderWidget).invokeSetValue(normalizedValue);
            }
        }

        if (storageOptionsYOffset != config.getButtonsYOffset()) {
            storageOptionsYOffset = config.getButtonsYOffset();
            updateStorageOptionsY();
            if (customizationWidgets.get(6) instanceof OptionSliderWidget optionSliderWidget) {
                double normalizedValue = (storageOptionsYOffset + 100) / 200.0;
                ((SliderWidgetAccessor)optionSliderWidget).invokeSetValue(normalizedValue);
            }
        }

        if (storageOptionsWidth != config.getButtonsWidth()) {
            storageOptionsWidth = config.getButtonsWidth();
            if (storageOptionsStyle == ButtonsStyle.DEFAULT) {
                updateStorageOptionsWidth();
            }
            if (customizationWidgets.get(7) instanceof OptionSliderWidget optionSliderWidget) {
                double normalizedValue = (storageOptionsWidth - 20) / 130.0;
                ((SliderWidgetAccessor)optionSliderWidget).invokeSetValue(normalizedValue);
            }
        }

        if (storageOptionsHeight != config.getButtonsHeight()) {
            storageOptionsHeight = config.getButtonsHeight();
            updateStorageOptionsHeight();
            if (customizationWidgets.get(8) instanceof OptionSliderWidget optionSliderWidget) {
                double normalizedValue = (storageOptionsHeight - 5) / 45.0;
                ((SliderWidgetAccessor)optionSliderWidget).invokeSetValue(normalizedValue);
            }
        }

        if (storageOptionsSpacing != config.getButtonsSpacing()) {
            storageOptionsSpacing = config.getButtonsSpacing();
            updateStorageOptionsHeight();
            if (customizationWidgets.get(9) instanceof OptionSliderWidget optionSliderWidget) {
                double normalizedValue = storageOptionsSpacing / 20.0;
                ((SliderWidgetAccessor)optionSliderWidget).invokeSetValue(normalizedValue);
            }
        }
    }

    /**
     * Adds the rendering of the title.
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 16777215);
    }

    /**
     * Renders the storage inventory screen in the background.
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int i = (this.width - backgroundWidth) / 2;
        int j = (this.height - backgroundHeight) / 2;

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, i, j, 0.0F, 0.0F, backgroundWidth, rows * 18 + 17, 256, 256, ColorHelper.getWhite(0.5f));
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, i, j + rows * 18 + 17, 0.0F, 126.0F, backgroundWidth, 96, 256, 256, ColorHelper.getWhite(0.5f));

        context.drawText(this.textRenderer, storagePreviewTitle, x + storagePreviewTitleX, y + storagePreviewTitleY, 4210752, false);
        context.drawText(this.textRenderer, playerInventoryTitle, x + playerInventoryTitleX, y + playerInventoryTitleY, 4210752, false);
    }

    /**
     * Stops the screen from pausing the game.
     */
    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close(){
        this.client.setScreen(this.parent);
    }
}
