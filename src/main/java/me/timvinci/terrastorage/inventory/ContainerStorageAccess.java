package me.timvinci.terrastorage.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A container only {@link StorageAccess} implementation, used by Quick Stack To Nearby Storages, where storages are
 * discovered as block or vehicle entities with no open menu, and therefore no slots to enforce rules.
 * This honors canPlaceItem and the container's max stack size, mirroring Slot.safeInsert, but it can't enforce slot
 * level rules the way {@link SlotStorageAccess} does.
 */
public class ContainerStorageAccess implements StorageAccess {
    private final Container container;

    public ContainerStorageAccess(Container container) {
        this.container = container;
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }

    @Override
    public ItemStack get(int index) {
        return container.getItem(index);
    }

    @Override
    public boolean canTake(int index, Player player) {
        return true;
    }

    @Override
    public ItemStack take(int index, int amount, Player player) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        return container.removeItem(index, amount);
    }

    @Override
    public void insert(int index, ItemStack stack) {
        if (stack.isEmpty() || !container.canPlaceItem(index, stack)) {
            return;
        }

        ItemStack existing = container.getItem(index);
        int capacity = container.getMaxStackSize(stack);

        if (existing.isEmpty()) {
            int transferable = Math.min(stack.getCount(), capacity);
            if (transferable <= 0) {
                return;
            }

            container.setItem(index, stack.split(transferable));
        }
        else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int transferable = Math.min(stack.getCount(), capacity - existing.getCount());
            if (transferable <= 0) {
                return;
            }

            existing.grow(transferable);
            stack.shrink(transferable);
            container.setItem(index, existing);
        }
    }

    @Override
    public int maxStackSize(int index, ItemStack stack) {
        return container.getMaxStackSize(stack);
    }
}
