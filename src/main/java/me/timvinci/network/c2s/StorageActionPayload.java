package me.timvinci.network.c2s;

import me.timvinci.util.Reference;
import me.timvinci.util.StorageAction;
import me.timvinci.util.TerrastorageCore;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        Inventory storageInventory;
        boolean storageIsShulkerBox = false;

        // Get the storage's inventory from a generic container (chests, barrels, etc...)
        if (player.currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            storageInventory = genericContainerScreenHandler.getInventory();
        }
        // Get the storage's inventory from a shulker box.
        else if (player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulkerBoxScreenHandler) {
            storageInventory = shulkerBoxScreenHandler.slots.getFirst().inventory;
            storageIsShulkerBox = true;
        }
        else {
            player.sendMessage(Text.literal(Reference.MOD_NAME + ": Unknown storage type."));
            return;
        }

        switch (action) {
            case LOOT_ALL -> TerrastorageCore.lootAll(player.getInventory(), storageInventory, hotbarProtection);
            case DEPOSIT_ALL -> TerrastorageCore.depositAll(player.getInventory(), storageInventory, hotbarProtection, storageIsShulkerBox);
            case QUICK_STACK -> TerrastorageCore.quickStack(player.getInventory(), storageInventory, hotbarProtection);
            case RESTOCK -> TerrastorageCore.restock(player.getInventory(), storageInventory, hotbarProtection);
            default -> throw new IllegalArgumentException("Unknown storage action: " + action);
        }
    }
}
