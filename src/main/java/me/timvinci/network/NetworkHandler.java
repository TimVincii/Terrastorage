package me.timvinci.network;

import me.timvinci.config.ConfigManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
    public static void sendGlobalBlockRenamedPacket(ServerWorld serverWorld, BlockPos pos, String newName) {
        Collection<ServerPlayerEntity> serverPlayersInRange = PlayerLookup.tracking(serverWorld, pos);
        for (ServerPlayerEntity serverPlayer : serverPlayersInRange) {
            sendBlockRenamedPacket(serverPlayer, pos, newName);
        }
    }

    public static void sendBlockRenamedPacket(ServerPlayerEntity player, BlockPos pos, String newName) {
        if (ServerPlayNetworking.canSend(player, PacketRegistry.blockRenamedIdentifier)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeString(newName);

            ServerPlayNetworking.send(player, PacketRegistry.blockRenamedIdentifier, buf);
        }
    }

    /**
     * Sends a server config packet to all players present on the server.
     * @param server The server.
     */
    public static void sendGlobalServerConfigPacket(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendServerConfigPacket(player);
        }
    }

    public static void sendServerConfigPacket(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, PacketRegistry.serverConfigIdentifier)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(ConfigManager.getInstance().getConfig().getActionCooldown());

            ServerPlayNetworking.send(player, PacketRegistry.serverConfigIdentifier, buf);
        }
    }

}
