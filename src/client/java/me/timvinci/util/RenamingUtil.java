package me.timvinci.util;

import me.timvinci.gui.RenameScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.text.Text;

/**
 * Utility class for renaming related logic.
 */
public class RenamingUtil {

    /**
     * Gets the name of the storage and opens the rename screen.
     */
    public static void getNameAndOpenScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        String name = client.currentScreen.getTitle().getString();
        client.execute(() -> {
            client.setScreen(new RenameScreen(client.currentScreen, name));
        });
    }

    /**
     * Updates the title of a screen.
     * @param client The MinecraftClient instance.
     * @param newTitle The new title.
     */
    public static void updateScreenTitle(MinecraftClient client, String newTitle) {
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof GenericContainerScreen genericContainerScreen) {
            client.setScreen(new GenericContainerScreen(genericContainerScreen.getScreenHandler(), client.player.getInventory(), Text.literal(newTitle)));
        }
        else if (currentScreen instanceof ShulkerBoxScreen shulkerBoxScreen) {
            client.setScreen(new ShulkerBoxScreen(shulkerBoxScreen.getScreenHandler(), client.player.getInventory(), Text.literal(newTitle)));
        }
    }
}
