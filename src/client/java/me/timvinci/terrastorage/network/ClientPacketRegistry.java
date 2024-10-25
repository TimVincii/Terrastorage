package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.mixin.LockableContainerBlockEntityAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
            String newName = buf.readString();

            client.execute(() -> processBlockRenamedPacket(client.world, pos, newName));
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
    private static void processBlockRenamedPacket(World world, BlockPos pos, String newName) {
        LockableContainerBlockEntityAccessor accessor = (LockableContainerBlockEntityAccessor) world.getBlockEntity(pos);
        accessor.setCustomName(newName.isEmpty() ? null : Text.literal(newName));
    }
}
