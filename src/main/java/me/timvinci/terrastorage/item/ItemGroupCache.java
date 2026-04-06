package me.timvinci.terrastorage.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;

/**
 * Caches the item group of items.
 */
public class ItemGroupCache {
    private static final Map<Item, CreativeModeTab> cache = new HashMap<>();
    private static final List<CreativeModeTab> filteredGroups = new ArrayList<>();

    /**
     * Populates the filteredGroups list with all item groups except for the search item group and empty groups.
     */
    public static void init() {
        CreativeModeTab searchGroup = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(CreativeModeTabs.SEARCH);

        for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
            if (!group.getDisplayItems().isEmpty() && group != searchGroup) {
                filteredGroups.add(group);
            }
        }
    }

    /**
     * Retrieves the item group from the cache, adding it if not already present.
     * @param item The item to check.
     * @return The group containing the item.
     */
    public static CreativeModeTab getGroup(Item item) {
        return cache.computeIfAbsent(item, ItemGroupCache::findGroup);
    }

    /**
     * Gets the item group of an item.
     * @param item The item.
     * @return The item group containing the item, or null if a group wasn't found.
     */
    private static CreativeModeTab findGroup(Item item) {
        ItemStack stack = item.getDefaultInstance();
        for (CreativeModeTab group : filteredGroups) {
            if (group.contains(stack)) {
                return group;
            }
        }

        return null;
    }
}
