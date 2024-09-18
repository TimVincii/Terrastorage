package me.timvinci.util;

/**
 * An enum class defining the buttons placement options.
 */
public enum ButtonsStyle {
    DEFAULT,
    TEXT_ONLY;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static ButtonsStyle next(ButtonsStyle current) {
        int index = (current.ordinal() + 1) % values().length;
        return values()[index];
    }
}
