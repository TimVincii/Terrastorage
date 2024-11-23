package me.timvinci.terrastorage.util;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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
    public static void processSlotClick(MinecraftClient client, ItemStack cursorStack, @Nullable Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        boolean cursorStackIsFavorite = ItemFavoritingUtils.isFavorite(cursorStack);
        if (slot == null) {
            if (actionType == SlotActionType.PICKUP && slotId == ScreenHandler.EMPTY_SPACE_SLOT_INDEX && cursorStackIsFavorite) {
                ci.cancel();
            }
            return;
        }

        boolean cancel = switch (actionType) {
            case QUICK_MOVE -> ItemFavoritingUtils.isFavorite(slot.getStack()) && !(client.currentScreen instanceof InventoryScreen || client.currentScreen instanceof CreativeInventoryScreen);
            case THROW -> ItemFavoritingUtils.isFavorite(slot.getStack());
            case SWAP -> !(slot.inventory instanceof PlayerInventory) && ItemFavoritingUtils.isFavorite(client.player.getInventory().getStack(button));
            default -> false;
        };

        if (cancel) {
            ci.cancel();
        }
    }
}
