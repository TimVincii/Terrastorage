package me.timvinci.terrastorage.gui.widget;

import me.timvinci.terrastorage.gui.RenameScreen;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

/**
 * Creates the buttons used by Terrastorage.
 */
public class StorageButtonCreator {
    public static final Identifier sortButtonTexture = new Identifier(Reference.MOD_ID, "textures/gui/sprites/sort_inventory.png");
    public static final Identifier quickStackButtonTexture = new Identifier(Reference.MOD_ID, "textures/gui/sprites/quick_stack.png");

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

    /**
     * Creates the Sort Inventory and Quick Stack To Nearby Storages buttons.
     * @param x The x position.
     * @param y The y position.
     * @return A pair of the inventory TexturedButtonWidgets.
     */
    public static Pair<TexturedButtonWidget, TexturedButtonWidget> createInventoryButtons(int x, int y) {
        TexturedButtonWidget quickStackButton = new TexturedButtonWidget(
                x,
                y,
                20,
                18,
                0,
                0,
                18,
                quickStackButtonTexture,
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
                sortButtonTexture,
                20,
                36,
                onPress -> ClientNetworkHandler.sendPlayerSortPacket()
        );
        sortInventoryButton.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.sort_inventory")));

        return new Pair<>(quickStackButton, sortInventoryButton);
    }
}