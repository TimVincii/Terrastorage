package me.timvinci.terrastorage.util;

/**
 * An enum class defining the border visibility options.
 */
public enum BorderVisibility {
    ALWAYS,
    ON_PRESS,
    NON_HOTBAR,
    ON_PRESS_NON_HOTBAR,
    NEVER;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static BorderVisibility next(BorderVisibility current) {
        int index = (current.ordinal() + 1) % values().length;
        return values()[index];
    }
}
