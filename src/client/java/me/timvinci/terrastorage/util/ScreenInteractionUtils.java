package me.timvinci.terrastorage.util;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A compact class housing methods related to interactions occurring in screens.
 */
public class ScreenInteractionUtils {

    /**
     * Processes a slot click and cancels the original method if it needs to be.
     * Called from both the HandledScreen mixin and the CreativeInventoryScreen mixin.
     */
    public static void processSlotClick(Minecraft client, ItemStack cursorStack, @Nullable Slot slot, int slotId, int button, ContainerInput containerInput, CallbackInfo ci) {
        boolean cursorStackIsFavorite = ItemFavoritingUtils.isFavorite(cursorStack);
        if (slot == null) {
            if (containerInput == ContainerInput.PICKUP && slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && cursorStackIsFavorite) {
                ci.cancel();
            }
            return;
        }

        boolean cancel = switch (containerInput) {
            case QUICK_MOVE -> ItemFavoritingUtils.isFavorite(slot.getItem()) && !(client.screen instanceof InventoryScreen || client.screen instanceof CreativeModeInventoryScreen);
            case THROW -> ItemFavoritingUtils.isFavorite(slot.getItem());
            case SWAP -> !(slot.container instanceof Inventory) && ItemFavoritingUtils.isFavorite(client.player.getInventory().getItem(button));
            default -> false;
        };

        if (cancel) {
            ci.cancel();
        }
    }
}
