package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.inventory.SlotStorageAccess;
import me.timvinci.terrastorage.inventory.StorageAccess;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * A payload sent from the client to the server once a player initiates a storage action.
 * @param syncId The sync id of the screen handler from which the action was sent.
 * @param action The action initiated.
 * @param hotbarProtection The hotbar protection value of the player.
 * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
 */
public record StorageActionPayload(
        Optional<Integer> syncId,
        StorageAction action,
        boolean hotbarProtection,
        Optional<Boolean> smartDepositMode
) implements CustomPacketPayload {
    public static final Type<StorageActionPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "storage_action"));
    public static final StreamCodec<FriendlyByteBuf, StorageActionPayload> actionCodec = StreamCodec.ofMember(
            (value, buf) -> {
                buf.writeOptional(value.syncId, FriendlyByteBuf::writeInt);
                buf.writeEnum(value.action);
                buf.writeBoolean(value.hotbarProtection);
                buf.writeOptional(value.smartDepositMode, FriendlyByteBuf::writeBoolean);
            },
            buf -> new StorageActionPayload(
                    buf.readOptional(FriendlyByteBuf::readInt),
                    buf.readEnum(StorageAction.class),
                    buf.readBoolean(),
                    buf.readOptional(FriendlyByteBuf::readBoolean)
            )
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return ID ; }

    /**
     * Handles the identification of the storage the player is interacting with, before calling TerrastorageCore to
     * perform the storage action.
     * @param player The player initiating the storage action.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param action The action initiated.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void receive(ServerPlayer player, Optional<Integer> syncId, StorageAction action, boolean hotbarProtection, Optional<Boolean> smartDepositMode) {
        if (action != StorageAction.QUICK_STACK_TO_NEARBY) {
            if (player.containerMenu == null || player.containerMenu.containerId != syncId.get()) {
                return;
            }

            // Operate on the menu's storage-side slots directly, so slot rules are respected and every storage type
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
}