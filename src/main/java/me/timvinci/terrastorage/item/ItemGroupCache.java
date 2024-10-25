package me.timvinci.terrastorage.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.*;

/**
 * Caches the item group of items.
 */
public class ItemGroupCache {
    private static final Map<Item, ItemGroup> cache = new HashMap<>();
    private static final List<ItemGroup> filteredGroups = new ArrayList<>();

    /**
     * Populates the filteredGroups list with all item groups except for the search item group and empty groups.
     */
    public static void init() {
        ItemGroup searchGroup = Registries.ITEM_GROUP.get(ItemGroups.SEARCH);

        for (ItemGroup group : ItemGroups.getGroups()) {
            if (!group.getDisplayStacks().isEmpty() && group != searchGroup) {
                filteredGroups.add(group);
            }
        }
    }

    /**
     * Retrieves the item group from the cache, adding it if not already present.
     * @param item The item to check.
     * @return The group containing the item.
     */
    public static ItemGroup getGroup(Item item) {
        return cache.computeIfAbsent(item, ItemGroupCache::findGroup);
    }

    /**
     * Gets the item group of an item.
     * @param item The item.
     * @return The item group containing the item, or null if a group wasn't found.
     */
    private static ItemGroup findGroup(Item item) {
        ItemStack stack = item.getDefaultStack();
        for (ItemGroup group : filteredGroups) {
            if (group.contains(stack)) {
                return group;
            }
        }

        return null;
    }
}
