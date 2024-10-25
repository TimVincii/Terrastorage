package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Provides a method for registering packet receivers and holds packet processing methods.
 */
public class PacketRegistry {
    public static final Identifier storageActionIdentifier = new Identifier(Reference.MOD_ID, "storage_action");
    public static final Identifier storageSortIdentifier = new Identifier(Reference.MOD_ID, "storage_sort_action");
    public static final Identifier renameIdentifier = new Identifier(Reference.MOD_ID, "rename_action");
    public static final Identifier playerSortIdentifier = new Identifier(Reference.MOD_ID, "player_sort_action");
    public static final Identifier itemFavoriteIdentifier = new Identifier(Reference.MOD_ID, "item_favorite_action");

    public static final Identifier blockRenamedIdentifier = new Identifier(Reference.MOD_ID, "block_renamed_update");
    public static final Identifier serverConfigIdentifier = new Identifier(Reference.MOD_ID, "server_config_update");

    /**
     * Registers the client to server packet receivers.
     */
    public static void registerPacketReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(storageActionIdentifier, (server, player, handler, buf, responseSender) -> {
            StorageAction action = buf.readEnumConstant(StorageAction.class);
            boolean hotbarProtection = buf.readBoolean();

            server.execute(() -> processStorageActionPacket(player, action, hotbarProtection));
        });

        ServerPlayNetworking.registerGlobalReceiver(storageSortIdentifier, (server, player, handler, buf, responseSender) -> {
            SortType type = buf.readEnumConstant(SortType.class);
            server.execute(() -> processStorageSortPacket(player, type ));
        });

        ServerPlayNetworking.registerGlobalReceiver(renameIdentifier, (server, player, handler, buf, responseSender) -> {
            String newName = buf.readString();
            server.execute(() -> processRenamePacket(player, newName));
        });

        ServerPlayNetworking.registerGlobalReceiver(playerSortIdentifier, (server, player, handler, buf, responseSender) -> {
            SortType type = buf.readEnumConstant(SortType.class);
            boolean hotbarProtection = buf.readBoolean();

            server.execute(() -> processPlayerSortPacket(player, type, hotbarProtection));
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
     * @param action The action initiated.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    private static void processStorageActionPacket(ServerPlayerEntity player, StorageAction action, boolean hotbarProtection) {
        if (action == StorageAction.QUICK_STACK_TO_NEARBY) {
            TerrastorageCore.quickStackToNearbyStorages(player, hotbarProtection);
            return;
        }

        if (player.currentScreenHandler == null) {
            return;
        }

        // Get the storage's inventory from the player's screen handler.
        Inventory storageInventory = player.currentScreenHandler.slots.get(0).inventory;

        // Check if the storage is a shulker box.
        boolean storageIsShulkerBox = player.currentScreenHandler.slots.get(0) instanceof ShulkerBoxSlot;

        switch (action) {
            case LOOT_ALL -> TerrastorageCore.lootAll(player.getInventory(), storageInventory, hotbarProtection);
            case DEPOSIT_ALL -> TerrastorageCore.depositAll(player.getInventory(), storageInventory, hotbarProtection, storageIsShulkerBox);
            case QUICK_STACK -> TerrastorageCore.quickStack(player.getInventory(), storageInventory, hotbarProtection);
            case RESTOCK -> TerrastorageCore.restock(player.getInventory(), storageInventory, hotbarProtection);
            default -> throw new IllegalArgumentException("Unknown storage action: " + action);
        }
    }

    /**
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
     * perform the sorting.
     * @param player The player initiating the sort.
     * @param type The sorting type of the player.
     */
    private static void processStorageSortPacket(ServerPlayerEntity player, SortType type) {
        if (player.currentScreenHandler == null) {
            return;
        }

        Inventory storageInventory = player.currentScreenHandler.slots.get(0).inventory;
        TerrastorageCore.sortStorageItems(storageInventory, type);
    }

    /**
     * Ensures the player's screen handler isn't null, before calling TerrastorageCore to perform the renaming.
     */
    private static void processRenamePacket(ServerPlayerEntity player, String newName) {
        if (player.currentScreenHandler == null) {
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
    }

    private static void processPlayerSortPacket(ServerPlayerEntity player, SortType type, boolean hotbarProtection) {
        TerrastorageCore.sortPlayerItems(player.getInventory(), type, hotbarProtection);
    }

    /**
     * Ensures the player's screen handler isn't null, and that the received slot id is in bounds, before modifying the
     * favorite status of the ItemStack.
     */
    private static void processItemFavoritePacket(ServerPlayerEntity player, int slotId, boolean value) {
        if (player.currentScreenHandler == null) {
            return;
        }

        ScreenHandler playerScreenHandler = player.currentScreenHandler;
        if (slotId < 0 || slotId >= playerScreenHandler.slots.size()) {
            return;
        }

        ItemStack slotStack = playerScreenHandler.getSlot(slotId).getStack();
        if (!slotStack.isEmpty()) {
            ItemFavoritingUtils.setFavorite(slotStack, value);
        }
    }
}
