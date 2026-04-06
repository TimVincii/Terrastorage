package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A payload sent from the client to the server once a player renames a storage.
 * @param newName The new name of the storage.
 */
public record RenamePayload(int syncId, String newName) implements CustomPacketPayload {
    public static final Type<RenamePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "rename_action"));
    public static final StreamCodec<FriendlyByteBuf, RenamePayload> renameCodec = StreamCodec.ofMember(
            (value, buf) -> {
                buf.writeContainerId(value.syncId);
                buf.writeUtf(value.newName);
            },
            buf -> new RenamePayload(
                    buf.readContainerId(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    /**
     * Checks if the player's screen handler is valid before calling TerrastorageCore to carry out the rename.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void receive(ServerPlayer player, int syncId, String newName) {
        if (player.containerMenu == null || player.containerMenu.containerId != syncId) {
            return;
        }

        if (!player.containerMenu.slots.getFirst().mayPickup(player)) {
            player.sendSystemMessage(Component.translatable("terrastorage.message.restricted_inventory"));
            return;
        }

        TerrastorageCore.renameStorage(player, newName);
    }
}
