package me.timvinci.terrastorage.network.s2c;

import me.timvinci.terrastorage.mixin.BaseContainerBlockEntityAccessor;
import me.timvinci.terrastorage.util.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

/**
 * A server to client payload, notifying the client of the renaming of a block entity.
 * @param pos The position of the block entity.
 * @param newName The new name of the block entity.
 */
public record BlockRenamedPayload(BlockPos pos, String newName) implements CustomPacketPayload {
    public static final Type<BlockRenamedPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block_renamed_update"));
    public static final StreamCodec<FriendlyByteBuf, BlockRenamedPayload> renamedCodec = StreamCodec.ofMember(
            (value, buf) -> {
                buf.writeBlockPos(value.pos);
                buf.writeUtf(value.newName);
            },
            buf -> new BlockRenamedPayload(
                    buf.readBlockPos(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    /**
     * Handles the renaming of the block entity on the client side.
     * @param player The player whose client received the payload.
     * @param pos The position of the block entity.
     * @param newName The new name of the block entity.
     */
    public static void receive(Player player, BlockPos pos, String newName) {
        BaseContainerBlockEntityAccessor accessor = (BaseContainerBlockEntityAccessor) player.level().getBlockEntity(pos);
        accessor.setName(newName.isEmpty() ? null : Component.literal(newName));
    }
}
