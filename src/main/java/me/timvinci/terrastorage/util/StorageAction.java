package me.timvinci.terrastorage.util;

import java.util.Arrays;

/**
 * An enum class defining the storage actions.
 */
public enum StorageAction {
    LOOT_ALL,
    DEPOSIT_ALL,
    QUICK_STACK,
    RESTOCK,
    SORT_ITEMS,
    RENAME,
    QUICK_STACK_TO_NEARBY;

    public static StorageAction[] getButtonsActions(boolean isEnderChest) {
        StorageAction[] allActions = values();
        return Arrays.copyOf(allActions, allActions.length - (isEnderChest ? 2 : 1));
    }
}

