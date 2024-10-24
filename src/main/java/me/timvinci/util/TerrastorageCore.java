package me.timvinci.util;

import me.timvinci.api.ItemFavoritingUtils;
import me.timvinci.inventory.CompactInventoryState;
import me.timvinci.inventory.CompleteInventoryState;
import me.timvinci.inventory.InventoryUtils;
import me.timvinci.mixin.DoubleInventoryAccessor;
import me.timvinci.mixin.EntityAccessor;
import me.timvinci.mixin.LockableContainerBlockEntityAccessor;
import me.timvinci.network.NetworkHandler;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

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
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || (InventoryUtils.isShulkerBox(playerStack) && isStorageShulkerBox))  {
                continue;
            }

            InventoryUtils.transferStack(storageInventory, storageInventoryState, playerStack);
        }

        if (!hotbarProtection) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);

                if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || (InventoryUtils.isShulkerBox(playerStack) && isStorageShulkerBox)) {
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
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !storageInventoryState.getNonFullItemSlots().containsKey(playerStack.getItem())) {
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
        List<ItemStack> sortedStacks = InventoryUtils.combineAndSortInventory(storageInventory, type, 0, storageInventory.size(), false);

        int slotIndex = 0;
        for (ItemStack stack : sortedStacks) {
            storageInventory.setStack(slotIndex++, stack);
        }

        storageInventory.markDirty();
    }

    /**
     * Handles the renaming of an entity or block entity that the player is interacting with.
     * Updates the name of the entity or block entity and sends the new name to all players tracking it.
     * Also reopens the screen for the player who initiated the rename action.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void renameStorage(ServerPlayerEntity player, String newName) {
        Text newCustomName = newName.isEmpty() ? null : Text.literal(newName);
        NamedScreenHandlerFactory factory;
        Inventory containerInventory = player.currentScreenHandler.slots.get(0).inventory;
        if (containerInventory instanceof VehicleInventory vehicleInventory) {
            Entity entity = (Entity) vehicleInventory;
            if (newName.equals(((EntityAccessor)entity).invokeGetDefaultName().getString())) {
                newCustomName = null;
            }

            entity.setCustomName(newCustomName);
            factory = (NamedScreenHandlerFactory) entity;
        }
        else if (containerInventory instanceof DoubleInventoryAccessor accessor) {
            if (accessor.first() instanceof LockableContainerBlockEntity firstPart &&
                    accessor.second() instanceof LockableContainerBlockEntity secondPart) {

                if (newName.equals("Large " + ((LockableContainerBlockEntityAccessor) firstPart).invokeGetContainerName().getString())) {
                    newCustomName = null;
                }

                ((LockableContainerBlockEntityAccessor) firstPart).setCustomName(newCustomName);
                ((LockableContainerBlockEntityAccessor) secondPart).setCustomName(newCustomName);

                firstPart.markDirty();
                secondPart.markDirty();

                NetworkHandler.sendGlobalBlockRenamedPacket(player.getServerWorld(), firstPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
                NetworkHandler.sendGlobalBlockRenamedPacket(player.getServerWorld(), secondPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
                factory = firstPart.getCachedState().createScreenHandlerFactory(player.getWorld(), firstPart.getPos());
            }
            else {
                player.sendMessage(Text.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
                return;
            }
        }
        else if (containerInventory instanceof LockableContainerBlockEntity lockableContainerBlockEntity) {
            LockableContainerBlockEntityAccessor accessor = (LockableContainerBlockEntityAccessor) lockableContainerBlockEntity;

            if (newName.equals(accessor.invokeGetContainerName().getString())) {
                newCustomName = null;
            }

            accessor.setCustomName(newCustomName);
            lockableContainerBlockEntity.markDirty();

            NetworkHandler.sendGlobalBlockRenamedPacket(player.getServerWorld(), lockableContainerBlockEntity.getPos(), newCustomName == null ? "" : newCustomName.getString());
            factory = lockableContainerBlockEntity.getCachedState().createScreenHandlerFactory(player.getWorld(), lockableContainerBlockEntity.getPos());
        }
        else {
            player.sendMessage(Text.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
            return;
        }

        player.closeHandledScreen();
        player.openHandledScreen(factory);
    }

    /**
     * Sorts the items of a player's inventory.
     * @param playerInventory The player's inventory.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void sortPlayerItems(PlayerInventory playerInventory, SortType type, boolean hotbarProtection) {
        List<ItemStack> sortedList = InventoryUtils.combineAndSortInventory(playerInventory, type, hotbarProtection ? PlayerInventory.getHotbarSize() : 0, playerInventory.main.size(), true);
        ArrayDeque<ItemStack> sortedStacks = new ArrayDeque<>(sortedList);

        int slotIndex = PlayerInventory.getHotbarSize();
        while (!sortedStacks.isEmpty() && slotIndex < 36) {
            if (playerInventory.main.get(slotIndex).isEmpty()) {
                playerInventory.main.set(slotIndex, sortedStacks.pollFirst());
            }
            slotIndex++;
        }
        if (!hotbarProtection && !sortedStacks.isEmpty()) {
            slotIndex = 0;
            do {
                if (playerInventory.main.get(slotIndex).isEmpty()) {
                    playerInventory.main.set(slotIndex, sortedStacks.pollFirst());
                }
                slotIndex++;
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
            Inventory storage = storagePair.getLeft();
            Vec3d storagePos = storagePair.getRight();
            CompactInventoryState storageState = new CompactInventoryState(storage);

            for (int i = startIndex; i < playerInventory.main.size(); i++) {
                ItemStack playerStack = playerInventory.getStack(i);
                if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !storageState.getNonFullItemSlots().containsKey(playerStack.getItem())) {
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
