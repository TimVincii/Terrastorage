package me.timvinci.terrastorage.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link StorageAccess} implementation backed by the Fabric Transfer API, used by Quick Stack To Nearby Storages to
 * reach storage blocks that have no open menu.
 * Going through the item storage lookup rather than the Container interface is what lets nearby storages cover modded
 * blocks, since any block that supports hoppers is reachable this way, whether it's a Container or not.
 */
public class SlottedStorageAccess implements StorageAccess {
    private final SlottedStorage<ItemVariant> storage;

    public SlottedStorageAccess(SlottedStorage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public int size() {
        return storage.getSlotCount();
    }

    @Override
    public ItemStack get(int index) {
        SingleSlotStorage<ItemVariant> slot = storage.getSlot(index);
        if (slot.isResourceBlank() || slot.getAmount() <= 0) {
            return ItemStack.EMPTY;
        }

        return slot.getResource().toStack(clampToInt(slot.getAmount()));
    }

    @Override
    public boolean canTake(int index, Player player) {
        return storage.getSlot(index).supportsExtraction();
    }

    @Override
    public ItemStack take(int index, int amount, Player player) {
        SingleSlotStorage<ItemVariant> slot = storage.getSlot(index);
        if (amount <= 0 || slot.isResourceBlank() || !slot.supportsExtraction()) {
            return ItemStack.EMPTY;
        }

        ItemVariant variant = slot.getResource();
        long extracted;
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = slot.extract(variant, amount, transaction);
            transaction.commit();
        }

        return extracted <= 0 ? ItemStack.EMPTY : variant.toStack(clampToInt(extracted));
    }

    @Override
    public void insert(int index, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        // The variant carries the stack's components, so this only merges into a slot holding an identical item.
        ItemVariant variant = ItemVariant.of(stack);
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = storage.getSlot(index).insert(variant, stack.getCount(), transaction);
            transaction.commit();
        }

        if (inserted > 0) {
            stack.shrink(clampToInt(inserted));
        }
    }

    @Override
    public int maxStackSize(int index, ItemStack stack) {
        SingleSlotStorage<ItemVariant> slot = storage.getSlot(index);
        // The capacity of a slot holding nothing is documented as an estimate, so the stack's own limit is used there.
        return slot.isResourceBlank() ? stack.getMaxStackSize() : clampToInt(slot.getCapacity());
    }

    @Override
    public void markDirty() {
        // Deliberately does nothing as storages mark themselves dirty when a transaction is committed.
    }

    /**
     * Transfer API amounts are longs, while stack counts are ints.
     * @param value The amount to clamp.
     * @return The amount, limited to the largest value an int can hold.
     */
    private static int clampToInt(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }
}
