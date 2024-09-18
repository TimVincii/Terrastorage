package me.timvinci.util;

import me.timvinci.inventory.CompactInventoryState;
import me.timvinci.inventory.CompleteInventoryState;
import me.timvinci.inventory.InventoryUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.List;

/**
 * Utility class that stores the implementation of the core options provided by Terrastorage.
 */
public class TerrastorageCore {

    /**
     * Attempts to loot all the items from the storage to the player.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void lootAll(PlayerInventory playerInventory, Inventory storageInventory, boolean hotbarProtection) {
        // Create an inventory state from the player's inventory.
        CompleteInventoryState playerInventoryState = new CompleteInventoryState(playerInventory, hotbarProtection);

        for (int i = 0; i < storageInventory.size(); i++) {
            ItemStack storageStack = storageInventory.getStack(i);
            if (storageStack.isEmpty()) {
                continue;
            }

            InventoryUtils.transferStack(playerInventory, playerInventoryState, storageStack);
        }

        if (playerInventoryState.wasModified()) {
            playerInventory.markDirty();
            storageInventory.markDirty();
        }
    }

    /**
     * Attempts to deposit all the items from the player to the storage.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param isStorageShulkerBox Whether the storage is a shulker box.
     */
    public static void depositAll(PlayerInventory playerInventory, Inventory storageInventory, boolean hotbarProtection, boolean isStorageShulkerBox) {
        // Create an inventory state from the storage's inventory.
        CompleteInventoryState storageInventoryState = new CompleteInventoryState(storageInventory);

        for (int i = PlayerInventory.getHotbarSize(); i < playerInventory.main.size(); i++) {
            ItemStack playerStack = playerInventory.getStack(i);
            if (playerStack.isEmpty() || (InventoryUtils.isShulkerBox(playerStack) && isStorageShulkerBox)) {
                continue;
            }

            InventoryUtils.transferStack(storageInventory, storageInventoryState, playerStack);
        }

        if (!hotbarProtection) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);

                if (playerStack.isEmpty() || (InventoryUtils.isShulkerBox(playerStack) && isStorageShulkerBox)) {
                    continue;
                }

                InventoryUtils.transferStack(storageInventory, storageInventoryState, playerStack);
            }
        }

        if (storageInventoryState.wasModified()) {
            playerInventory.markDirty();
            storageInventory.markDirty();
        }
    }

    /**
     * Attempts to deposit all the items of the player that can stack with existing items of the storage, from the
     * player to the storage.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void quickStack(PlayerInventory playerInventory, Inventory storageInventory, boolean hotbarProtection) {
        // Create a compact inventory state from the storage's inventory.
        CompactInventoryState storageInventoryState = new CompactInventoryState(storageInventory);

        int startIndex = hotbarProtection ? PlayerInventory.getHotbarSize() : 0;
        for (int i = startIndex; i < playerInventory.main.size(); i++) {
            ItemStack playerStack = playerInventory.getStack(i);
            if (playerStack.isEmpty() || !storageInventoryState.getNonFullItemSlots().containsKey(playerStack.getItem())) {
                continue;
            }

            InventoryUtils.transferToExistingStack(storageInventory, storageInventoryState, playerStack);
        }

        if (storageInventoryState.wasModified()) {
            playerInventory.markDirty();
            storageInventory.markDirty();
        }
    }

    /**
     * Attempts to loot all the items of the storage that can stack with existing items of the player, from the storage
     * to the player.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void restock(PlayerInventory playerInventory, Inventory storageInventory, boolean hotbarProtection) {
        // Create an inventory state from the player's inventory.
        CompactInventoryState playerInventoryState = new CompactInventoryState(playerInventory, hotbarProtection);

        for (int i = 0; i < storageInventory.size(); i++) {
            ItemStack storageStack = storageInventory.getStack(i);
            if (storageStack.isEmpty() || !playerInventoryState.getNonFullItemSlots().containsKey(storageStack.getItem())) {
                continue;
            }

            InventoryUtils.transferToExistingStack(playerInventory, playerInventoryState, storageStack);
        }

        if (playerInventoryState.wasModified()) {
            playerInventory.markDirty();
            storageInventory.markDirty();
        }
    }

    /**
     * Sorts the items of a storage.
     * @param storageInventory The storage's inventory.
     * @param type The sorting type of the player.
     */
    public static void sortStorageItems(Inventory storageInventory, SortType type) {
        List<ItemStack> sortedStacks = InventoryUtils.combineAndSortInventory(storageInventory, type, 0, storageInventory.size());

        int slotIndex = 0;
        for (ItemStack stack : sortedStacks) {
            storageInventory.setStack(slotIndex++, stack);
        }

        storageInventory.markDirty();
    }

    /**
     * Sorts the items of a player's inventory.
     * @param playerInventory The player's inventory.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void sortPlayerItems(PlayerInventory playerInventory, SortType type, boolean hotbarProtection) {
        List<ItemStack> sortedList = InventoryUtils.combineAndSortInventory(playerInventory, type, hotbarProtection ? PlayerInventory.getHotbarSize() : 0, playerInventory.main.size());
        ArrayDeque<ItemStack> sortedStacks = new ArrayDeque<>(sortedList);

        int slotIndex = PlayerInventory.getHotbarSize();
        while (!sortedStacks.isEmpty() && slotIndex < 36) {
            playerInventory.main.set(slotIndex++, sortedStacks.pollFirst());
        }
        if (!hotbarProtection && !sortedStacks.isEmpty()) {
            slotIndex = 0;
            do {
                playerInventory.main.set(slotIndex++, sortedStacks.pollFirst());
            }
            while (!sortedStacks.isEmpty());
        }

        playerInventory.markDirty();
    }

    /**
     * Attempts to deposit all the items of the player that can stack with existing items in nearby storages, from the
     * player to the storages.
     * @param player The player who initiated the operation.
     * @param hotbarProtection The player's hotbar protection value.
     */
    public static void quickStackToNearbyStorages(ServerPlayerEntity player, boolean hotbarProtection) {
        List<Pair<Inventory, Vec3d>> nearbyStorages = InventoryUtils.getNearbyStorages(player);
        if (nearbyStorages.isEmpty()) {
            return;
        }

        Map<Vec3d, ArrayList<Item>> animationMap = new HashMap<>();

        PlayerInventory playerInventory = player.getInventory();
        int startIndex = hotbarProtection ? PlayerInventory.getHotbarSize() : 0;
        boolean playerInventoryModified = false;

        for (Pair<Inventory, Vec3d> storagePair : nearbyStorages) {
            Inventory storage = storagePair.getKey();
            Vec3d storagePos = storagePair.getValue();
            CompactInventoryState storageState = new CompactInventoryState(storage);

            for (int i = startIndex; i < playerInventory.main.size(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);
                if (playerStack.isEmpty() || !storageState.getNonFullItemSlots().containsKey(playerStack.getItem())) {
                    continue;
                }

                Item playerItem = playerStack.getItem();
                InventoryUtils.transferToExistingStack(storage, storageState, playerStack);
                animationMap.computeIfAbsent(storagePos, k -> new ArrayList<>()).add(playerItem);
            }

            if (storageState.wasModified()) {
                storage.markDirty();
                playerInventoryModified = true;
            }
        }

        if (playerInventoryModified) {
            playerInventory.markDirty();
        }

        InventoryUtils.triggerFlyOutAnimation(player.getServerWorld(), player.getEyePos(), animationMap);
    }
}
