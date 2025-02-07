package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.SlotBackedInventory;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
) implements CustomPayload {
    public static final Id<StorageActionPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "storage_action"));
    public static final PacketCodec<PacketByteBuf, StorageActionPayload> actionCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeOptional(value.syncId, PacketByteBuf::writeInt);
                buf.writeEnumConstant(value.action);
                buf.writeBoolean(value.hotbarProtection);
                buf.writeOptional(value.smartDepositMode, PacketByteBuf::writeBoolean);
            },
            buf -> new StorageActionPayload(
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readEnumConstant(StorageAction.class),
                    buf.readBoolean(),
                    buf.readOptional(PacketByteBuf::readBoolean)
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID ; }

    /**
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
     * perform the storage action.
     * @param player The player initiating the storage action.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param action The action initiated.
     * @param hotbarProtection The hotbar protection value of the player.
     * @param smartDepositMode Whether the player's quick stack mode is 'smart deposit'.
     */
    public static void receive(ServerPlayerEntity player, Optional<Integer> syncId, StorageAction action, boolean hotbarProtection, Optional<Boolean> smartDepositMode) {
        if (action != StorageAction.QUICK_STACK_TO_NEARBY) {
            if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId.get()) {
                return;
            }

            Inventory storageInventory;
            Slot firstSlot = player.currentScreenHandler.slots.getFirst();
            if (firstSlot.inventory.size() != 0) {
                if (!firstSlot.canTakeItems(player)) {
                    player.sendMessage(Text.translatable("terrastorage.message.restricted_inventory"));
                    return;
                }

                // Get the storage's inventory from the player's screen handler.
                storageInventory = firstSlot.inventory;
            }
            else { // Handle "broken" screen handlers
                List<Slot> nonPlayerSlots = player.currentScreenHandler.slots.stream()
                        .filter(slot -> !(slot.inventory instanceof PlayerInventory))
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