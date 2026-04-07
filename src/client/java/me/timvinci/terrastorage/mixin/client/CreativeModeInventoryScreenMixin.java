package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.ScreenInteractionUtils;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
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
 * A mixin of the CreativeModeInventoryScreen class, adds the inventory storage buttons to the creative inventory screen,
 * and item favoriting related logic.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Unique
    private ImageButton quickStackButton;
    @Unique
    private ImageButton sortInventoryButton;
    @Shadow
    private static CreativeModeTab selectedTab;
    @Shadow @Nullable
    private Slot destroyItemSlot;

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    /**
     * Stops favorite items from being removed by the 'delete item' slot, and calls the ScreenInteractionUtils class to
     * process the slot click.
     */
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(@Nullable Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        ItemStack cursorStack = this.menu.getCarried();
        if (Objects.equals(slot, destroyItemSlot) && !cursorStack.isEmpty() && ItemFavoritingUtils.isFavorite(cursorStack)) {
            ci.cancel();
            return;
        }

        ScreenInteractionUtils.processSlotClick(this.minecraft, cursorStack, slot, slotId, buttonNum, containerInput, ci);
    }

    /**
     * Stops the client from deleting favorite items when the 'delete item' slot is shift pressed.
     */
    @Redirect(method = "slotClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void redirectSetStackNoCallbacks(Slot slot, ItemStack emptyStack) {
        if (slot.hasItem() && ItemFavoritingUtils.isFavorite(slot.getItem())) {
            return;
        }

        slot.set(emptyStack);
    }

    /**
     * Stops favorite items from being deleted when the 'delete item' slot is shift pressed.
     */
    @Redirect(method = "slotClicked",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemAdd(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void redirectClickCreativeStack(MultiPlayerGameMode interactionManager, ItemStack clicked, int i, @Nullable Slot slot, int slotId, int buttonNum, ContainerInput containerInput) {
        if (containerInput != ContainerInput.QUICK_MOVE) {
            interactionManager.handleCreativeModeItemAdd(clicked, i);
            return;
        }

        if (i < 0 || i >= this.minecraft.player.inventoryMenu.getItems().size()) {
            this.minecraft.player.sendSystemMessage(Component.translatable("terrastorage.message.general_error"));
            return;
        }

        try {
            ItemStack playerStack = this.minecraft.player.inventoryMenu.getItems().get(i);

            // If the stack is favorited or empty, skip this call entirely
            if (playerStack.isEmpty() || ItemFavoritingUtils.isFavorite(playerStack)) {
                return;
            }

            interactionManager.handleCreativeModeItemAdd(clicked, i);
        }
        catch (Exception e) {
            this.minecraft.player.sendSystemMessage(Component.translatable("terrastorage.message.general_error"));
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
                    target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;selectTab(Lnet/minecraft/world/item/CreativeModeTab;)V")
    )
    private void onInit(CallbackInfo ci) {
        int buttonX = this.leftPos + 138;
        int buttonY = this.topPos + 19;
        Tuple<ImageButton, ImageButton> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getA();
        this.addRenderableWidget(quickStackButton);

        sortInventoryButton = buttons.getB();
        this.addRenderableWidget(sortInventoryButton);
    }

    /**
     * Modifies the visibility of the buttons to only appear in the inventory tab.
     */
    @Inject(method = "selectTab", at = @At("TAIL"))
    private void onSetSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (BuiltInRegistries.CREATIVE_MODE_TAB.wrapAsHolder(tab).is(CreativeModeTabs.INVENTORY)) {
            quickStackButton.visible = true;
            sortInventoryButton.visible = true;
        }
        else {
            quickStackButton.visible = false;
            sortInventoryButton.visible = false;
        }
    }
}
