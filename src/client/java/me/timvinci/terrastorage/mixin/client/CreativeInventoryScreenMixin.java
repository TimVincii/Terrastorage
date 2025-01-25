package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.ScreenInteractionUtils;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * A mixin of the CreativeInventoryScreen class, adds the inventory storage buttons to the creative inventory screen,
 * and item favoriting related logic.
 */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Unique
    private TexturedButtonWidget quickStackButton;
    @Unique
    private TexturedButtonWidget sortInventoryButton;
    @Shadow
    private static ItemGroup selectedTab;
    @Shadow @Nullable
    private Slot deleteItemSlot;

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * Stops favorite items from being removed by the delete item slot, and calls the ScreenInteractionUtils class to
     * process the slot click.
     */
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        ItemStack cursorStack = this.handler.getCursorStack();
        if (Objects.equals(slot, deleteItemSlot) && !cursorStack.isEmpty() && ItemFavoritingUtils.isFavorite(cursorStack)) {
            ci.cancel();
            return;
        }

        ScreenInteractionUtils.processSlotClick(this.client, cursorStack, slot, slotId, button, actionType, ci);
    }

    /**
     * Stops the client from deleting favorite items when the delete item slot is shift pressed.
     */
    @Redirect(method = "onMouseClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;setStackNoCallbacks(Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void redirectSetStackNoCallbacks(Slot slot, ItemStack emptyStack) {
        if (slot.hasStack() && ItemFavoritingUtils.isFavorite(slot.getStack())) {
            System.out.println("Redirected set stack on callbacks");
            return;
        }

        slot.setStackNoCallbacks(emptyStack);
    }

    /**
     * Stops favorite items from being deleted when the delete item slot is shift pressed.
     */
    @Redirect(method = "onMouseClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickCreativeStack(Lnet/minecraft/item/ItemStack;I)V"))
    private void redirectClickCreativeStack(ClientPlayerInteractionManager interactionManager, ItemStack stack, int i, @Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (actionType != SlotActionType.QUICK_MOVE) {
            interactionManager.clickCreativeStack(stack, i);
            return;
        }

        if (i < 0 || i >= this.client.player.playerScreenHandler.getStacks().size()) {
            this.client.player.sendMessage(Text.translatable("terrastorage.message.general_error"), false);
            return;
        }

        try {
            ItemStack playerStack = this.client.player.playerScreenHandler.getStacks().get(i);

            // If the stack is favorited or empty, skip this call entirely
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack)) {
                return;
            }

            interactionManager.clickCreativeStack(stack, i);
        }
        catch (Exception e) {
            this.client.player.sendMessage(Text.translatable("terrastorage.message.general_error"), false);
            TerrastorageClient.CLIENT_LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Adds the sort inventory and quick stack to nearby chests buttons once the creative inventory screen is
     * initializing.
     */
    @Inject(method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    )
    private void onInit(CallbackInfo ci) {
        int buttonX = this.x + 138;
        int buttonY = this.y + 19;
        Pair<TexturedButtonWidget, TexturedButtonWidget> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getLeft();
        this.addDrawableChild(quickStackButton);

        sortInventoryButton = buttons.getRight();
        this.addDrawableChild(sortInventoryButton);
    }

    /**
     * Modifies the visibility of the buttons to only appear in the inventory tab.
     */
    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void onSetSelectedTab(ItemGroup group, CallbackInfo ci) {
        if (Registries.ITEM_GROUP.getEntry(group).matchesKey(ItemGroups.INVENTORY)) {
            quickStackButton.visible = true;
            sortInventoryButton.visible = true;
        }
        else {
            quickStackButton.visible = false;
            sortInventoryButton.visible = false;
        }
    }
}
