package me.timvinci.terrastorage.util;

/**
 * An enum class defining the sorting types.
 */
public enum SortType {
    ITEM_GROUP,
    ITEM_COUNT,
    ITEM_RARITY,
    ITEM_NAME,
    ITEM_ID;

    /**
     * Gets the next enum constant after the one provided.
     * @param current The current constant
     * @return The next constant.
     */
    public static SortType next(SortType current) {
        int index = (current.ordinal() + 1) % values().length;
        return values()[index];
    }
}
