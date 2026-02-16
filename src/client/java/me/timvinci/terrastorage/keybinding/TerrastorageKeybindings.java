package me.timvinci.terrastorage.keybinding;

import me.timvinci.terrastorage.util.Reference;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Holds the keybindings used by Terrastorage, and a method for registering them.
 */
public class TerrastorageKeybindings {
    private static final KeyBinding.Category terrastorage_main = KeyBinding.Category.create(Identifier.of(Reference.MOD_ID, "keybinding_main"));
    public static KeyBinding favoriteItemModifier;
    public static KeyBinding sortInventoryBind;

    public static void registerKeybindings() {
        favoriteItemModifier = new KeyBinding(
            "terrastorage.keybinding.favorite_item_modifier",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                terrastorage_main
        );

        KeyBindingHelper.registerKeyBinding(favoriteItemModifier);

        sortInventoryBind = new KeyBinding(
                "terrastorage.keybinding.sort_inventory_bind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                terrastorage_main
        );

        KeyBindingHelper.registerKeyBinding(sortInventoryBind);
    }
}
