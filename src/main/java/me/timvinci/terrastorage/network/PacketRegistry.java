package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Provides a method for registering packet receivers and holds packet processing methods.
 */
public class PacketRegistry {
    public static final Identifier storageActionIdentifier = new Identifier(Reference.MOD_ID, "storage_action");
    public static final Identifier sortIdentifier = new Identifier(Reference.MOD_ID, "sort_action");
    public static final Identifier renameIdentifier = new Identifier(Reference.MOD_ID, "rename_action");
    public static final Identifier itemFavoriteIdentifier = new Identifier(Reference.MOD_ID, "item_favorite_action");

    public static final Identifier blockRenamedIdentifier = new Identifier(Reference.MOD_ID, "block_renamed_update");
    public static final Identifier serverConfigIdentifier = new Identifier(Reference.MOD_ID, "server_config_update");

    /**
     * Registers the client to server packet receivers.
     */
    public static void registerPacketReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(storageActionIdentifier, (server, player, handler, buf, responseSender) -> {
            Optional<Integer> syncId = buf.readOptional(PacketByteBuf::readInt);
            StorageAction action = buf.readEnumConstant(StorageAction.class);
            boolean hotbarProtection = buf.readBoolean();
            Optional<Boolean> smartDepositMode = buf.readOptional(PacketByteBuf::readBoolean);

            server.execute(() -> processStorageActionPacket(player, syncId, action, hotbarProtection, smartDepositMode));
        });

        ServerPlayNetworking.registerGlobalReceiver(sortIdentifier, (server, player, handler, buf, responseSender) -> {
            Optional<Integer> syncId = buf.readOptional(PacketByteBuf::readInt);
            SortType type = buf.readEnumConstant(SortType.class);
            Optional<Boolean> hotbarProtection = buf.readOptional(PacketByteBuf::readBoolean);

            server.execute(() -> processSortPacket(player, syncId, type, hotbarProtection));
        });

        ServerPlayNetworking.registerGlobalReceiver(renameIdentifier, (server, player, handler, buf, responseSender) -> {
            int syncId = buf.readInt();
            String newName = buf.readString();

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
    private static void processStorageActionPacket(ServerPlayerEntity player, Optional<Integer> syncId, StorageAction action, boolean hotbarProtection, Optional<Boolean> smartDepositMode) {
        if (action != StorageAction.QUICK_STACK_TO_NEARBY) {
            if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId.get()) {
                return;
            }

            ScreenHandler playerScreenHandler = player.currentScreenHandler;
            if (!playerScreenHandler.slots.get(0).canTakeItems(player)) {
                player.sendMessage(Text.translatable("terrastorage.message.restricted_inventory"));
                return;
            }

            // Get the storage's inventory from the player's screen handler.
            Inventory storageInventory = playerScreenHandler.slots.get(0).inventory;

            switch (action) {
                case LOOT_ALL -> TerrastorageCore.lootAll(player.getInventory(), storageInventory, hotbarProtection);
                case DEPOSIT_ALL -> {
                    boolean storageIsShulkerBox = playerScreenHandler.slots.get(0) instanceof ShulkerBoxSlot;
                    TerrastorageCore.depositAll(player.getInventory(), storageInventory, hotbarProtection, storageIsShulkerBox);
                }
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
    private static void processSortPacket(ServerPlayerEntity player, Optional<Integer> syncId, SortType type, Optional<Boolean> hotbarProtection) {
        if (hotbarProtection.isPresent()) {
            // Player inventory sorting.
            TerrastorageCore.sortPlayerItems(player.getInventory(), type, hotbarProtection.get());
        }
        else {
            // Storage sorting.
            if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId.get()) {
                return;
            }

            ScreenHandler playerScreenHandler = player.currentScreenHandler;
            if (!playerScreenHandler.slots.get(0).canTakeItems(player)) {
                player.sendMessage(Text.translatable("terrastorage.message.restricted_inventory"));
                return;
            }

            Inventory storageInventory = playerScreenHandler.slots.get(0).inventory;
            TerrastorageCore.sortStorageItems(storageInventory, type);
        }
    }

    /**
     * Ensures the player's screen handler isn't null, before calling TerrastorageCore to perform the renaming.
     */
    private static void processRenamePacket(ServerPlayerEntity player, int syncId, String newName) {
        if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId) {
            return;
        }

        if (!player.currentScreenHandler.slots.get(0).canTakeItems(player)) {
            player.sendMessage(Text.translatable("terrastorage.message.restricted_inventory"));
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
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
