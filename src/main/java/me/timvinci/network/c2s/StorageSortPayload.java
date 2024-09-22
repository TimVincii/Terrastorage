package me.timvinci.network.c2s;

import me.timvinci.util.Reference;
import me.timvinci.util.SortType;
import me.timvinci.util.TerrastorageCore;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player initiates a storage sort.
 * @param type The sorting type of the player.
 */
public record StorageSortPayload(SortType type) implements CustomPayload {
    public static final Id<StorageSortPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "storage_sort_action"));
    public static final PacketCodec<PacketByteBuf, StorageSortPayload> storageSortCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeEnumConstant(value.type);
            },
            buf -> new StorageSortPayload(
                    buf.readEnumConstant(SortType.class)
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    /**
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
     * perform the sorting.
     * @param player The player initiating the sort.
     * @param type The sorting type of the player.
     */
    public static void receive(ServerPlayerEntity player, SortType type) {
        if (player.currentScreenHandler.slots.size() - 36 < 27) {
            return;
        }

        Inventory storageInventory = player.currentScreenHandler.slots.getFirst().inventory;
        TerrastorageCore.sortStorageItems(storageInventory, type);
    }
}
