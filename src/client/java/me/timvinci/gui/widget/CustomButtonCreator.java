package me.timvinci.gui.widget;

import me.timvinci.gui.RenameScreen;
import me.timvinci.network.ClientNetworkHandler;
import me.timvinci.util.StorageAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Provides a method for creating custom buttons.
 */
public class CustomButtonCreator {

    /**
     * Creates a custom button to be used by the HandledScreenMixin.
     * @param buttonText The text.
     * @param buttonTooltip The tooltip.
     * @param x The x position.
     * @param y The y position.
     * @param width The width.
     * @param height The height.
     * @return A custom button with the aforementioned properties.
     */
    public static CustomButtonWidget createCustomButton(Text buttonText, Tooltip buttonTooltip, int x, int y, int width, int height) {
        ButtonWidget.PressAction onPress;
        // Modify the press action based on which custom button is being created.
        if (buttonText.equals(Text.translatable("terrastorage.button.sort_items"))) {
            onPress = button -> ClientNetworkHandler.sendStorageSortPayload();
        } else if (buttonText.equals(Text.translatable("terrastorage.button.rename"))) {
            onPress = button -> {
                MinecraftClient client = MinecraftClient.getInstance();
                String name = client.currentScreen.getTitle().getString();
                client.execute(() -> {
                    client.setScreen(new RenameScreen(client.currentScreen, name));
                });
            };
        } else {
            // Get the StorageAction enum constant from the text of the button.
            StorageAction action = StorageAction.valueOf(buttonText.getString().replaceAll(" ", "_").toUpperCase());
            onPress = button -> ClientNetworkHandler.sendActionPayload(action);
        }

        return new CustomButtonWidget(
            x,
            y,
            width,
            height,
            buttonText,
            buttonTooltip,
            onPress
        );
    }
}
