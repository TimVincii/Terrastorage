package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.SlotBackedInventory;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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
    public static final Type<StorageActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "storage_action"));
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
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
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

            Container storageInventory;
            Slot firstSlot = player.containerMenu.slots.getFirst();
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
}
