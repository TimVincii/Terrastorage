package me.timvinci.network.s2c;

import me.timvinci.util.Reference;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A server to client payload, ordering the client to update a screen title.
 * See client/network/ClientReceiverRegistry for the handling of this payload on the client side.
 * @param newTitle
 */
public record ScreenTitleUpdatePayload(String newTitle) implements CustomPayload {
    public static final Id<ScreenTitleUpdatePayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "screen_title_update"));
    public static final PacketCodec<PacketByteBuf, ScreenTitleUpdatePayload> titleUpdateCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.newTitle);
            },
            buf -> new ScreenTitleUpdatePayload(
                    buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
