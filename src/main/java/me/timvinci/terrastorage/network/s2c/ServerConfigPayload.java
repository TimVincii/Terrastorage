package me.timvinci.terrastorage.network.s2c;

import me.timvinci.terrastorage.util.Reference;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A server to client payload, passing the server config properties that are used by the client to the client.
 * See client/network/ClientReceiverRegistry for the handling of this payload on the client side.
 * @param actionCooldown The action cooldown property value.
 * @param enableItemFavoriting The enable item favoriting property value.
 */
public record ServerConfigPayload(int actionCooldown, boolean enableItemFavoriting) implements CustomPayload {
    public static final Id<ServerConfigPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "server_config_update"));
    public static final PacketCodec<PacketByteBuf, ServerConfigPayload> configCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.actionCooldown);
                buf.writeBoolean(value.enableItemFavoriting);
            },
            buf -> new ServerConfigPayload(
                    buf.readInt(),
                    buf.readBoolean()
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}