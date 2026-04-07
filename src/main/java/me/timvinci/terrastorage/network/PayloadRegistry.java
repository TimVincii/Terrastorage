package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.network.c2s.*;
import me.timvinci.terrastorage.network.s2c.BlockRenamedPayload;
import me.timvinci.terrastorage.network.s2c.ServerConfigPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Provides a method for registering receivers and payload.
 */
public class PayloadRegistry {

    /**
     * Registers the client to server payload receivers as well as the server to client payloads.
     */
    public static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(StorageActionPayload.ID, StorageActionPayload.actionCodec);
        ServerPlayNetworking.registerGlobalReceiver(StorageActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> StorageActionPayload.receive(context.player(), payload.syncId(), payload.action(), payload.hotbarProtection(), payload.smartDepositMode()));
        });

        PayloadTypeRegistry.serverboundPlay().register(SortPayload.ID, SortPayload.storageSortCodec);
        ServerPlayNetworking.registerGlobalReceiver(SortPayload.ID, (payload, context) -> {
            context.server().execute(() -> SortPayload.receive(context.player(), payload.syncId(), payload.sortType(), payload.hotbarProtection()));
        });

        PayloadTypeRegistry.serverboundPlay().register(RenamePayload.ID, RenamePayload.renameCodec);
        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.ID, (payload, context) -> {
            context.server().execute(() -> RenamePayload.receive(context.player(), payload.syncId(), payload.newName()));
        });

        PayloadTypeRegistry.serverboundPlay().register(ItemFavoritePayload.ID, ItemFavoritePayload.toggleItemFavoritedCodec);
        ServerPlayNetworking.registerGlobalReceiver(ItemFavoritePayload.ID, (payload, context) -> {
            context.server().execute(() -> ItemFavoritePayload.receive(context.player(), payload.slotId(), payload.value()));
        });

        PayloadTypeRegistry.clientboundPlay().register(BlockRenamedPayload.ID, BlockRenamedPayload.renamedCodec);
        PayloadTypeRegistry.clientboundPlay().register(ServerConfigPayload.ID, ServerConfigPayload.configCodec);
    }
}
