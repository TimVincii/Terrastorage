package me.timvinci.terrastorage.network.c2s;

import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.StorageAction;
import me.timvinci.terrastorage.util.TerrastorageCore;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player initiates a storage action.
 * @param action The action initiated.
 * @param hotbarProtection The hotbar protection value of the player.
 */
public record StorageActionPayload(StorageAction action, boolean hotbarProtection) implements CustomPayload {
    public static final Id<StorageActionPayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "storage_action"));
    public static final PacketCodec<PacketByteBuf, StorageActionPayload> actionCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeEnumConstant(value.action);
                buf.writeBoolean(value.hotbarProtection);
            },
            buf -> new StorageActionPayload(
                    buf.readEnumConstant(StorageAction.class),
                    buf.readBoolean()
            )
    );
    @Override
    public Id<? extends CustomPayload> getId() { return ID ; }

    /**
     * Handles the identification of the inventory the player is interacting with, before calling TerrastorageCore to
     * perform the storage action.
     * @param player The player initiating the storage action.
     * @param action The action initiated.
     * @param hotbarProtection The hotbar protection value of the player.
     */
    public static void receive(ServerPlayerEntity player, StorageAction action, boolean hotbarProtection) {
        if (action == StorageAction.QUICK_STACK_TO_NEARBY) {
            TerrastorageCore.quickStackToNearbyStorages(player, hotbarProtection);
            return;
        }

        if (player.currentScreenHandler == null) {
            return;
        }

        // Get the storage's inventory from the player's screen handler.
        Inventory storageInventory = player.currentScreenHandler.slots.getFirst().inventory;

        // Check if the storage is a shulker box.
        boolean storageIsShulkerBox = player.currentScreenHandler.slots.getFirst() instanceof ShulkerBoxSlot;

        switch (action) {
            case LOOT_ALL -> TerrastorageCore.lootAll(player.getInventory(), storageInventory, hotbarProtection);
            case DEPOSIT_ALL -> TerrastorageCore.depositAll(player.getInventory(), storageInventory, hotbarProtection, storageIsShulkerBox);
            case QUICK_STACK -> TerrastorageCore.quickStack(player.getInventory(), storageInventory, hotbarProtection);
            case RESTOCK -> TerrastorageCore.restock(player.getInventory(), storageInventory, hotbarProtection);
            default -> throw new IllegalArgumentException("Unknown storage action: " + action);
        }
    }
}
