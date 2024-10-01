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
import net.minecraft.screen.NamedScreenHandlerFactory;
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
     * Also reopens the screen for the player who initiated the rename action.
     * @param player The player initiating the rename action.
     * @param newName The new name to apply to the entity or block entity. If empty, the name will be reset to default.
     */
    public static void receive(ServerPlayerEntity player, String newName) {
        Text newCustomName = newName.isEmpty() ? null : Text.literal(newName);

        if (player.currentScreenHandler == null) {
            return;
        }

        NamedScreenHandlerFactory factory;
        Inventory containerInventory = player.currentScreenHandler.slots.getFirst().inventory;
        if (containerInventory instanceof VehicleInventory vehicleInventory) {
            Entity entity = (Entity) vehicleInventory;
            if (newName.equals(((EntityAccessor)entity).invokeGetDefaultName().getString())) {
                newCustomName = null;
            }

            entity.setCustomName(newCustomName);
            factory = (NamedScreenHandlerFactory) entity;
        }
        else if (containerInventory instanceof DoubleInventoryAccessor accessor) {
            if (accessor.first() instanceof LockableContainerBlockEntity firstPart &&
                accessor.second() instanceof LockableContainerBlockEntity secondPart) {

                if (newName.equals("Large " + ((LockableContainerBlockEntityAccessor) firstPart).invokeGetContainerName().getString())) {
                    newCustomName = null;
                }

                ((LockableContainerBlockEntityAccessor) firstPart).setCustomName(newCustomName);
                ((LockableContainerBlockEntityAccessor) secondPart).setCustomName(newCustomName);

                firstPart.markDirty();
                secondPart.markDirty();

                NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), firstPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
                NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), secondPart.getPos(), newCustomName == null ? "" : newCustomName.getString());
                factory = firstPart.getCachedState().createScreenHandlerFactory(player.getWorld(), firstPart.getPos());
            }
            else {
                player.sendMessage(Text.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
                return;
            }
        }
        else if (containerInventory instanceof LockableContainerBlockEntity lockableContainerBlockEntity) {
            LockableContainerBlockEntityAccessor accessor = (LockableContainerBlockEntityAccessor) lockableContainerBlockEntity;

            if (newName.equals(accessor.invokeGetContainerName().getString())) {
                newCustomName = null;
            }

            accessor.setCustomName(newCustomName);
            lockableContainerBlockEntity.markDirty();

            NetworkHandler.sendGlobalBlockRenamedPayload(player.getServerWorld(), lockableContainerBlockEntity.getPos(), newCustomName == null ? "" : newCustomName.getString());
            factory = lockableContainerBlockEntity.getCachedState().createScreenHandlerFactory(player.getWorld(), lockableContainerBlockEntity.getPos());
        }
        else {
            player.sendMessage(Text.literal("The storage you tried to rename is currently unsupported by Terrastorage."));
            return;
        }

        player.closeHandledScreen();
        player.openHandledScreen(factory);
    }
}
