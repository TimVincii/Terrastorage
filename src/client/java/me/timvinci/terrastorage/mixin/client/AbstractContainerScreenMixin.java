package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.config.ServerConfigHolder;
import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
import me.timvinci.terrastorage.util.*;
import me.timvinci.terrastorage.gui.widget.StorageButtonWidget;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
 * @param <T> The screen handler type.
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
     * Adds the storage option buttons once the handled screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode, or if the handled screen is that of the player's inventory.
        if (Minecraft.getInstance().player.isSpectator() ||
            menu instanceof CreativeModeInventoryScreen.ItemPickerMenu ||
            menu instanceof InventoryMenu) {
            return;
        }

        // Check if the handled screen is a storage.
        // Primary check scans for a non player slot with an inventory size of at least 27.
        // Secondary check counts the amount of non player slots and is for screen handlers whose slots list doesn't
        // provide a proper reference to the inventory.
        boolean largeNonPlayerInventory = false;
        int nonPlayerSlotCount = 0;
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof Inventory)) {
                if (slot.container.getContainerSize() >= 27) {
                    largeNonPlayerInventory = true;
                    break;
                }

                nonPlayerSlotCount++;
            }
        }

        // If both checks fail, this is (most very likely) not a storage.
        if (!largeNonPlayerInventory && nonPlayerSlotCount < 27) {
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
     * Provides the ability to favorite items stacks.
     */
    @Inject(method = "mouseClicked",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    private void mouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (click.button() != 0 || slot == null || !slot.hasItem() || !menu.getCarried().isEmpty()) {
            return;
        }

        boolean modifierIsPressed = InputConstants.isKeyDown(minecraft.getWindow(), KeyMappingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getValue());
        boolean playerOwnedSlot = slot.container instanceof Inventory;

        if (modifierIsPressed && playerOwnedSlot) {
            if (!ServerConfigHolder.enableItemFavoriting) {
                minecraft.player.sendSystemMessage(Component.translatable("terrastorage.message.item_favoriting_disabled"));
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
     * Provides the ability to sort inventories through the sort inventory keybind.
     * Injected at TAIL to allow any other logic related to the same keybind to happen before the sorting.
     */
    @Inject(method = "mouseClicked", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void mouseClickedTail(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (slot == null || slot.container.getContainerSize() < 27) {
            return;
        }

        if (TerrastorageKeybindings.sortInventoryBind.matchesMouse(click)) {
            ClientNetworkHandler.sendSortPayload(slot.container instanceof Inventory);
        }
    }

    /**
     * Calls the ScreenInteractionUtils to process a slot click.
     */
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, ContainerInput containerInput, CallbackInfo ci) {
        ScreenInteractionUtils.processSlotClick(this.minecraft, this.menu.getCarried(), slot, slotId, button, containerInput, ci);
    }

    /**
     * Provides the ability to sort inventories through the sort inventory keybind.
     * Injected at TAIL to allow any other logic related to the same keybind to happen before the sorting.
     */
    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void onKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot == null || hoveredSlot.container.getContainerSize() < 27) {
            return;
        }

        if (TerrastorageKeybindings.sortInventoryBind.matches(input)) {
            ClientNetworkHandler.sendSortPayload(hoveredSlot.container instanceof Inventory);
        }
    }

    /**
     * Draws the favorite border on slots that hold a favorite item stack.
     */
    // TODO
    @Inject(method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void drawSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci, int i, int j, ItemStack itemStack, boolean bl, boolean bl2, ItemStack itemStack2, String string) {
        if (!(slot.container instanceof Inventory) || !ItemFavoritingUtils.isFavorite(itemStack)) {
            return;
        }

        BorderVisibility borderVisibility = ClientConfigManager.getInstance().getConfig().getBorderVisibility();
        if (borderVisibility == BorderVisibility.NEVER) {
            return;
        }

        boolean needsModifierPressed = borderVisibility == BorderVisibility.ON_PRESS || borderVisibility == BorderVisibility.ON_PRESS_NON_HOTBAR;

        if (!needsModifierPressed || InputConstants.isKeyDown(minecraft.getWindow(),
                KeyMappingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getValue())) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, favoriteBorder, i, j, 0, 0, 16, 16, 16, 16);
        }
    }
}
