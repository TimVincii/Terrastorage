package me.timvinci.terrastorage.keybinding;

import me.timvinci.terrastorage.util.Reference;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Holds the keybindings used by Terrastorage, and a method for registering them.
 */
public class TerrastorageKeybindings {
    private static final KeyMapping.Category terrastorage_main = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "keybinding_main"));
    public static KeyMapping favoriteItemModifier;
    public static KeyMapping sortInventoryBind;

    public static void registerKeybindings() {
        favoriteItemModifier = new KeyMapping(
            "terrastorage.keybinding.favorite_item_modifier",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                terrastorage_main
        );

        KeyBindingHelper.registerKeyBinding(favoriteItemModifier);

        sortInventoryBind = new KeyMapping(
                "terrastorage.keybinding.sort_inventory_bind",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                terrastorage_main
        );

        KeyBindingHelper.registerKeyBinding(sortInventoryBind);
    }
}
