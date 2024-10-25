package me.timvinci.terrastorage.util;

/**
 * An enum class defining the buttons style options.
 */
public enum ButtonsPlacement {
    RIGHT,
    LEFT;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static ButtonsPlacement next(ButtonsPlacement current) {
        int index = (current.ordinal() + 1) % values().length;
        return values()[index];
    }
}
