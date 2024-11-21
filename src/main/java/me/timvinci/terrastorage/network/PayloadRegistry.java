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
        PayloadTypeRegistry.playC2S().register(StorageActionPayload.ID, StorageActionPayload.actionCodec);
        ServerPlayNetworking.registerGlobalReceiver(StorageActionPayload.ID, (payload, context) -> {
            StorageActionPayload.receive(context.player(), payload.syncId(), payload.action(), payload.hotbarProtection(), payload.smartDepositMode());
        });

        PayloadTypeRegistry.playC2S().register(SortPayload.ID, SortPayload.storageSortCodec);
        ServerPlayNetworking.registerGlobalReceiver(SortPayload.ID, (payload, context) -> {
            SortPayload.receive(context.player(), payload.syncId(), payload.type(), payload.hotbarProtection());
        });

        PayloadTypeRegistry.playC2S().register(RenamePayload.ID, RenamePayload.renameCodec);
        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.ID, (payload, context) -> {
            RenamePayload.receive(context.player(), payload.syncId(), payload.newName());
        });

        PayloadTypeRegistry.playC2S().register(ItemFavoritePayload.ID, ItemFavoritePayload.toggleItemFavoritedCodec);
        ServerPlayNetworking.registerGlobalReceiver(ItemFavoritePayload.ID, (payload, context) -> {
            ItemFavoritePayload.receive(context.player(), payload.slotId(), payload.value());
        });

        PayloadTypeRegistry.playS2C().register(BlockRenamedPayload.ID, BlockRenamedPayload.renamedCodec);
        PayloadTypeRegistry.playS2C().register(ServerConfigPayload.ID, ServerConfigPayload.configCodec);
    }
}
