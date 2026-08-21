package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.inventory.SlotBackedInventory;
import me.timvinci.terrastorage.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Provides a method for registering packet receivers and holds packet processing methods.
 */
public class PacketRegistry {
    public static final ResourceLocation storageActionIdentifier = new ResourceLocation(Reference.MOD_ID, "storage_action");
    public static final ResourceLocation sortIdentifier = new ResourceLocation(Reference.MOD_ID, "sort_action");
    public static final ResourceLocation renameIdentifier = new ResourceLocation(Reference.MOD_ID, "rename_action");
    public static final ResourceLocation itemFavoriteIdentifier = new ResourceLocation(Reference.MOD_ID, "item_favorite_action");

    public static final ResourceLocation blockRenamedIdentifier = new ResourceLocation(Reference.MOD_ID, "block_renamed_update");
    public static final ResourceLocation serverConfigIdentifier = new ResourceLocation(Reference.MOD_ID, "server_config_update");

    /**
     * Registers the client to server packet receivers.
     */
    public static void registerPacketReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(storageActionIdentifier, (server, player, handler, buf, responseSender) -> {
            Optional<Integer> syncId = buf.readOptional(FriendlyByteBuf::readInt);
            StorageAction action = buf.readEnum(StorageAction.class);
            boolean hotbarProtection = buf.readBoolean();
            Optional<Boolean> smartDepositMode = buf.readOptional(FriendlyByteBuf::readBoolean);

            server.execute(() -> processStorageActionPacket(player, syncId, action, hotbarProtection, smartDepositMode));
        });

        ServerPlayNetworking.registerGlobalReceiver(sortIdentifier, (server, player, handler, buf, responseSender) -> {
            Optional<Integer> syncId = buf.readOptional(FriendlyByteBuf::readInt);
            SortType type = buf.readEnum(SortType.class);
            Optional<Boolean> hotbarProtection = buf.readOptional(FriendlyByteBuf::readBoolean);

            server.execute(() -> processSortPacket(player, syncId, type, hotbarProtection));
        });

        ServerPlayNetworking.registerGlobalReceiver(renameIdentifier, (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readInt();
            String newName = buf.readUtf();

            server.execute(() -> processRenamePacket(player, syncId, newName));
        });

        ServerPlayNetworking.registerGlobalReceiver(itemFavoriteIdentifier, (server, player, handler, buf, responseSender) -> {
            int slotId = buf.readInt();
            boolean value = buf.readBoolean();

            server.execute(() -> processItemFavoritePacket(player, slotId, value));
        });
    }

    /**
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
     * perform the storage action.
     * @param player The player initiating the storage action.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param action The action initiated.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     *
     */
    private static void processStorageActionPacket(ServerPlayer player, Optional<Integer> syncId, StorageAction action, boolean hotbarProtection, Optional<Boolean> smartDepositMode) {
        if (action != StorageAction.QUICK_STACK_TO_NEARBY) {
            if (player.containerMenu == null || player.containerMenu.containerId != syncId.get()) {
                return;
            }

            Container storageInventory;
            Slot firstSlot = player.containerMenu.slots.get(0);
            if (firstSlot.container.getContainerSize() != 0) {
                if (!firstSlot.mayPickup(player)) {
                    player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
                    return;
                }

                // Get the storage's inventory from the player's screen handler.
                storageInventory = firstSlot.container;
            }
            else { // Handle "broken" screen handlers
                List<Slot> nonPlayerSlots = player.containerMenu.slots.stream()
                        .filter(slot -> !(slot.container instanceof Inventory))
                        .toList();

                // Create a SlotBackedInventory, which will hold a reference to all slots and will make inventory
                // adjustments using them.
                storageInventory = new SlotBackedInventory(nonPlayerSlots);
            }


            switch (action) {
                case LOOT_ALL -> TerrastorageCore.lootAll(player.getInventory(), storageInventory, hotbarProtection);
                case DEPOSIT_ALL -> TerrastorageCore.depositAll(player.getInventory(), storageInventory, firstSlot, hotbarProtection);
                case QUICK_STACK -> TerrastorageCore.quickStack(player.getInventory(), storageInventory, hotbarProtection, smartDepositMode.get());
                case RESTOCK -> TerrastorageCore.restock(player.getInventory(), storageInventory, hotbarProtection);
                default -> throw new IllegalArgumentException("Unknown storage action: " + action);
            }
        }
        else {
            TerrastorageCore.quickStackToNearbyStorages(player, hotbarProtection, smartDepositMode.get());
        }
    }

    /**
     * Handles the identification of the inventory to be sorted, before calling TerrastorageCore to perform the sorting.
     * @param player The player initiating the sort.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    private static void processSortPacket(ServerPlayer player, Optional<Integer> syncId, SortType type, Optional<Boolean> hotbarProtection) {
        if (hotbarProtection.isPresent()) {
            // Player inventory sorting.
            TerrastorageCore.sortPlayerItems(player.getInventory(), type, hotbarProtection.get());
        }
        else {
            // Storage sorting.
            if (player.containerMenu == null || player.containerMenu.containerId != syncId.get()) {
                return;
            }

            Container storageInventory;
            Slot firstSlot = player.containerMenu.slots.get(0);
            if (firstSlot.container.getContainerSize() != 0) {
                if (!firstSlot.mayPickup(player)) {
                    player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
                    return;
                }

                // Get the storage's inventory from the player's screen handler.
                storageInventory = firstSlot.container;
            }
            else { // Handle "broken" screen handlers
                List<Slot> nonPlayerSlots = player.containerMenu.slots.stream()
                        .filter(slot -> !(slot.container instanceof Inventory))
                        .toList();

                // Create a SlotBackedInventory, which will hold a reference to all slots and will make inventory
                // adjustments using them.
                storageInventory = new SlotBackedInventory(nonPlayerSlots);
            }

            TerrastorageCore.sortStorageItems(storageInventory, type);
        }
    }

    /**
     * Ensures the player's screen handler isn't null, before calling TerrastorageCore to perform the renaming.
     */
    private static void processRenamePacket(ServerPlayer player, int syncId, String newName) {
        if (player.containerMenu == null || player.containerMenu.containerId != syncId) {
            return;
        }

        if (!player.containerMenu.slots.get(0).mayPickup(player)) {
            player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
    }

    /**
     * Ensures the player's screen handler isn't null, and that the received slot id is in bounds, before modifying the
     * favorite status of the ItemStack.
     */
    private static void processItemFavoritePacket(ServerPlayer player, int slotId, boolean value) {
        if (player.containerMenu == null) {
            return;
        }

        AbstractContainerMenu playerScreenHandler = player.containerMenu;
        if (slotId < 0 || slotId >= playerScreenHandler.slots.size()) {
            return;
        }

        ItemStack slotStack = playerScreenHandler.getSlot(slotId).getItem();
        if (!slotStack.isEmpty()) {
            ItemFavoritingUtils.setFavorite(slotStack, value);
        }
    }
}
