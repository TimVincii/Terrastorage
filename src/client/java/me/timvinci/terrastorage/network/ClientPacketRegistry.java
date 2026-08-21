package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.mixin.BaseContainerBlockEntityAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Provides a method for registering client global receivers and holds packet processing methods.
 */
public class ClientPacketRegistry {

    /**
     * Registers server to client packet receivers.
     */
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.blockRenamedIdentifier, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String newName = buf.readUtf();

            client.execute(() -> processBlockRenamedPacket(client.level, pos, newName));
        });

        ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.serverConfigIdentifier, (client, handler, buf, responseSender) -> {
            ClientNetworkHandler.actionCooldown = buf.readInt();
        });
    }

    /**
     * Handles the renaming of the block entity on the client side.
     * @param world The world.
     * @param pos The position of the block entity.
     * @param newName The new name of the block entity.
     */
    private static void processBlockRenamedPacket(Level world, BlockPos pos, String newName) {
        BaseContainerBlockEntityAccessor accessor = (BaseContainerBlockEntityAccessor) world.getBlockEntity(pos);
        accessor.setName(newName.isEmpty() ? null : Component.literal(newName));
    }
}
