package me.timvinci.terrastorage.network.s2c;

import me.timvinci.terrastorage.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

/**
 * A server to client payload, passing the server config properties that are used by the client to the client.
 * See client/network/ClientReceiverRegistry for the handling of this payload on the client side.
 * @param actionCooldown The action cooldown property value.
 * @param enableItemFavoriting The enable item favoriting property value.
 */
public record ServerConfigPayload(int actionCooldown, boolean enableItemFavoriting) implements CustomPacketPayload {
    public static final Type<ServerConfigPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "server_config_update"));
    public static final StreamCodec<FriendlyByteBuf, ServerConfigPayload> configCodec = StreamCodec.ofMember(
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
    public Type<? extends CustomPacketPayload> type() { return ID; }
}