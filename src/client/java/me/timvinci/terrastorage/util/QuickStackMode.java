package me.timvinci.terrastorage.util;

/**
 * An enum class defining the quick stacking modes.
 */
public enum QuickStackMode {
    FILL_UP,
    SMART_DEPOSIT;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static QuickStackMode next(QuickStackMode current) {
        int index = (current.ordinal() + 1) % values().length;
        // Very weird way to change the tooltip of the quick stack button, but outweighs the other options so...
        LocalizedTextProvider.updateQuickStackTooltip(values()[index]);
        return values()[index];
    }
}
