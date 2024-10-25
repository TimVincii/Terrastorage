package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.SortType;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player initiates an inventory sort.
 * @param type The sorting type of the player.
 * @param hotbarProtection The hotbar protection value of the player.
 */
public record PlayerSortPayload(SortType type, boolean hotbarProtection) implements CustomPayload {
    public static final Id<PlayerSortPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "player_sort_action"));
    public static final PacketCodec<PacketByteBuf, PlayerSortPayload> playerSortCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeEnumConstant(value.type);
                buf.writeBoolean(value.hotbarProtection);
            },
            buf -> new PlayerSortPayload(
                    buf.readEnumConstant(SortType.class),
                    buf.readBoolean()
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public static void receive(ServerPlayerEntity player, SortType type, boolean hotbarProtection) {
        TerrastorageCore.sortPlayerItems(player.getInventory(), type, hotbarProtection);
    }
}