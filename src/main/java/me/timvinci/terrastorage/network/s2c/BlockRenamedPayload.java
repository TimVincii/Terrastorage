package me.timvinci.terrastorage.network.s2c;

import me.timvinci.terrastorage.mixin.LockableContainerBlockEntityAccessor;
import me.timvinci.terrastorage.util.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * A server to client payload, notifying the client of the renaming of a block entity.
 * @param pos The position of the block entity.
 * @param newName The new name of the block entity.
 */
public record BlockRenamedPayload(BlockPos pos, String newName) implements CustomPayload {
    public static final Id<BlockRenamedPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "block_renamed_update"));
    public static final PacketCodec<PacketByteBuf, BlockRenamedPayload> renamedCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeBlockPos(value.pos);
                buf.writeString(value.newName);
            },
            buf -> new BlockRenamedPayload(
                    buf.readBlockPos(),
                    buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Handles the renaming of the block entity on the client side.
     * @param player The player whose client received the payload.
     * @param pos The position of the block entity.
     * @param newName The new name of the block entity.
     */
    public static void receive(PlayerEntity player, BlockPos pos, String newName) {
        LockableContainerBlockEntityAccessor accessor = (LockableContainerBlockEntityAccessor) player.getEntityWorld().getBlockEntity(pos);
        accessor.setCustomName(newName.isEmpty() ? null : Text.literal(newName));
    }
}
