package me.timvinci.terrastorage.gui.widget;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.gui.RenameScreen;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
import me.timvinci.terrastorage.util.ButtonsStyle;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Locale;

/**
 * Creates the buttons used by Terrastorage.
 */
public class StorageButtonCreator {

    /**
     * Creates a custom button to be used by the HandledScreenMixin.
     * @param buttonText The text.
     * @param x The x position.
     * @param y The y position.
     * @param width The width.
     * @param height The height.
     * @return A custom button with the aforementioned properties.
     */
    public static StorageButtonWidget createStorageButton(StorageAction action, int x, int y, int width, int height, Text buttonText, ButtonsStyle buttonStyle) {
        ButtonWidget.PressAction onPress = switch (action) {
            case SORT_ITEMS -> button -> ClientNetworkHandler.sendSortPacket(false);
            case RENAME -> button -> {
                MinecraftClient client = MinecraftClient.getInstance();
                String name = client.currentScreen.getTitle().getString();
                client.execute(() -> {
                    client.setScreen(new RenameScreen(client.currentScreen, name));
                });
            };
            default -> button -> ClientNetworkHandler.sendActionPacket(action);
        };

        if (buttonStyle == ButtonsStyle.TEXT_ONLY) {
            width = MinecraftClient.getInstance().textRenderer.getWidth(buttonText) + 6;
        }

        return new StorageButtonWidget(
                x,
                y,
                width,
                height,
                buttonText,
                buttonStyle,
                onPress
        );
    }

    public static StorageButtonWidget createDummyStorageButton(int width, int height, Text buttonText, ButtonsStyle buttonStyle) {
        if (buttonStyle == ButtonsStyle.TEXT_ONLY) {
            width = MinecraftClient.getInstance().textRenderer.getWidth(buttonText) + 6;
        }

        return new StorageButtonWidget(
                0,
                0,
                width,
                height,
                buttonText,
                buttonStyle,
                onPress -> {}
        );
    }

    /**
     * Creates the Sort Inventory and Quick Stack To Nearby Storages buttons.
     * @param x The x position.
     * @param y The y position.
     * @return A pair of the inventory TexturedButtonWidgets.
     */
    public static Pair<TexturedButtonWidget, TexturedButtonWidget> createInventoryButtons(int x, int y) {
        Identifier quickStackTexture = getButtonTextures("quick_stack");
        Identifier sortInventoryTexture = getButtonTextures("sort_inventory");
        TexturedButtonWidget quickStackButton = new TexturedButtonWidget(
                x,
                y,
                20,
                18,
                0,
                0,
                18,
                quickStackTexture,
                20,
                36,
                onPress -> ClientNetworkHandler.sendActionPacket(StorageAction.QUICK_STACK_TO_NEARBY)
        );
        quickStackButton.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.quick_stack_to_nearby")));

        TexturedButtonWidget sortInventoryButton = new TexturedButtonWidget(
                x + 24,
                y,
                20,
                18,
                0,
                0,
                18,
                sortInventoryTexture,
                20,
                36,
                onPress -> ClientNetworkHandler.sendSortPacket(true)
        );
        sortInventoryButton.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.sort_inventory")));

        return new Pair<>(quickStackButton, sortInventoryButton);
    }

    private static Identifier getButtonTextures(String buttonName) {
        String stylePrefix = ClientConfigManager.getInstance().getConfig().getButtonsTextures().name().toLowerCase(Locale.ENGLISH);
        return new Identifier(Reference.MOD_ID, "textures/gui/sprites/" + stylePrefix + "_" + buttonName + ".png");
    }
}
