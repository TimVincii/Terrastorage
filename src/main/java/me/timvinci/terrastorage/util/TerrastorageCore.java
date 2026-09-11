package me.timvinci.terrastorage.util;

import com.mojang.datafixers.util.Pair;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.inventory.*;
import me.timvinci.terrastorage.item.StackIdentifier;
import me.timvinci.terrastorage.item.StackProcessor;
import me.timvinci.terrastorage.mixin.CompoundContainerAccessor;
import me.timvinci.terrastorage.mixin.EntityAccessor;
import me.timvinci.terrastorage.mixin.BaseContainerBlockEntityAccessor;
import me.timvinci.terrastorage.network.NetworkHandler;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Utility class that stores the implementation of the core options provided by Terrastorage.
 * Storages are accessed exclusively through a {@link StorageAccess}, and the player's inventory through a
 * {@link PlayerSlotAccess}, so that every operation respects the rules of both the slots it takes from and the slots
 * it places into.
 */
public class TerrastorageCore {

    /**
     * Attempts to loot all the items from the storage to the player.
     * @param player The player performing the operation.
     * @param storage The storage access.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void lootAll(ServerPlayer player, StorageAccess storage, boolean hotbarProtection) {
        Inventory playerInventory = player.getInventory();
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);
        // Create an inventory state from the player's inventory (the receiver).
        CompleteInventoryState playerInventoryState = new CompleteInventoryState(playerAccess, hotbarProtection);

        boolean modified = false;
        for (int i = 0; i < storage.size(); i++) {
            if (!storage.canTake(i, player)) {
                continue;
            }

            ItemStack storageStack = storage.get(i);
            if (storageStack.isEmpty()) {
                continue;
            }

            // Work on a copy; transferStack shrinks it by whatever the player could accept.
            ItemStack working = storageStack.copy();
            int before = working.getCount();
            InventoryUtils.transferStack(playerAccess, playerInventoryState, working);

            int moved = before - working.getCount();
            if (moved > 0) {
                storage.take(i, moved, player);
                modified = true;
            }
        }

        if (modified) {
            playerInventory.setChanged();
        }
    }

    /**
     * Attempts to deposit all the items from the player to the storage.
     * @param player The player performing the operation.
     * @param storage The storage access.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void depositAll(ServerPlayer player, StorageAccess storage, boolean hotbarProtection) {
        Inventory playerInventory = player.getInventory();
        StorageState storageState = new StorageState(storage);
        // Respect the source-side pickup rules: never deposit an item the player isn't allowed to take from its slot
        // (e.g. the currently-open backpack, which must not be placed inside itself).
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);

        for (int i = Inventory.getSelectionSize(); i < playerInventory.items.size(); i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !playerAccess.canTake(i)) {
                continue;
            }

            // playerStack is the player's live stack; inserting shrinks it, removing the deposited items.
            InventoryUtils.insertIntoStorage(storage, storageState, playerStack);
        }

        if (!hotbarProtection) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack playerStack = playerInventory.getItem(i);
                if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !playerAccess.canTake(i)) {
                    continue;
                }

                InventoryUtils.insertIntoStorage(storage, storageState, playerStack);
            }
        }

        if (storageState.wasModified()) {
            playerInventory.setChanged();
        }
    }

    /**
     * Performs a quick stack operation on a storage.
     * @param player The player performing the operation.
     * @param storage The storage access.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void quickStack(ServerPlayer player, StorageAccess storage, boolean hotbarProtection, boolean smartDepositMode) {
        Inventory playerInventory = player.getInventory();
        StorageState storageState = new StorageState(storage);
        StackProcessor processor = InventoryUtils.createStorageStackProcessor(storage, storageState, smartDepositMode);
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);

        int startIndex = hotbarProtection ? Inventory.getSelectionSize() : 0;
        for (int i = startIndex; i < playerInventory.items.size(); i++) {
            if (!playerAccess.canTake(i)) {
                continue;
            }

            processor.tryProcess(playerInventory.getItem(i));
        }

        if (storageState.wasModified()) {
            playerInventory.setChanged();
        }
    }


    /**
     * Attempts to loot all the items of the storage that can stack with existing items of the player, from the storage
     * to the player.
     * @param player The player performing the operation.
     * @param storage The storage access.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void restock(ServerPlayer player, StorageAccess storage, boolean hotbarProtection) {
        Inventory playerInventory = player.getInventory();
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);
        // Create an inventory state from the player's inventory (the receiver).
        CompactInventoryState playerInventoryState = new CompactInventoryState(playerAccess, hotbarProtection);

        boolean modified = false;
        for (int i = 0; i < storage.size(); i++) {
            if (!storage.canTake(i, player)) {
                continue;
            }

            ItemStack storageStack = storage.get(i);
            if (storageStack.isEmpty() || !playerInventoryState.getNonFullItemSlots().containsKey(new StackIdentifier(storageStack))) {
                continue;
            }

            ItemStack working = storageStack.copy();
            int before = working.getCount();
            InventoryUtils.transferToExistingStack(playerAccess, playerInventoryState, working);

            int moved = before - working.getCount();
            if (moved > 0) {
                storage.take(i, moved, player);
                modified = true;
            }
        }

        if (modified) {
            playerInventory.setChanged();
        }
    }

    /**
     * Sorts the items of a storage.
     * Only slots the player may take from are sorted, so special slots such as upgrade or tool slots exclude
     * themselves via their own rules. If anything can't be placed back, the original contents are restored.
     * @param player The player performing the operation.
     * @param storage The storage access.
     * @param type The sorting type of the player.
     */
    public static void sortStorageItems(ServerPlayer player, StorageAccess storage, SortType type) {
        List<Integer> sortableSlots = new ArrayList<>();
        for (int i = 0; i < storage.size(); i++) {
            if (storage.canTake(i, player)) {
                sortableSlots.add(i);
            }
        }
        if (sortableSlots.isEmpty()) {
            return;
        }

        // Snapshot the sortable slots (for restore-on-failure) and gather their items.
        List<ItemStack> snapshot = new ArrayList<>(sortableSlots.size());
        List<ItemStack> gathered = new ArrayList<>();
        for (int index : sortableSlots) {
            ItemStack stack = storage.get(index);
            snapshot.add(stack.copy());
            if (!stack.isEmpty()) {
                gathered.add(stack.copy());
            }
        }

        // Clear the sortable slots.
        for (int index : sortableSlots) {
            ItemStack stack = storage.get(index);
            if (!stack.isEmpty()) {
                storage.take(index, stack.getCount(), player);
            }
        }

        // Redistribute the sorted, merged stacks. safeInsert splits each oversized stack across slots by capacity.
        Deque<ItemStack> sorted = new ArrayDeque<>(InventoryUtils.combineForStorageSort(gathered, type));
        for (int index : sortableSlots) {
            if (sorted.isEmpty()) {
                break;
            }

            ItemStack head = sorted.peekFirst();
            storage.insert(index, head);
            if (head.isEmpty()) {
                sorted.pollFirst();
            }
        }

        if (!sorted.isEmpty()) {
            restoreStorageSnapshot(storage, sortableSlots, snapshot, player);
        }
    }

    /**
     * Restores a set of storage slots to a previously captured snapshot, used when a sort could not be completed.
     * @param storage The storage access.
     * @param slots The slot indexes that were affected.
     * @param snapshot The captured stacks, parallel to the slots.
     * @param player The player performing the operation.
     */
    private static void restoreStorageSnapshot(StorageAccess storage, List<Integer> slots, List<ItemStack> snapshot, ServerPlayer player) {
        for (int index : slots) {
            ItemStack stack = storage.get(index);
            if (!stack.isEmpty()) {
                storage.take(index, stack.getCount(), player);
            }
        }

        for (int k = 0; k < slots.size(); k++) {
            ItemStack original = snapshot.get(k);
            if (!original.isEmpty()) {
                storage.insert(slots.get(k), original.copy());
            }
        }
    }

    /**
     * Handles the renaming of an entity or block entity that the player is interacting with.
     * Updates the name of the entity or block entity and sends the new name to all players tracking it.
     * Also reopens the screen for the player who initiated the rename action.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void renameStorage(ServerPlayer player, String newName) {
        Component newCustomName = newName.isEmpty() ? null : Component.literal(newName);
        MenuProvider factory;
        Container containerInventory = player.containerMenu.slots.getFirst().container;
        if (containerInventory instanceof ContainerEntity vehicleInventory) {
            Entity entity = (Entity) vehicleInventory;
            if (newName.equals(((EntityAccessor)entity).invokeGetTypeName().getString())) {
                newCustomName = null;
            }

            entity.setCustomName(newCustomName);
            factory = (MenuProvider) entity;
        }
        else if (containerInventory instanceof CompoundContainerAccessor accessor) {
            if (accessor.Container1() instanceof BaseContainerBlockEntity firstPart &&
                    accessor.Container2() instanceof BaseContainerBlockEntity secondPart) {

                String containerName = ((BaseContainerBlockEntityAccessor)firstPart).invokeGetDefaultName().getString();
                String doubleContainerName = Component.translatable("container.chestDouble").getString().replace(Component.translatable("container.chest").getString(), containerName);
                if (newName.equals(doubleContainerName)) {
                    newCustomName = null;
                }


                ((BaseContainerBlockEntityAccessor) firstPart).setName(newCustomName);
                ((BaseContainerBlockEntityAccessor) secondPart).setName(newCustomName);

                firstPart.setChanged();
                secondPart.setChanged();

                NetworkHandler.sendGlobalBlockRenamedPayload(player.serverLevel(), firstPart.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
                NetworkHandler.sendGlobalBlockRenamedPayload(player.serverLevel(), secondPart.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
                factory = firstPart.getBlockState().getMenuProvider(player.level(), firstPart.getBlockPos());
            }
            else {
                player.sendSystemMessage(Component.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
                return;
            }
        }
        else if (containerInventory instanceof BaseContainerBlockEntity lockableContainerBlockEntity) {
            BaseContainerBlockEntityAccessor accessor = (BaseContainerBlockEntityAccessor) lockableContainerBlockEntity;

            if (newName.equals(accessor.invokeGetDefaultName().getString())) {
                newCustomName = null;
            }

            accessor.setName(newCustomName);
            lockableContainerBlockEntity.setChanged();

            NetworkHandler.sendGlobalBlockRenamedPayload(player.serverLevel(), lockableContainerBlockEntity.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
            factory = lockableContainerBlockEntity.getBlockState().getMenuProvider(player.level(), lockableContainerBlockEntity.getBlockPos());
        }
        else {
            player.sendSystemMessage(Component.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
            return;
        }

        player.closeContainer();
        player.openMenu(factory);
    }

    /**
     * Sorts the items of a player's inventory.
     * Items are only gathered from slots the player may take from, and only placed into slots that accept them, so a
     * slot that a mod has locked keeps its item instead of being shuffled elsewhere. If anything can't be placed
     * back, the inventory is restored to its pre-sort state.
     * @param player The player performing the operation.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void sortPlayerItems(ServerPlayer player, SortType type, boolean hotbarProtection) {
        Inventory playerInventory = player.getInventory();
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);
        int size = playerInventory.items.size();

        // Gather the sortable slots, snapshotting them so the whole sort can be rolled back.
        List<Integer> gatheredSlots = new ArrayList<>();
        List<ItemStack> snapshot = new ArrayList<>();
        List<ItemStack> gathered = new ArrayList<>();
        for (int i = hotbarProtection ? Inventory.getSelectionSize() : 0; i < size; i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !playerAccess.canTake(i)) {
                continue;
            }

            gatheredSlots.add(i);
            snapshot.add(playerStack.copy());
            gathered.add(playerStack.copy());
            playerInventory.setItem(i, ItemStack.EMPTY);
        }

        if (gathered.isEmpty()) {
            return;
        }

        // Redistribute over the main inventory first and then the hotbar, matching the established sort layout.
        Deque<ItemStack> sortedStacks = new ArrayDeque<>(InventoryUtils.combineAndSort(gathered, type));
        List<Integer> filledSlots = new ArrayList<>();
        distributeSortedStacks(playerAccess, playerInventory, sortedStacks, filledSlots, Inventory.getSelectionSize(), size);
        if (!hotbarProtection) {
            distributeSortedStacks(playerAccess, playerInventory, sortedStacks, filledSlots, 0, Inventory.getSelectionSize());
        }

        // Everything gathered came out of these slots, so it should always fit back. If a slot refuses it anyway,
        // undo the sort rather than lose the leftovers.
        if (!sortedStacks.isEmpty()) {
            for (int index : filledSlots) {
                playerInventory.setItem(index, ItemStack.EMPTY);
            }
            for (int k = 0; k < gatheredSlots.size(); k++) {
                playerInventory.setItem(gatheredSlots.get(k), snapshot.get(k));
            }
        }

        playerInventory.setChanged();
    }

    /**
     * Places sorted stacks into the empty slots of a player inventory range that accept them, recording which slots
     * were filled so that the operation can be undone.
     * @param access The rule aware view of the player's inventory.
     * @param playerInventory The player's inventory.
     * @param sortedStacks The remaining sorted stacks, polled as they are placed.
     * @param filledSlots Collects the indices that received a stack.
     * @param startIndex The index at which placement starts.
     * @param endIndex The index at which placement ends.
     */
    private static void distributeSortedStacks(PlayerSlotAccess access, Inventory playerInventory, Deque<ItemStack> sortedStacks, List<Integer> filledSlots, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex && !sortedStacks.isEmpty(); i++) {
            if (!playerInventory.getItem(i).isEmpty() || !access.canPlace(i, sortedStacks.peekFirst())) {
                continue;
            }

            playerInventory.setItem(i, sortedStacks.pollFirst());
            filledSlots.add(i);
        }
    }

    /**
     * Performs a quick stack operation on all storage nearby the player.
     * Nearby storages have no open menu, and so no slots to enforce rules, and are accessed through a
     * {@link ContainerStorageAccess} instead.
     * @param player The player who initiated the operation.
     * @param hotbarProtection The player's hotbar protection value.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void quickStackToNearbyStorages(ServerPlayer player, boolean hotbarProtection, boolean smartDepositMode) {
        List<Pair<Container, Vec3>> nearbyStorages = InventoryUtils.getNearbyStorages(player);
        if (nearbyStorages.isEmpty()) {
            return;
        }

        Map<Vec3, ArrayList<Item>> animationMap = new HashMap<>();

        Inventory playerInventory = player.getInventory();
        int startIndex = hotbarProtection ? Inventory.getSelectionSize() : 0;
        PlayerSlotAccess playerAccess = new PlayerSlotAccess(player.containerMenu, player);
        boolean playerInventoryModified = false;

        for (Pair<Container, Vec3> storagePair : nearbyStorages) {
            Container storage = storagePair.getFirst();
            Vec3 storagePos = storagePair.getSecond();

            StorageAccess access = new ContainerStorageAccess(storage);
            StorageState storageState = new StorageState(access);
            StackProcessor processor = InventoryUtils.createStorageStackProcessor(access, storageState, smartDepositMode);

            for (int i = startIndex; i < playerInventory.items.size(); i++) {
                if (!playerAccess.canTake(i)) {
                    continue;
                }

                ItemStack playerStack = playerInventory.getItem(i);
                Item playerItem = playerStack.getItem();
                if (processor.tryProcess(playerStack)) {
                    animationMap.computeIfAbsent(storagePos, k -> new ArrayList<>()).add(playerItem);
                }
            }

            if (storageState.wasModified()) {
                storage.setChanged();
                playerInventoryModified = true;
            }
        }

        if (playerInventoryModified) {
            playerInventory.setChanged();
        }


        int itemAnimationLength = ConfigManager.getInstance().getConfig().getItemAnimationLength();
        if (itemAnimationLength != 0) {
            InventoryUtils.triggerFlyOutAnimation(player.serverLevel(), player.getEyePosition(), itemAnimationLength, animationMap);
        }
    }
}
