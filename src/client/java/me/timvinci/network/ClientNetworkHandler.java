package me.timvinci.network;

import me.timvinci.config.ClientConfigManager;
import me.timvinci.network.c2s.*;
import me.timvinci.util.StorageAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Handled client to server payload sending.
 * Carries out a cooldown check before sending payloads.
 */
public class ClientNetworkHandler {
    public static int actionCooldown = 10;
    private static long lastActionWorldTime = 0;
    private static World lastWorld = null;

    public static void sendActionPayload(StorageAction action) {
        if (!ClientPlayNetworking.canSend(StorageActionPayload.ID)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
            return;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new StorageActionPayload(action, ClientConfigManager.getInstance().getConfig().getHotbarProtection()));
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static void sendStorageSortPayload() {
        if (!ClientPlayNetworking.canSend(StorageActionPayload.ID)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
            return;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new StorageSortPayload(ClientConfigManager.getInstance().getConfig().getSortType()));
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static void sendRenamePayload(String newName) {
        if (!ClientPlayNetworking.canSend(RenamePayload.ID)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
            return;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new RenamePayload(newName));
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }

    }

    public static void sendPlayerSortPayload() {
        if (!ClientPlayNetworking.canSend(PlayerSortPayload.ID)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
            return;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new PlayerSortPayload(
                    ClientConfigManager.getInstance().getConfig().getSortType(),
                    ClientConfigManager.getInstance().getConfig().getHotbarProtection()
            ));
        }
        else {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
        }
    }

    public static boolean sendItemFavoritedPayload(int slotId, boolean value) {
        if (!ClientPlayNetworking.canSend(ItemFavoritePayload.ID)) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
            return false;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new ItemFavoritePayload(slotId, value));
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
