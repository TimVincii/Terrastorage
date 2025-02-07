package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.SlotBackedInventory;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.SortType;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * A payload sent from the client to the server once a player initiates a sort operation.
 * @param syncId The sync id of the screen handler from which the action was sent.
 * @param type The sorting type of the player.
 * @param hotbarProtection The hotbar protection value of the player.
 */
public record SortPayload(
        Optional<Integer> syncId,
        SortType type,
        Optional<Boolean> hotbarProtection
) implements CustomPayload {
    public static final Id<SortPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "sort_action"));
    public static final PacketCodec<PacketByteBuf, SortPayload> storageSortCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeOptional(value.syncId, PacketByteBuf::writeInt);
                buf.writeEnumConstant(value.type);
                buf.writeOptional(value.hotbarProtection, PacketByteBuf::writeBoolean);
            },
            buf -> new SortPayload(
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readEnumConstant(SortType.class),
                    buf.readOptional(PacketByteBuf::readBoolean)
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    /**
     * Handles the identification of the inventory to be sorted, before calling TerrastorageCore to perform the sorting.
     * @param player The player initiating the sort.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param type The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void receive(ServerPlayerEntity player, Optional<Integer> syncId, SortType type, Optional<Boolean> hotbarProtection) {
        if (hotbarProtection.isPresent()) {
            // Player inventory sorting.
            TerrastorageCore.sortPlayerItems(player.getInventory(), type, hotbarProtection.get());
        }
        else {
            // Storage sorting.
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

            TerrastorageCore.sortStorageItems(storageInventory, type);
        }
    }
}
