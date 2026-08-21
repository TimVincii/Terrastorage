package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.inventory.SlotStorageAccess;
import me.timvinci.terrastorage.inventory.StorageAccess;
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
            SortType sortType = buf.readEnum(SortType.class);
            Optional<Boolean> hotbarProtection = buf.readOptional(FriendlyByteBuf::readBoolean);

            server.execute(() -> processSortPacket(player, syncId, sortType, hotbarProtection));
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

            // Operate on the menu's storage-side slots directly, so slot rules are respected and every storage sortType
            // (vanilla containers and item-handler backed storages alike) is handled the same way.
            List<Slot> storageSlots = InventoryUtils.getStorageSlots(player.containerMenu, player);
            if (storageSlots.isEmpty() || !InventoryUtils.hasUsableSlot(storageSlots, player)) {
                player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
                return;
            }

            StorageAccess storage = new SlotStorageAccess(storageSlots);
            switch (action) {
                case LOOT_ALL -> TerrastorageCore.lootAll(player, storage, hotbarProtection);
                case DEPOSIT_ALL -> TerrastorageCore.depositAll(player, storage, hotbarProtection);
                case QUICK_STACK -> TerrastorageCore.quickStack(player, storage, hotbarProtection, smartDepositMode.get());
                case RESTOCK -> TerrastorageCore.restock(player, storage, hotbarProtection);
                default -> throw new IllegalArgumentException("Unknown storage action: " + action);
            }

            // Push the corrected state to the client immediately, closing any desync window.
            player.containerMenu.broadcastChanges();
        }
        else {
            TerrastorageCore.quickStackToNearbyStorages(player, hotbarProtection, smartDepositMode.get());
        }
    }


    /**
     * Handles the identification of the inventory to be sorted, before calling TerrastorageCore to perform the sorting.
     * @param player The player initiating the sort.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param sortType The sorting sortType of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    private static void processSortPacket(ServerPlayer player, Optional<Integer> syncId, SortType sortType, Optional<Boolean> hotbarProtection) {
        if (hotbarProtection.isPresent()) {
            // Player inventory sorting.
            TerrastorageCore.sortPlayerItems(player, sortType, hotbarProtection.get());
        }
        else {
            // Storage sorting.
            if (player.containerMenu == null || player.containerMenu.containerId != syncId.get()) {
                return;
            }

            List<Slot> storageSlots = InventoryUtils.getStorageSlots(player.containerMenu, player);
            if (storageSlots.isEmpty() || !InventoryUtils.hasUsableSlot(storageSlots, player)) {
                player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
                return;
            }

            TerrastorageCore.sortStorageItems(player, new SlotStorageAccess(storageSlots), sortType);

            // Push the corrected state to the client immediately, closing any desync window.
            player.containerMenu.broadcastChanges();
        }
    }


    /**
     * Ensures the player's screen handler isn't null, before calling TerrastorageCore to perform the renaming.
     */
    private static void processRenamePacket(ServerPlayer player, int syncId, String newName) {
        if (player.containerMenu == null || player.containerMenu.containerId != syncId) {
            return;
        }

        // No slot check is needed here, as renaming doesn't move any items, and renameStorage reports the storages
        // it doesn't support by itself.
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
