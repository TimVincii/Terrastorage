package me.timvinci.gui.widget;

import me.timvinci.gui.RenameScreen;
import me.timvinci.network.ClientNetworkHandler;
import me.timvinci.util.StorageAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Provides a method for creating storage buttons.
 */
public class StorageButtonCreator {

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
    public static StorageButtonWidget createStorageButton(StorageAction action, Text buttonText, Tooltip buttonTooltip, int x, int y, int width, int height) {
        ButtonWidget.PressAction onPress = switch (action) {
            case SORT_ITEMS -> button -> ClientNetworkHandler.sendStorageSortPacket();
            case RENAME -> button -> {
                MinecraftClient client = MinecraftClient.getInstance();
                String name = client.currentScreen.getTitle().getString();
                client.execute(() -> client.setScreen(new RenameScreen(client.currentScreen, name)));
            };
            default -> button -> ClientNetworkHandler.sendActionPacket(action);
        };

        return new StorageButtonWidget(
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
