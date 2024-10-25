package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player renames a storage.
 * @param newName The new name of the storage.
 */
public record RenamePayload(String newName) implements CustomPayload {
    public static final Id<RenamePayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "rename_action"));
    public static final PacketCodec<PacketByteBuf, RenamePayload> renameCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.newName);
            },
            buf -> new RenamePayload(
                    buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Handles the renaming of an entity or block entity that the player is interacting with.
     * Updates the name of the entity or block entity and sends the new name to all players tracking it.
     * Also reopens the screen for the player who initiated the rename action.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void receive(ServerPlayerEntity player, String newName) {
        if (player.currentScreenHandler == null) {
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
    }
}
