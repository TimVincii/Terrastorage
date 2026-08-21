package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.inventory.SlotStorageAccess;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.SortType;
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
    public static final Type<SortPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "sort_action"));
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
     * Handles the identification of the storage to be sorted, before calling TerrastorageCore to perform the sorting.
     * @param player The player initiating the sort.
     * @param syncId The sync id of the screen handler from which the action was sent.
     * @param sortType The sorting type of the player.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void receive(ServerPlayer player, Optional<Integer> syncId, SortType sortType, Optional<Boolean> hotbarProtection) {
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
}
