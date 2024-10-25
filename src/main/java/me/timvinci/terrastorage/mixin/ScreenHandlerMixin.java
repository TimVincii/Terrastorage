package me.timvinci.terrastorage.mixin;

import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * A mixin of the ScreenHandler class, used to make the favorite item interactions feel and behave similarly to how they
 * do in Terraria.
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    /**
     * Redirects the Slot.setStack call from within the internalOnSlotClick method.
     * Un-favorites favorite items when they are placed in a slot outside the player's inventory, and makes right click
     * dragging of favorite items over non-favorite slots (and the opposite) respect the slot's favorite state.
     */
    @Redirect(method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V"
            ))
    private void redirectSetStack(Slot slot, ItemStack stack, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (!(slot.inventory instanceof PlayerInventory)) {
            if (ItemFavoritingUtils.isFavorite(stack)) {
                ItemFavoritingUtils.setFavorite(stack, false);
            }
        }
        else if (actionType == SlotActionType.QUICK_CRAFT && slot.hasStack()) {
            boolean slotIsFavorite = ItemFavoritingUtils.isFavorite(slot.getStack());
            if (ItemFavoritingUtils.isFavorite(stack) != slotIsFavorite) {
                ItemFavoritingUtils.setFavorite(stack, slotIsFavorite);
            }
        }

        slot.setStack(stack);
    }

    /**
     * Redirects the Slot.insertStack method from within the internalOnSlotClick method.
     * Un-favorites favorite items when they are inserted in a slot outside the player's inventory, and makes the item
     * interaction of favorite items with non-favorite items behave like it does in Terraria (when items are combined
     * via left-click and when increasing the count of a stack via right-clicking).
     */
    @Redirect(method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack redirectInsertStack(Slot slot, ItemStack cursorStack, int count) {
        if (!slot.hasStack()) {
            if (!(slot.inventory instanceof PlayerInventory) && ItemFavoritingUtils.isFavorite(cursorStack)) {
                if (count == 1 && cursorStack.getCount() > 1) {
                    // If the insertion count is one, pass a single non-favorite copy of the cursor stack to the slot.
                    // This is done to prevent the entire cursor stack from being un-favorited.
                    cursorStack.decrement(1);
                    ItemStack copy = cursorStack.copyWithCount(1);
                    ItemFavoritingUtils.setFavorite(copy, false);
                    slot.setStack(copy);
                    return cursorStack;
                } else { // Un-favorite an item stack if it placed outside the player's inventory.
                    ItemFavoritingUtils.setFavorite(cursorStack, false);
                }
            }
            return slot.insertStack(cursorStack, count);
        } else if (ItemFavoritingUtils.isFavorite(slot.getStack()) == ItemFavoritingUtils.isFavorite(cursorStack)) {
            return slot.insertStack(cursorStack, count);
        }

        ItemStack slotStack = slot.getStack();
        boolean slotIsFavorite = ItemFavoritingUtils.isFavorite(slotStack);
        if (count != 1) {
            // Set the favorite status of the cursor stack based on the favorite status of the receiving slot stack.
            ItemFavoritingUtils.setFavorite(cursorStack, slotIsFavorite);
        } else  {
            if (slotStack.getCount() < slotStack.getMaxCount()) {
                cursorStack.decrement(1);
                slotStack.increment(1);
                slot.setStack(slotStack);
            }

            return cursorStack;
        }

        return slot.insertStack(cursorStack, count);
    }

    /**
     * Redirects the ItemStack.areItemsAndComponentsEqual call from within the internalOnSlotClick method.
     * Uses the custom-made comparison method from the InventoryUtils class.
     */
    @Redirect(method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;areItemsAndComponentsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean redirectAreItemsAndComponentsEqual(ItemStack slotStack, ItemStack cursorStack) {
        return InventoryUtils.areItemsAndComponentsEqual(slotStack, cursorStack);
    }

    /**
     * Redirects the ItemStack.areItemsAndComponentsEqual call from within the canInsertItemIntoSlot method.
     * Uses the custom-made comparison method from the InventoryUtils class.
     */
    @Redirect(method = "canInsertItemIntoSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;areItemsAndComponentsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private static boolean redirectAreItemsAndComponentsEqual_(ItemStack firstStack, ItemStack secondStack) {
        return InventoryUtils.areItemsAndComponentsEqual(firstStack, secondStack);
    }

    /**
     * Redirects the canInsertItemIntoSlot call from within the internalOnSlotClick method.
     * If the SlotActionType is PICKUP_ALL, redirect the method to the original canInsertItemIntoSlot.
     */
    @Redirect(method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/ScreenHandler;canInsertItemIntoSlot(Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/item/ItemStack;Z)Z"
            )
    )
    private boolean redirectCanInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType == SlotActionType.PICKUP_ALL) {
            return originalCanInsertItemIntoSlot(slot, stack, allowOverflow);
        }

        return ScreenHandler.canInsertItemIntoSlot(slot, stack, allowOverflow);
    }

    /**
     * This is the original ScreenHandler.canInsertItemIntoSlot method, we create a copy of it here since the original
     * method is modified via the @Redirect 2 methods above.
     */
    @Unique
    private boolean originalCanInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl = slot == null || !slot.hasStack();
        if (!bl && ItemStack.areItemsAndComponentsEqual(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
        } else {
            return bl;
        }
    }

    /**
     * Redirects the Slot.tryTakeStackRange call from within the internalOnSlotClick method.
     * Removes the favorite status from item stacks created by splitting a favorite stack.
     */
    @Redirect(method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;tryTakeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Ljava/util/Optional;"
            )
    )
    private Optional<ItemStack> redirectTryTakeStackRange(Slot slot, int min, int max, PlayerEntity player) {
        if (!ItemFavoritingUtils.isFavorite(slot.getStack()) || min == slot.getStack().getCount()) {
            return slot.tryTakeStackRange(min, max, player);
        }

        Optional<ItemStack> stack = slot.tryTakeStackRange(min, max, player);
        stack.ifPresent(itemStack -> ItemFavoritingUtils.setFavorite(itemStack, false));

        return stack;
    }
}
