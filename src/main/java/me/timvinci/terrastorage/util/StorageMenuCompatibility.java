package me.timvinci.terrastorage.util;

import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Arrays;

/**
 * Compatibility checks for container menus that look like large storages but own custom slot rules.
 */
public final class StorageMenuCompatibility {
    private static final String BACKPACKED_MENU_PACKAGE = "com.mrcrayfish.backpacked.inventory.container.";
    private static final String BACKPACKED_BACKPACK_MENU = BACKPACKED_MENU_PACKAGE + "BackpackContainerMenu";
    private static final String BACKPACKED_BACKPACK_MENU_PREFIX = BACKPACKED_MENU_PACKAGE + "Backpack";

    private StorageMenuCompatibility() {}

    public static boolean shouldSkipStorageActions(AbstractContainerMenu menu) {
        if (menu == null) {
            return false;
        }

        String menuClassName = menu.getClass().getName();
        return menuClassName.startsWith(BACKPACKED_BACKPACK_MENU_PREFIX) && !menuClassName.equals(BACKPACKED_BACKPACK_MENU);
    }

    public static boolean shouldUseSlotBackedStorage(AbstractContainerMenu menu) {
        return menu != null && menu.getClass().getName().equals(BACKPACKED_BACKPACK_MENU);
    }

    public static StorageAction[] getCompatibleButtonActions(AbstractContainerMenu menu, boolean isEnderChest) {
        StorageAction[] actions = StorageAction.getButtonsActions(isEnderChest);
        if (!shouldUseSlotBackedStorage(menu)) {
            return actions;
        }

        return Arrays.stream(actions)
                .filter(action -> action != StorageAction.RENAME)
                .toArray(StorageAction[]::new);
    }
}
