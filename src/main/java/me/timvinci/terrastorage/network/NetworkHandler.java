package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.config.ConfigManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.Collection;

/**
 * Handles server to client packet sending.
 */
public class NetworkHandler {

    /**
     * Sends a block renamed packet to all players who are tracking the renamed block entity.
     * @param serverWorld The server world.
     * @param pos The position of the renamed block entity.
     * @param newName The new name of the block entity.
     */
    public static void sendGlobalBlockRenamedPacket(ServerLevel serverWorld, BlockPos pos, String newName) {
        Collection<ServerPlayer> serverPlayersInRange = PlayerLookup.tracking(serverWorld, pos);
        for (ServerPlayer serverPlayer : serverPlayersInRange) {
            sendBlockRenamedPacket(serverPlayer, pos, newName);
        }
    }

    public static void sendBlockRenamedPacket(ServerPlayer player, BlockPos pos, String newName) {
        if (ServerPlayNetworking.canSend(player, PacketRegistry.blockRenamedIdentifier)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeUtf(newName);

            ServerPlayNetworking.send(player, PacketRegistry.blockRenamedIdentifier, buf);
        }
    }

    /**
     * Sends a server config packet to all players present on the server.
     * @param server The server.
     */
    public static void sendGlobalServerConfigPacket(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendServerConfigPacket(player);
        }
    }

    public static void sendServerConfigPacket(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, PacketRegistry.serverConfigIdentifier)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(ConfigManager.getInstance().getConfig().getActionCooldown());

            ServerPlayNetworking.send(player, PacketRegistry.serverConfigIdentifier, buf);
        }
    }

}
