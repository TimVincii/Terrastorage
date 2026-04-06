package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.Terrastorage;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

/**
 * A payload sent from the client to the server once a player favorites or unfavorites an item.
 * @param slotId The id of the slot holding the item stack whose favorite status was changed.
 * @param value The favorite state.
 */
public record ItemFavoritePayload(int slotId, boolean value) implements CustomPacketPayload {
    public static final Type<ItemFavoritePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "item_favorite_action"));
    public static final StreamCodec<FriendlyByteBuf, ItemFavoritePayload> toggleItemFavoritedCodec = StreamCodec.ofMember(
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
    public Type<? extends CustomPacketPayload> type() { return ID; }

    /**
     * Ensures the player's screen handler isn't null, and that the received slot id is in bounds, before modifying the
     * favorite status of the ItemStack.
     */
    public static void receive(ServerPlayer player, int slotId, boolean value) {
        if (player.containerMenu == null || !Terrastorage.itemFavoritingEnabled) {
            return;
        }

        AbstractContainerMenu playerScreenHandler = player.containerMenu;
        if (slotId < 0 || slotId >= playerScreenHandler.slots.size()) {
            return;
        }

        ItemStack slotStack = playerScreenHandler.getSlot(slotId).getItem();
        if (!slotStack.isEmpty()) {
            ItemFavoritingUtils.setFavorite(slotStack, value);
        }
    }
}
