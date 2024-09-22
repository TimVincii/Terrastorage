package me.timvinci.network;

import me.timvinci.network.s2c.BlockRenamedPayload;
import me.timvinci.network.s2c.ServerConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Provides a method for registering client global receivers.
 */
public class ClientReceiverRegistry {

    /**
     * Registers server to client payload receivers.
     */
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BlockRenamedPayload.ID, (payload, context) -> {
            BlockRenamedPayload.receive(context.player(), payload.pos(), payload.newName());
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerConfigPayload.ID, (payload, context) -> {
            ClientNetworkHandler.actionCooldown = payload.actionCooldown();
        });
    }
}
