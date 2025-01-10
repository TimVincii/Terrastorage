package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.Terrastorage;
import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.network.s2c.BlockRenamedPayload;
import me.timvinci.terrastorage.network.s2c.ServerConfigPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

/**
 * Handles server to client payload sending.
 */
public class NetworkHandler {

    /**
     * Sends a block renamed payload to all players who are tracking the renamed block entity.
     * @param serverWorld The server world.
     * @param pos The position of the renamed block entity.
     * @param newName The new name of the block entity.
     */
    public static void sendGlobalBlockRenamedPayload(ServerWorld serverWorld, BlockPos pos, String newName) {
        Collection<ServerPlayerEntity> serverPlayersInRange = PlayerLookup.tracking(serverWorld, pos);
        for (ServerPlayerEntity serverPlayer : serverPlayersInRange) {
            sendBlockRenamedPayload(serverPlayer, pos, newName);
        }
    }

    public static void sendBlockRenamedPayload(ServerPlayerEntity player, BlockPos pos, String newName) {
        if (ServerPlayNetworking.canSend(player, BlockRenamedPayload.ID)) {
            ServerPlayNetworking.send(player, new BlockRenamedPayload(pos, newName));
        }
    }

    /**
     * Sends a server config payload to all players present on the server.
     * @param server The server.
     */
    public static void sendGlobalServerConfigPayload(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendServerConfigPayload(player);
        }
    }

    public static void sendServerConfigPayload(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, ServerConfigPayload.ID)) {
            ServerPlayNetworking.send(player, new ServerConfigPayload(
                    ConfigManager.getInstance().getConfig().getActionCooldown(),
                    Terrastorage.itemFavoritingEnabled)
            );
        }
    }

}
