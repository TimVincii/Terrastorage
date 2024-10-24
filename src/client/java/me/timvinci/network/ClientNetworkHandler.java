package me.timvinci.network;

import me.timvinci.config.ClientConfigManager;
import me.timvinci.util.StorageAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Handled client to server packet sending.
 * Carries out a cooldown check before sending payloads.
 */
public class ClientNetworkHandler {
    public static int actionCooldown = 10;
    private static long lastActionWorldTime = 0;
    private static World lastWorld = null;


    public static void sendActionPacket(StorageAction action) {
        if (!ClientPlayNetworking.canSend(PacketRegistry.storageActionIdentifier)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_packet"));
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(action);
            buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());

            ClientPlayNetworking.send(PacketRegistry.storageActionIdentifier, buf);
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static void sendStorageSortPacket() {
        if (!ClientPlayNetworking.canSend(PacketRegistry.storageSortIdentifier)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_packet"));
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(ClientConfigManager.getInstance().getConfig().getSortType());

            ClientPlayNetworking.send(PacketRegistry.storageSortIdentifier, buf);
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static void sendRenamePacket(String newName) {
        if (!ClientPlayNetworking.canSend(PacketRegistry.renameIdentifier)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_packet"));
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(newName);

            ClientPlayNetworking.send(PacketRegistry.renameIdentifier, buf);
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }

    }

    public static void sendPlayerSortPacket() {
        if (!ClientPlayNetworking.canSend(PacketRegistry.playerSortIdentifier)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_packet"));
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(ClientConfigManager.getInstance().getConfig().getSortType());
            buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());

            ClientPlayNetworking.send(PacketRegistry.playerSortIdentifier, buf);
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static boolean sendItemFavoritedPacket(int slotId, boolean value) {
        if (!ClientPlayNetworking.canSend(PacketRegistry.itemFavoriteIdentifier)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_packet"));
            return false;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(slotId);
            buf.writeBoolean(value);

            ClientPlayNetworking.send(PacketRegistry.itemFavoriteIdentifier, buf);
            return true;
        }

        MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        return false;
    }

    /**
     * Checks whether payload sending is on cooldown.
     * @return True if it isn't, false otherwise.
     */
    private static boolean canPerformAction() {
        MinecraftClient client = MinecraftClient.getInstance();
        World currentWorld = client.world;

        long currentWorldTime = currentWorld.getTime();

        if (lastWorld != currentWorld) {
            lastActionWorldTime = 0;
            lastWorld = currentWorld;
        }

        if (currentWorldTime - lastActionWorldTime >= actionCooldown) {
            lastActionWorldTime = currentWorldTime;
            return true;
        }
        
        return false;
    }
}
