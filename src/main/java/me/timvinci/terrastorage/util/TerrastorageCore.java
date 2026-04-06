package me.timvinci.terrastorage.util;

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
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;

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
    public static void lootAll(Inventory playerInventory, Container storageInventory, boolean hotbarProtection) {
        // Create an inventory state from the player's inventory.
        CompleteInventoryState playerInventoryState = new CompleteInventoryState(playerInventory, hotbarProtection);

        for (int i = 0; i < storageInventory.getContainerSize(); i++) {
            ItemStack storageStack = storageInventory.getItem(i);
            if (storageStack.isEmpty()) {
                continue;
            }

            InventoryUtils.transferStack(playerInventory, playerInventoryState, storageStack);
        }

        if (playerInventoryState.wasModified()) {
            playerInventory.setChanged();
            storageInventory.setChanged();
        }
    }

    /**
     * Attempts to deposit all the items from the player to the storage.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param firstSlot The first slot of the screen handler of the storage inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void depositAll(Inventory playerInventory, Container storageInventory, Slot firstSlot, boolean hotbarProtection) {
        // Create an inventory state from the storage's inventory.
        CompleteInventoryState storageInventoryState = new CompleteInventoryState(storageInventory);

        for (int i = Inventory.getSelectionSize(); i < playerInventory.getNonEquipmentItems().size(); i++) {
            ItemStack playerStack = playerInventory.getItem(i);
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !firstSlot.mayPlace(playerStack)) {
                continue;
            }

            InventoryUtils.transferStack(storageInventory, storageInventoryState, playerStack);
        }

        if (!hotbarProtection) {
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack playerStack = playerInventory.getItem(i);

                if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack) || !firstSlot.mayPlace(playerStack)) {
                    continue;
                }

                InventoryUtils.transferStack(storageInventory, storageInventoryState, playerStack);
            }
        }

        if (storageInventoryState.wasModified()) {
            playerInventory.setChanged();
            storageInventory.setChanged();
        }
    }

    /**
     * Performs a quick stack operation on a storage inventory.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void quickStack(Inventory playerInventory, Container storageInventory, boolean hotbarProtection, boolean smartDepositMode) {
        InventoryState storageInventoryState = smartDepositMode ?
                new ExpandedInventoryState(storageInventory) :
                new CompactInventoryState(storageInventory);

        StackProcessor processor = InventoryUtils.createStackProcessor(storageInventoryState, storageInventory, smartDepositMode);

        int startIndex = hotbarProtection ? Inventory.getSelectionSize() : 0;
        for (int i = startIndex; i < playerInventory.getNonEquipmentItems().size(); i++) {
            processor.tryProcess(playerInventory.getItem(i));
        }

        if (storageInventoryState.wasModified()) {
            playerInventory.setChanged();
            storageInventory.setChanged();
        }
    }

    /**
     * Attempts to loot all the items of the storage that can stack with existing items of the player, from the storage
     * to the player.
     * @param playerInventory The player's inventory.
     * @param storageInventory The storage's inventory.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void restock(Inventory playerInventory, Container storageInventory, boolean hotbarProtection) {
        // Create an inventory state from the player's inventory.
        CompactInventoryState playerInventoryState = new CompactInventoryState(playerInventory, hotbarProtection);

        for (int i = 0; i < storageInventory.getContainerSize(); i++) {
            ItemStack storageStack = storageInventory.getItem(i);
            if (storageStack.isEmpty() || !playerInventoryState.getNonFullItemSlots().containsKey(new StackIdentifier(storageStack))) {
                continue;
            }

            InventoryUtils.transferToExistingStack(playerInventory, playerInventoryState, storageStack);
        }

        if (playerInventoryState.wasModified()) {
            playerInventory.setChanged();
            storageInventory.setChanged();
        }
    }

    /**
     * Sorts the items of a storage.
     * @param storageInventory The storage's inventory.
     * @param type The sorting type of the player.
     */
    public static void sortStorageItems(Container storageInventory, SortType type) {
        List<ItemStack> sortedStacks = InventoryUtils.combineAndSortInventory(storageInventory, type, 0, storageInventory.getContainerSize(), false);

        int slotIndex = 0;
        for (ItemStack stack : sortedStacks) {
            storageInventory.setItem(slotIndex++, stack);
        }

        storageInventory.setChanged();
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

                ServerLevel world = player.level();
                NetworkHandler.sendGlobalBlockRenamedPayload(world, firstPart.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
                NetworkHandler.sendGlobalBlockRenamedPayload(world, secondPart.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
                factory = firstPart.getBlockState().getMenuProvider(world, firstPart.getBlockPos());
            }
            else {
                player.sendSystemMessage(Component.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
                return;
            }
        }
        else if (containerInventory instanceof BaseContainerBlockEntity baseContainerBlockEntity) {
            BaseContainerBlockEntityAccessor accessor = (BaseContainerBlockEntityAccessor) baseContainerBlockEntity;

            if (newName.equals(accessor.invokeGetDefaultName().getString())) {
                newCustomName = null;
            }

            accessor.setName(newCustomName);
            baseContainerBlockEntity.setChanged();

            ServerLevel world = player.level();
            NetworkHandler.sendGlobalBlockRenamedPayload(world, baseContainerBlockEntity.getBlockPos(), newCustomName == null ? "" : newCustomName.getString());
            factory = baseContainerBlockEntity.getBlockState().getMenuProvider(world, baseContainerBlockEntity.getBlockPos());
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
     * @param playerInventory The player's inventory.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void sortPlayerItems(Inventory playerInventory, SortType type, boolean hotbarProtection) {
        List<ItemStack> sortedList = InventoryUtils.combineAndSortInventory(playerInventory, type,
                hotbarProtection ? Inventory.getSelectionSize() : 0,
                playerInventory.getNonEquipmentItems().size(), true);
        ArrayDeque<ItemStack> sortedStacks = new ArrayDeque<>(sortedList);

        int slotIndex = Inventory.getSelectionSize();
        while (!sortedStacks.isEmpty() && slotIndex < 36) {
            if (playerInventory.getNonEquipmentItems().get(slotIndex).isEmpty()) {
                playerInventory.getNonEquipmentItems().set(slotIndex, sortedStacks.pollFirst());
            }
            slotIndex++;
        }
        if (!hotbarProtection && !sortedStacks.isEmpty()) {
            slotIndex = 0;
            do {
                if (playerInventory.getNonEquipmentItems().get(slotIndex).isEmpty()) {
                    playerInventory.getNonEquipmentItems().set(slotIndex, sortedStacks.pollFirst());
                }
                slotIndex++;
            }
            while (!sortedStacks.isEmpty());
        }

        playerInventory.setChanged();
    }

    /**
     * Performs a quick stack operation on all storage nearby the player.
     * @param player The player who initiated the operation.
     * @param hotbarProtection The player's hotbar protection value.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void quickStackToNearbyStorages(ServerPlayer player, boolean hotbarProtection, boolean smartDepositMode) {
        List<Tuple<Container, Vec3>> nearbyStorages = InventoryUtils.getNearbyStorages(player);
        if (nearbyStorages.isEmpty()) {
            return;
        }

        Function<Container, InventoryState> stateFactory = InventoryUtils.getInventoryStateFactory(smartDepositMode);
        Map<Vec3, ArrayList<Item>> animationMap = new HashMap<>();

        Inventory playerInventory = player.getInventory();
        int startIndex = hotbarProtection ? Inventory.getSelectionSize() : 0;
        boolean playerInventoryModified = false;

        for (Tuple<Container, Vec3> storagePair : nearbyStorages) {
            Container storage = storagePair.getA();
            Vec3 storagePos = storagePair.getB();

            InventoryState storageState = stateFactory.apply(storage);
            StackProcessor processor = InventoryUtils.createStackProcessor(storageState, storage, smartDepositMode);

            for (int i = startIndex; i < playerInventory.getNonEquipmentItems().size(); i++) {
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
            InventoryUtils.triggerFlyOutAnimation(player.level(), player.getEyePosition(), itemAnimationLength, animationMap);
        }
    }
}
