package me.timvinci.terrastorage.network;

import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.QuickStackMode;
import me.timvinci.terrastorage.util.StorageAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Handled client to server packet sending.
 * Carries out a cooldown check before sending payloads.
 */
public class ClientNetworkHandler {
    public static int actionCooldown = 10;
    private static long lastActionWorldTime = 0;
    private static Level lastWorld = null;


    public static void sendActionPacket(StorageAction action) {
        if (!canSendPacket(PacketRegistry.storageActionIdentifier) ||
            action != StorageAction.QUICK_STACK_TO_NEARBY && Minecraft.getInstance().player.containerMenu == null) {
            return;
        }

        if (canPerformAction()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            switch (action) {
                case QUICK_STACK -> {
                    buf.writeOptional(Optional.of(getSyncId()), FriendlyByteBuf::writeInt);
                    buf.writeEnum(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getStorageQuickStackMode() == QuickStackMode.SMART_DEPOSIT), FriendlyByteBuf::writeBoolean);
                }
                case QUICK_STACK_TO_NEARBY -> {
                    buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeInt);
                    buf.writeEnum(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getNearbyQuickStackMode() == QuickStackMode.SMART_DEPOSIT), FriendlyByteBuf::writeBoolean);
                }
                default -> {
                    buf.writeOptional(Optional.of(getSyncId()), FriendlyByteBuf::writeInt);
                    buf.writeEnum(action);
                    buf.writeBoolean(ClientConfigManager.getInstance().getConfig().getHotbarProtection());
                    buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeBoolean);
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
            !playerInventory && Minecraft.getInstance().player.containerMenu == null) {
            return;
        }

        if (canPerformAction()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            if (playerInventory) {
                buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeInt);
                buf.writeEnum(ClientConfigManager.getInstance().getConfig().getSortType());
                buf.writeOptional(Optional.of(ClientConfigManager.getInstance().getConfig().getHotbarProtection()), FriendlyByteBuf::writeBoolean);
            }
            else {
                buf.writeOptional(Optional.of(getSyncId()), FriendlyByteBuf::writeInt);
                buf.writeEnum(ClientConfigManager.getInstance().getConfig().getSortType());
                buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeBoolean);
            }

            ClientPlayNetworking.send(PacketRegistry.sortIdentifier, buf);
        } else {
            LocalizedTextProvider.sendCooldownMessage();
        }
    }

    public static void sendRenamePacket(String newName) {
        if (!canSendPacket(PacketRegistry.renameIdentifier) || Minecraft.getInstance().player.containerMenu == null) {
            return;
        }

        if (canPerformAction()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(getSyncId());
            buf.writeUtf(newName);

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
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(slotId);
            buf.writeBoolean(value);

            ClientPlayNetworking.send(PacketRegistry.itemFavoriteIdentifier, buf);
            return true;
        }

        LocalizedTextProvider.sendCooldownMessage();
        return false;
    }

    private static boolean canSendPacket(ResourceLocation channelName) {
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

        Minecraft client = Minecraft.getInstance();
        Level currentWorld = client.level;

        long currentWorldTime = currentWorld.getGameTime();

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
        return Minecraft.getInstance().player.containerMenu.containerId;
    }
}
