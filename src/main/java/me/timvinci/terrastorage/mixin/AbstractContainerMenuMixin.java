package me.timvinci.terrastorage.mixin;

import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * A mixin of the AbstractContainerMenu class, used to make the favorite item interactions feel and behave similarly to how they
 * do in Terraria.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    /**
     * Redirects {@link Slot#setByPlayer} calls inside {@code AbstractContainerMenu#doClick}.
     * Un-favorites favorite items when they are placed in a slot outside the player's inventory, and makes right click
     * dragging of favorite items over non-favorite slots (and the opposite) respect the slot's favorite state.
     */
    @Redirect(method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;setByPlayer(Lnet/minecraft/world/item/ItemStack;)V"
            ))
    private void redirectDoClickSlotSetByPlayer(Slot slot, ItemStack stack, int slotIndex, int button, ClickType actionType, Player player) {
        if (!InventoryUtils.isPlayerSlot(slot, player)) {
            if (ItemFavoritingUtils.isFavorite(stack)) {
                ItemFavoritingUtils.setFavorite(stack, false);
            }
        }
        else if (actionType == ClickType.QUICK_CRAFT && slot.hasItem()) {
            boolean slotIsFavorite = ItemFavoritingUtils.isFavorite(slot.getItem());
            if (ItemFavoritingUtils.isFavorite(stack) != slotIsFavorite) {
                ItemFavoritingUtils.setFavorite(stack, slotIsFavorite);
            }
        }

        slot.setByPlayer(stack);
    }

    /**
     * Redirects {@link Slot#safeInsert(ItemStack, int)} calls inside {@code AbstractContainerMenu#doClick}.
     * Un-favorites favorite items when they are inserted in a slot outside the player's inventory, and makes the item
     * interaction of favorite items with non-favorite items behave like it does in Terraria (when items are combined
     * via left-click and when increasing the count of a stack via right-clicking).
     */
    @Redirect(method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack redirectDoClickSlotSafeInsert(Slot slot, ItemStack cursorStack, int count, int slotIndex, int button, ClickType actionType, Player player) {
        if (!slot.hasItem()) {
            if (!InventoryUtils.isPlayerSlot(slot, player) && ItemFavoritingUtils.isFavorite(cursorStack)) {
                if (count == 1 && cursorStack.getCount() > 1) {
                    // If the insertion count is one, pass a single non-favorite copy of the cursor stack to the slot.
                    // This is done to prevent the entire cursor stack from being un-favorited.
                    cursorStack.shrink(1);
                    ItemStack copy = cursorStack.copyWithCount(1);
                    ItemFavoritingUtils.setFavorite(copy, false);
                    slot.setByPlayer(copy);
                    return cursorStack;
                } else { // Un-favorite an item stack if it placed outside the player's inventory.
                    ItemFavoritingUtils.setFavorite(cursorStack, false);
                }
            }
            return slot.safeInsert(cursorStack, count);
        } else if (ItemFavoritingUtils.isFavorite(slot.getItem()) == ItemFavoritingUtils.isFavorite(cursorStack)) {
            return slot.safeInsert(cursorStack, count);
        }

        ItemStack slotStack = slot.getItem();
        boolean slotIsFavorite = ItemFavoritingUtils.isFavorite(slotStack);
        if (count != 1) {
            // Set the favorite status of the cursor stack based on the favorite status of the receiving slot stack.
            ItemFavoritingUtils.setFavorite(cursorStack, slotIsFavorite);
        } else  {
            if (slotStack.getCount() < slotStack.getMaxStackSize()) {
                cursorStack.shrink(1);
                slotStack.grow(1);
                slot.setByPlayer(slotStack);
            }

            return cursorStack;
        }

        return slot.safeInsert(cursorStack, count);
    }

    /**
     * Redirects {@link ItemStack#isSameItemSameComponents} calls inside {@code AbstractContainerMenu#doClick}.
     * Uses the custom-made comparison method from the InventoryUtils class.
     */
    @Redirect(method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean redirectDoClickIsSameItemSameComponents(ItemStack slotStack, ItemStack cursorStack) {
        return InventoryUtils.areItemsAndComponentsEqual(slotStack, cursorStack);
    }

    /**
     * Redirects {@link ItemStack#isSameItemSameComponents} calls inside {@link AbstractContainerMenu#canItemQuickReplace}.
     * Uses the custom-made comparison method from the InventoryUtils class.
     */
    @Redirect(method = "canItemQuickReplace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean redirectCanItemQuickReplaceIsSameItemSameComponents(ItemStack firstStack, ItemStack secondStack) {
        return InventoryUtils.areItemsAndComponentsEqual(firstStack, secondStack);
    }

    /**
     * Redirects {@link AbstractContainerMenu#canItemQuickReplace} calls inside {@code AbstractContainerMenu#doClick}.
     * If the container input is PICKUP_ALL, the call is routed to the unmodified copy of canItemQuickReplace below.
     */
    @Redirect(method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;canItemQuickReplace(Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/item/ItemStack;Z)Z"
            )
    )
    private boolean redirectDoClickCanItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean allowOverflow, int slotIndex, int button, ClickType actionType, Player player) {
        if (actionType == ClickType.PICKUP_ALL) {
            return originalCanItemQuickReplace(slot, stack, allowOverflow);
        }

        return AbstractContainerMenu.canItemQuickReplace(slot, stack, allowOverflow);
    }

    /**
     * A copy of the original {@link AbstractContainerMenu#canItemQuickReplace} method, kept here since the original is
     * altered by the isSameItemSameComponents redirect above.
     */
    @Unique
    private boolean originalCanItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl = slot == null || !slot.hasItem();
        if (!bl && ItemStack.isSameItemSameComponents(stack, slot.getItem())) {
            return slot.getItem().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxStackSize();
        } else {
            return bl;
        }
    }

    /**
     * Redirects {@link Slot#tryRemove} calls inside {@code AbstractContainerMenu#doClick}.
     * Removes the favorite status from item stacks created by splitting a favorite stack.
     */
    @Redirect(method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;tryRemove(IILnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"
            )
    )
    private Optional<ItemStack> redirectDoClickSlotTryRemove(Slot slot, int min, int max, Player player) {
        if (!ItemFavoritingUtils.isFavorite(slot.getItem()) || min == slot.getItem().getCount()) {
            return slot.tryRemove(min, max, player);
        }

        Optional<ItemStack> stack = slot.tryRemove(min, max, player);
        stack.ifPresent(itemStack -> ItemFavoritingUtils.setFavorite(itemStack, false));

        return stack;
    }
}
