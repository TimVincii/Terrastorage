package me.timvinci.network.c2s;

import me.timvinci.api.ItemFavoritingUtils;
import me.timvinci.util.Reference;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player favorites or unfavorites an item.
 * @param slotId The id of the slot holding the item stack whose favorite status was changed.
 * @param value The favorite state.
 */
public record ItemFavoritePayload(int slotId, boolean value) implements CustomPayload {
    public static final Id<ItemFavoritePayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "item_favorite_action"));
    public static final PacketCodec<PacketByteBuf, ItemFavoritePayload> toggleItemFavoritedCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.slotId);
                buf.writeBoolean(value.value);
            },
            buf -> new ItemFavoritePayload(
                    buf.readInt(),
                    buf.readBoolean()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    /**
     * Ensures the player's screen handler isn't null, and that the received slot id is in bounds, before modifying the
     * favorite status of the ItemStack.
     */
    public static void receive(ServerPlayerEntity player, int slotId, boolean value) {
        if (player.currentScreenHandler == null) {
            return;
        }

        ScreenHandler playerScreenHandler = player.currentScreenHandler;
        if (slotId < 0 || slotId >= playerScreenHandler.slots.size()) {
            return;
        }

        ItemStack slotStack = playerScreenHandler.getSlot(slotId).getStack();
        if (!slotStack.isEmpty()) {
            ItemFavoritingUtils.setFavorite(slotStack, value);
        }
    }
}
