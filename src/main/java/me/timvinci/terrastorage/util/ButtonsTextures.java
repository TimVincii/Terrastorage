package me.timvinci.terrastorage.util;

/**
 * An enum class defining the textures for the inventory storage option buttons.
 */
public enum ButtonsTextures {
    TERRARIA,
    MINECRAFT;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static ButtonsTextures next(ButtonsTextures current) {
        int index = (current.ordinal() + 1) % values().length;
        return values()[index];
    }
}
