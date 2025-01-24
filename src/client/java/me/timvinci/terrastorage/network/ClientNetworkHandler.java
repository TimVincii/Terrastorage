package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.QuickStackMode;
import me.timvinci.terrastorage.util.StorageAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Handled client to server packet sending.
 * Carries out a cooldown check before sending payloads.
 */
public class ClientNetworkHandler {
    public static int actionCooldown = 10;
    private static long lastActionWorldTime = 0;
    private static World lastWorld = null;


    public static void sendActionPacket(StorageAction action) {
        if (!canSendPacket(PacketRegistry.storageActionIdentifier) ||
            action != StorageAction.QUICK_STACK_TO_NEARBY && MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            switch (action) {
                case QUICK_STACK -> {
                    buf.writeOptional(Optional.of(getSyncId()), PacketByteBuf::writeInt);
                    buf.writeEnumConstant(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getStorageQuickStackMode() == QuickStackMode.SMART_DEPOSIT), PacketByteBuf::writeBoolean);
                }
                case QUICK_STACK_TO_NEARBY -> {
                    buf.writeOptional(Optional.empty(), PacketByteBuf::writeInt);
                    buf.writeEnumConstant(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getNearbyQuickStackMode() == QuickStackMode.SMART_DEPOSIT), PacketByteBuf::writeBoolean);
                }
                default -> {
                    buf.writeOptional(Optional.of(getSyncId()), PacketByteBuf::writeInt);
                    buf.writeEnumConstant(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.empty(), PacketByteBuf::writeBoolean);
                }
            }

            ClientPlayNetworking.send(PacketRegistry.storageActionIdentifier, buf);
        }
        else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }

    public static void sendSortPacket(boolean playerInventory) {
        if (!canSendPacket(PacketRegistry.sortIdentifier) ||
            !playerInventory && MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            if (playerInventory) {
                buf.writeOptional(Optional.empty(), PacketByteBuf::writeInt);
                buf.writeEnumConstant(ClientConfigManager.getInstance().getConfig().getSortType());
                buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getHotbarProtection()), PacketByteBuf::writeBoolean);
            }
            else {
                buf.writeOptional(Optional.of(getSyncId()), PacketByteBuf::writeInt);
                buf.writeEnumConstant(ClientConfigManager.getInstance().getConfig().getSortType());
                buf.writeOptional(Optional.empty(), PacketByteBuf::writeBoolean);
            }

            ClientPlayNetworking.send(PacketRegistry.sortIdentifier, buf);
        } else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }

    public static void sendRenamePacket(String newName) {
        if (!canSendPacket(PacketRegistry.renameIdentifier) || MinecraftClient.getInstance().player.currentScreenHandler == null) {
            return;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(getSyncId());
            buf.writeString(newName);

            ClientPlayNetworking.send(PacketRegistry.renameIdentifier, buf);
        }
        else {
            LocalizedTextProvider.sendCooldownMessage();
        }

    }

    public static boolean sendItemFavoritedPacket(int slotId, boolean value) {
        if (!canSendPacket(PacketRegistry.itemFavoriteIdentifier)) {
            return false;
        }

        if (canPerformAction()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(slotId);
            buf.writeBoolean(value);

            ClientPlayNetworking.send(PacketRegistry.itemFavoriteIdentifier, buf);
            return true;
        }

        LocalizedTextProvider.sendCooldownMessage();
        return false;
    }

    private static boolean canSendPacket(Identifier channelName) {
        if (!ClientPlayNetworking.canSend(channelName)) {
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
        if (actionCooldown == 0) {
            return true;
        }

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

    private static int getSyncId() {
        return MinecraftClient.getInstance().player.currentScreenHandler.syncId;
    }
}
