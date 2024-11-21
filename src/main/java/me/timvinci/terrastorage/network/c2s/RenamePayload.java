package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player renames a storage.
 * @param newName The new name of the storage.
 */
public record RenamePayload(int syncId, String newName) implements CustomPayload {
    public static final Id<RenamePayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "rename_action"));
    public static final PacketCodec<PacketByteBuf, RenamePayload> renameCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeSyncId(value.syncId);
                buf.writeString(value.newName);
            },
            buf -> new RenamePayload(
                    buf.readSyncId(),
                    buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Checks if the player's screen handler is valid before calling TerrastorageCore to carry out the rename.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void receive(ServerPlayerEntity player, int syncId, String newName) {
        if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId) {
            return;
        }

        if (!player.currentScreenHandler.slots.getFirst().canTakeItems(player)) {
            player.sendMessage(Text.translatable("terrastorage.message.restricted_inventory"));
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
    }
}
