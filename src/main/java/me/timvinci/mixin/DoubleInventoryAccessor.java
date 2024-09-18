package me.timvinci.mixin;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A mixin accessor for the DoubleInventory class.
 */
@Mixin(DoubleInventory.class)
public interface DoubleInventoryAccessor {

    @Accessor("first")
    Inventory first();

    @Accessor("second")
    Inventory second();
}
