package me.timvinci.network.c2s;

import me.timvinci.mixin.DoubleInventoryAccessor;
import me.timvinci.mixin.EntityAccessor;
import me.timvinci.mixin.LockableContainerBlockEntityAccessor;
import me.timvinci.network.NetworkHandler;
import me.timvinci.util.Reference;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A payload sent from the client to the server once a player renames a storage.
 * @param newName The new name of the storage.
 */
public record RenamePayload(String newName) implements CustomPayload {
    public static final Id<RenamePayload> ID = new Id<>(Identifier.of(Reference.MOD_ID, "rename_action"));
    public static final PacketCodec<PacketByteBuf, RenamePayload> renameCodec = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.newName);
            },
            buf -> new RenamePayload(
                    buf.readString()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Handles the renaming of an entity or block entity that the player is interacting with.
     * Updates the name of the entity or block entity and sends the new name to all players tracking it.
     * Also updates the screen title for the player who initiated the rename action.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void receive(ServerPlayerEntity player, String newName) {
        Text newCustomName = newName.isEmpty() ? null : Text.literal(newName);
        String newTitle = newName;

        Inventory containerInventory = player.currentScreenHandler.slots.getFirst().inventory;
        if (containerInventory instanceof VehicleInventory vehicleInventory) {
            Entity entity = (Entity) vehicleInventory;
            if (newCustomName == null) {
                newTitle = ((EntityAccessor)entity).invokeGetDefaultName().getString();
            }
            else if (newName.equals(((EntityAccessor)entity).invokeGetDefaultName().getString())) {
                newCustomName = null;
            }

            entity.setCustomName(newCustomName);
        }
        else if (containerInventory instanceof DoubleInventoryAccessor accessor) {
            if (newCustomName == null) {
                newTitle = Text.translatable("container.chestDouble").getString();
            }
            else if (newName.equals(Text.translatable("container.chestDouble").getString())) {
                newCustomName = null;
            }

            LockableContainerBlockEntity firstPart = (LockableContainerBlockEntity) accessor.first();
            LockableContainerBlockEntity secondPart = (LockableContainerBlockEntity) accessor.second();

            ((LockableContainerBlockEntityAccessor)firstPart).setCustomName(newCustomName);
            ((LockableContainerBlockEntityAccessor)secondPart).setCustomName(newCustomName);

            firstPart.markDirty();
            secondPart.markDirty();

            NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), firstPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
            NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), secondPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
        }
        else if (containerInventory instanceof LockableContainerBlockEntity lockableContainerBlockEntity) {
            LockableContainerBlockEntityAccessor accessor = (LockableContainerBlockEntityAccessor) lockableContainerBlockEntity;

            if (newCustomName == null) {
                newTitle = accessor.invokeGetContainerName().getString();
            }
            else if (newName.equals(accessor.invokeGetContainerName().getString())) {
                newCustomName = null;
            }

            ((LockableContainerBlockEntityAccessor)lockableContainerBlockEntity).setCustomName(newCustomName);
            lockableContainerBlockEntity.markDirty();

            NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), lockableContainerBlockEntity.getPos(), newCustomName == null ? "" : newCustomName.getString());
        }
        else {
            newTitle = "RENAME FAILED!";
        }

        NetworkHandler.sendScreenTitleUpdatePayload(player, newTitle);
    }
}
