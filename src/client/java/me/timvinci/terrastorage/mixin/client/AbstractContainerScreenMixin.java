package me.timvinci.terrastorage.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.config.ServerConfigHolder;
import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.gui.widget.StorageButtonWidget;
import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
import me.timvinci.terrastorage.util.*;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * A mixin of the AbstractContainerScreen class, adds the storage option buttons to storage screens, and provides item favoriting
 * support.
 * @param <T> The container screen type.
 */
@Mixin(AbstractContainerScreen.class )
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    @Unique
    private final Identifier favoriteBorder = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");
    @Shadow
    protected T menu;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow @Nullable
    protected Slot hoveredSlot;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    /**
     * Injects into {@code AbstractContainerScreen#init} at TAIL to add the storage option buttons once the container
     * screen is finished initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInitTail(CallbackInfo ci) {
        Player player = minecraft.player;
        // Return if the player is in spectator mode, or if the container screen is that of the player's inventory.
        if (player.isSpectator() ||
            menu instanceof CreativeModeInventoryScreen.ItemPickerMenu ||
            menu instanceof InventoryMenu) {
            return;
        }

        // Return if the container screen isn't that of a storage.
        if (!InventoryUtils.isStorageMenu(menu, player)) {
            return;
        }

        // Add the options buttons if it is enabled.
        if (ClientConfigManager.getInstance().getConfig().getDisplayOptionsButton()) {
            int optionsButtonX = (this.width - 120) / 2;
            int optionsButtonY = this.topPos - 20;
            Button optionsButtonWidget = Button.builder(
                            Component.translatable("terrastorage.button.options"),
                            onPress -> {
                                minecraft.execute(() -> {
                                    minecraft.setScreen(new TerrastorageOptionsScreen(minecraft.screen));
                                });
                            })
                    .size(120, 15)
                    .pos(optionsButtonX, optionsButtonY)
                    .build();
            optionsButtonWidget.setTooltip(Tooltip.create(Component.translatable("terrastorage.button.tooltip.options")));

            this.addRenderableWidget(optionsButtonWidget);
        }

        if (!ClientConfigManager.getInstance().getConfig().getButtonsEnabled()) {
            return;
        }

        boolean isEnderChest = menu instanceof ChestMenu && this.getTitle().equals(Component.translatable("container.enderchest"));
        StorageAction[] buttonActions = StorageAction.getButtonsActions(isEnderChest);

        ButtonsStyle buttonsStyle = ClientConfigManager.getInstance().getConfig().getButtonsStyle();
        // Set the buttons offset.
        int buttonsXOffset = ClientConfigManager.getInstance().getConfig().getButtonsXOffset();
        int buttonsYOffset = ClientConfigManager.getInstance().getConfig().getButtonsYOffset();

        // Set the button dimensions and vertical spacing.
        int buttonsWidth = ClientConfigManager.getInstance().getConfig().getButtonsWidth();
        int buttonsHeight = ClientConfigManager.getInstance().getConfig().getButtonsHeight();
        int buttonsSpacing = ClientConfigManager.getInstance().getConfig().getButtonsSpacing();

        // Place the buttons on the side of the container gui.
        int buttonX = ClientConfigManager.getInstance().getConfig().getButtonsPlacement() == ButtonsPlacement.RIGHT?
                this.leftPos + this.imageWidth + 5 + buttonsXOffset :
                this.leftPos - ((buttonsStyle == ButtonsStyle.DEFAULT ? buttonsWidth : 70) + 5) + buttonsXOffset;
        // Get the height of the container, excluding the player's inventory portion whose height is 94.
        int containerHeight = this.imageHeight - 94;
        int buttonSectionHeight = buttonActions.length * buttonsHeight + (buttonActions.length-1) * buttonsSpacing;
        // Centering the buttons vertically alongside the container gui.
        int buttonY = this.topPos - (buttonSectionHeight - containerHeight) / 2 + buttonsYOffset;

        if (ClientConfigManager.getInstance().getConfig().getButtonsTooltip()) {
            for (StorageAction storageAction : buttonActions) {
                Component buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                Tooltip buttonTooltip = LocalizedTextProvider.buttonTooltipCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createStorageButton(storageAction, buttonX, buttonY, buttonsWidth, buttonsHeight, buttonText, buttonsStyle);
                storageButton.setTooltip(buttonTooltip);

                this.addRenderableWidget(storageButton);
                buttonY += buttonsHeight + buttonsSpacing;
            }
        }
        else {
            for (StorageAction storageAction : buttonActions) {
                Component buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createStorageButton(storageAction, buttonX, buttonY, buttonsWidth, buttonsHeight, buttonText, buttonsStyle);

                this.addRenderableWidget(storageButton);
                buttonY += buttonsHeight + buttonsSpacing;
            }
        }
    }

    /**
     * Injects into {@link AbstractContainerScreen#mouseClicked} immediately after the hovered slot is assigned to
     * a local variable. Provides the ability to favorite item stacks.
     */
    @Inject(method = "mouseClicked",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    private void onMouseClickedAfterGetHoveredSlot(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (click.button() != 0 || slot == null || !slot.hasItem() || !menu.getCarried().isEmpty()) {
            return;
        }

        boolean modifierIsPressed = InputConstants.isKeyDown(minecraft.getWindow(), KeyBindingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getValue());
        boolean playerOwnedSlot = InventoryUtils.isPlayerSlot(slot, minecraft.player);

        if (modifierIsPressed && playerOwnedSlot) {
            if (!ServerConfigHolder.enableItemFavoriting) {
                minecraft.player.displayClientMessage(Component.translatable("terrastorage.message.item_favoriting_disabled"), false);
            }
            else {
                ItemStack slotStack = slot.getItem();
                int slotId = this.menu instanceof CreativeModeInventoryScreen.ItemPickerMenu ? slot.getContainerSlot() : slot.index;
                boolean toggledValue = !ItemFavoritingUtils.isFavorite(slotStack);
                if (ClientNetworkHandler.sendItemFavoritedPayload(slotId, toggledValue)) {
                    ItemFavoritingUtils.setFavorite(slotStack, toggledValue);
                }
            }

            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /**
     * Injects into {@link AbstractContainerScreen#mouseClicked} at TAIL, allowing any other logic related to the same
     * keybind to happen before the sorting.
     * Provides the ability to sort inventories through the sort inventory keybind if it's a mouse keybind.
     */
    @Inject(method = "mouseClicked", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onMouseClickedTail(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (slot == null || !TerrastorageKeybindings.sortInventoryBind.matchesMouse(click)) {
            return;
        }

        boolean playerInventory = InventoryUtils.isPlayerSlot(slot, minecraft.player);
        if (playerInventory || InventoryUtils.isStorageMenu(menu, minecraft.player)) {
            ClientNetworkHandler.sendSortPayload(playerInventory);
        }
    }

    /**
     * Injects into {@code AbstractContainerScreen#slotClicked}
     * Calls {@link ScreenInteractionUtils#processSlotClick} to process a slot click.
     */
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At("HEAD"), cancellable = true)
    private void onSlotClickedHead(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        ScreenInteractionUtils.processSlotClick(this.minecraft, this.menu.getCarried(), slot, slotId, button, actionType, ci);
    }

    /**
     * Injects into {@link AbstractContainerScreen#keyPressed} at TAIL, allowing any other logic related to the same
     * keybind to happen before the sorting.
     * Provides the ability to sort inventories through the sort inventory keybind if it's a keyboard keybind.
     */
    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void onKeyPressedTail(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot == null || !TerrastorageKeybindings.sortInventoryBind.matches(input)) {
            return;
        }

        boolean playerInventory = InventoryUtils.isPlayerSlot(hoveredSlot, minecraft.player);
        if (playerInventory || InventoryUtils.isStorageMenu(menu, minecraft.player)) {
            ClientNetworkHandler.sendSortPayload(playerInventory);
        }
    }

    /**
     * Injects into {@code AbstractContainerScreen#extractSlot} immediately before {@link GuiGraphics#renderItemDecorations(Font, ItemStack, int, int, String)}
     * is called.
     * Draws the favorite border on slots that hold a favorite item stack.
     */
    @Inject(method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRenderSlotBeforeRenderItemDecorations(GuiGraphics context, Slot slot, int mouseX, int mouseY, CallbackInfo ci, int i, int j, ItemStack itemStack, boolean bl, boolean bl2, ItemStack itemStack2, String string) {
        if (!InventoryUtils.isPlayerSlot(slot, minecraft.player) || !ItemFavoritingUtils.isFavorite(itemStack)) {
            return;
        }

        BorderVisibility borderVisibility = ClientConfigManager.getInstance().getConfig().getBorderVisibility();
        if (borderVisibility == BorderVisibility.NEVER) {
            return;
        }

        boolean needsModifierPressed = borderVisibility == BorderVisibility.ON_PRESS || borderVisibility == BorderVisibility.ON_PRESS_NON_HOTBAR;

        if (!needsModifierPressed || InputConstants.isKeyDown(minecraft.getWindow(),
                KeyBindingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getValue())) {
            context.blit(RenderPipelines.GUI_TEXTURED, favoriteBorder, i, j, 0, 0, 16, 16, 16, 16);
        }
    }
}
