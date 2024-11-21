package me.timvinci.terrastorage.item;

import me.timvinci.terrastorage.inventory.InventoryUtils;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Compact record used for identifying item stacks by both their item and component data.
 * @param item The item of the stack.
 * @param components The component data of the stack.
 */
public record StackIdentifier(Item item, @Nullable ComponentMapImpl components) {

    public StackIdentifier(ItemStack stack) {
        this(stack.getItem(), new ComponentMapImpl(stack.getComponents()));
    }

    /**
     * Use InventoryUtils.areComponentMapsEqual instead of the ComponentMapImpl.equals method which checks if the
     * baseComponent ComponentMaps are equal by reference.
     * @param o the reference object with which to compare.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StackIdentifier other) {
            if (!Objects.equals(item, other.item)) {
                return false;
            }

            if (components == null && other.components == null)  {
                return true;
            }
            if (components == null || other.components == null)  {
                return false;
            }

            return InventoryUtils.areComponentMapsEqual(this.components, other.components);
        }

        return false;
    }

    /**
     * Calculating a hashCode based on the actual contents of the component map.
     * @return The updated hash code.
     */
    @Override
    public int hashCode() {
        int result = item != null ? item.hashCode() : 0;

        // Use custom logic to hash component contents directly
        if (components != null) {
            for (ComponentType<?> type : components.getTypes()) {
                Object componentValue = components.get(type);
                result = 31 * result + (componentValue != null ? componentValue.hashCode() : 0);
            }
        }
        return result;
    }
}
