package me.timvinci.mixin.client;

import me.timvinci.config.ClientConfigManager;
import me.timvinci.gui.TerrastorageOptionsScreen;
import me.timvinci.gui.widget.CustomButtonCreator;
import me.timvinci.util.ButtonsPlacement;
import me.timvinci.util.ButtonsStyle;
import me.timvinci.gui.widget.CustomButtonWidget;
import me.timvinci.util.LocalizedTextProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the HandledScreen class, adds the storage option buttons to storage screens.
 * @param <T> The screen handler type.
 */
@Mixin(HandledScreen.class )
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected int x;
    @Shadow protected int y;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    /**
     * Adds the storage option buttons once the handled screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode.
        if (MinecraftClient.getInstance().player.isSpectator())
            return;

        boolean isEnderChest = false;
        if (this.getScreenHandler() instanceof GenericContainerScreenHandler) {
            if (this.getTitle().equals(Text.translatable("container.enderchest"))) {
                isEnderChest = true;
            }
        }
        else if (!(this.getScreenHandler() instanceof ShulkerBoxScreenHandler)) {
            return;
        }

        // Set the button sizes and their spacing.
        int buttonWidth = 70;
        int buttonHeight = 15;
        int buttonSpacing = 2;

        Text[] buttonsText = LocalizedTextProvider.getButtonsText(isEnderChest);
        Tooltip[] buttonsTooltip = LocalizedTextProvider.getButtonsTooltip(isEnderChest);

        // Place the buttons on the side of the container gui.
        int buttonX = ClientConfigManager.getInstance().getConfig().getButtonsPlacement() == ButtonsPlacement.RIGHT?
                x + backgroundWidth + 5 :
                x - (buttonWidth + 5);
        // Get the height of the container, excluding the player's inventory portion whose height is 94.
        int containerHeight = backgroundHeight - 94;
        int buttonSectionHeight = buttonsText.length * buttonHeight + (buttonsText.length-1) * buttonSpacing;
        // Centering the buttons vertically alongside the container gui.
        int buttonY = y - (buttonSectionHeight - containerHeight) / 2;

        if (ClientConfigManager.getInstance().getConfig().getButtonsStyle() == ButtonsStyle.TEXT_ONLY) {
            TextRenderer textRenderer = this.textRenderer;
            // Create the buttons with the width of their text.
            for (int i = 0; i < buttonsText.length; i++) {
                buttonWidth = textRenderer.getWidth(buttonsText[i]) + 6;
                CustomButtonWidget button = CustomButtonCreator.createCustomButton(buttonsText[i], buttonsTooltip[i], buttonX, buttonY, buttonWidth, buttonHeight);
                this.addDrawableChild(button);

                buttonY += buttonHeight + buttonSpacing;
            }
        }
        else {
            // Create the buttons with a set width.
            for (int i = 0; i < buttonsText.length; i++) {
                CustomButtonWidget button = CustomButtonCreator.createCustomButton(buttonsText[i], buttonsTooltip[i], buttonX, buttonY, buttonWidth, buttonHeight);
                this.addDrawableChild(button);

                buttonY += buttonHeight + buttonSpacing;
            }
        }

        // Add the options buttons if it is enabled.
        if (ClientConfigManager.getInstance().getConfig().getDisplayOptionsButton()) {
            int optionsButtonX = (client.currentScreen.width - 120) / 2;
            int optionsButtonY = y - 20;
            ButtonWidget optionsButtonWidget = ButtonWidget.builder(
                            Text.translatable("terrastorage.button.options"),
                            onPress -> {
                                client.execute(() -> {
                                    client.setScreen(new TerrastorageOptionsScreen(client.currentScreen));
                                });
                            })
                    .size(120, 15)
                    .position(optionsButtonX, optionsButtonY)
                    .build();
            optionsButtonWidget.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.options")));

            this.addDrawableChild(optionsButtonWidget);
        }
    }
}
