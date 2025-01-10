package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.config.ServerConfigHolder;
import me.timvinci.terrastorage.network.c2s.*;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.QuickStackMode;
import me.timvinci.terrastorage.util.StorageAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Handled client to server payload sending.
 * Carries out a cooldown check before sending payloads.
 */
public class ClientNetworkHandler {
    private static long lastActionWorldTime = 0;
    private static World lastWorld = null;

    public static void sendActionPayload(StorageAction action) {
        if (!canSendPayload(StorageActionPayload.ID) ||
                action != StorageAction.QUICK_STACK_TO_NEARBY && MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            StorageActionPayload payload = switch (action) {
                case QUICK_STACK -> new StorageActionPayload(
                        Optional.of(getSyncId()),
                        action,
                        ClientConfigManager.getInstance().getConfig().getHotbarProtection(),
                        Optional.of(ClientConfigManager.getInstance().getConfig().getStorageQuickStackMode() == QuickStackMode.SMART_DEPOSIT)
                );
                case QUICK_STACK_TO_NEARBY -> new StorageActionPayload(
                        Optional.empty(),
                        action,
                        ClientConfigManager.getInstance().getConfig().getHotbarProtection(),
                        Optional.of(ClientConfigManager.getInstance().getConfig().getNearbyQuickStackMode() == QuickStackMode.SMART_DEPOSIT)
                );
                default -> new StorageActionPayload(
                        Optional.of(getSyncId()),
                        action,
                        ClientConfigManager.getInstance().getConfig().getHotbarProtection(),
                        Optional.empty()
                );
            };

            ClientPlayNetworking.send(payload);
        }
        else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }

    public static void sendSortPayload(boolean playerInventory) {
        if (!canSendPayload(SortPayload.ID) ||
            !playerInventory && MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            SortPayload payload = playerInventory ?
                    new SortPayload(
                            Optional.empty(),
                            ClientConfigManager.getInstance().getConfig().getSortType(),
                            Optional.of(ClientConfigManager.getInstance().getConfig().getHotbarProtection())
                    ) :
                    new SortPayload(
                            Optional.of(getSyncId()),
                            ClientConfigManager.getInstance().getConfig().getSortType(),
                            Optional.empty()
                    );

            ClientPlayNetworking.send(payload);
        } else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }

    public static void sendRenamePayload(String newName) {
        if (!canSendPayload(RenamePayload.ID) || MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new RenamePayload(getSyncId(), newName));
        }
        else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }


    public static boolean sendItemFavoritedPayload(int slotId, boolean value) {
        if (!canSendPayload(ItemFavoritePayload.ID)) {
            return false;
        }

        if (canPerformAction()) {
            ClientPlayNetworking.send(new ItemFavoritePayload(slotId, value));
            return true;
        }

        LocalizedTextProvider.sendCooldownMessage();
        return false;
    }

    private static boolean canSendPayload(CustomPayload.Id<?> type) {
        if (!ClientPlayNetworking.canSend(type)) {
            LocalizedTextProvider.sendUnsupportedMessage();
            return false;
        }

        return true;
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

        if (currentWorldTime - lastActionWorldTime >= ServerConfigHolder.actionCooldown) {
            lastActionWorldTime = currentWorldTime;
            return true;
        }

        return false;
    }

    private static int getSyncId() {
        return MinecraftClient.getInstance().player.currentScreenHandler.syncId;
    }
}