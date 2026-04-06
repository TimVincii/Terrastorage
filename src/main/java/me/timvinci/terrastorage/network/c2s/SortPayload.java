package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.SlotBackedInventory;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.SortType;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * A payload sent from the client to the server once a player initiates a sort operation.
 * @param syncId The sync id of the screen handler from which the action was sent.
 * @param sortType The sorting type of the player.
 * @param hotbarProtection The hotbar protection value of the player.
 */
public record SortPayload(
        Optional<Integer> syncId,
        SortType sortType,
        Optional<Boolean> hotbarProtection
) implements CustomPacketPayload {
    public static final Type<SortPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "sort_action"));
    public static final StreamCodec<FriendlyByteBuf, SortPayload> storageSortCodec = StreamCodec.ofMember(
            (value, buf) -> {
                buf.writeOptional(value.syncId, FriendlyByteBuf::writeInt);
                buf.writeEnum(value.sortType);
                buf.writeOptional(value.hotbarProtection, FriendlyByteBuf::writeBoolean);
            },
            buf -> new SortPayload(
                    buf.readOptional(FriendlyByteBuf::readInt),
                    buf.readEnum(SortType.class),
                    buf.readOptional(FriendlyByteBuf::readBoolean)
            )
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    /**
     * Handles the identification of the inventory to be sorted, before calling TerrastorageCore to perform the sorting.
     * @param player The player initiating the sort.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param sortType The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void receive(ServerPlayer player, Optional<Integer> syncId, SortType sortType, Optional<Boolean> hotbarProtection) {
        if (hotbarProtection.isPresent()) {
            // Player inventory sorting.
            TerrastorageCore.sortPlayerItems(player.getInventory(), sortType, hotbarProtection.get());
        }
        else {
            // Storage sorting.
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

            TerrastorageCore.sortStorageItems(storageInventory, sortType);
        }
    }
}
