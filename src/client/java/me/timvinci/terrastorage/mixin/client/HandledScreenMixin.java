package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
import me.timvinci.terrastorage.util.ButtonsPlacement;
import me.timvinci.terrastorage.util.ButtonsStyle;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.ScreenInteractionUtils;
import me.timvinci.terrastorage.util.*;
import me.timvinci.terrastorage.gui.widget.StorageButtonWidget;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

/**
 * A mixin of the HandledScreen class, adds the storage option buttons to storage screens, and provides item favoriting
 * support.
 * @param <T> The screen handler type.
 */
@Mixin(HandledScreen.class )
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Unique
    private final Identifier favoriteBorder = new Identifier(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");
    @Shadow
    protected T handler;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow @Nullable
    protected Slot focusedSlot;
    

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    /**
     * Adds the storage option buttons once the handled screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode, or if the handled screen is that of the player's inventory.
        if (MinecraftClient.getInstance().player.isSpectator() ||
            handler instanceof CreativeInventoryScreen.CreativeScreenHandler ||
            handler instanceof PlayerScreenHandler) {
            return;
        }

        // Check if the handled screen is that of a storage by searching for a slot whose inventory isn't the player's
        // and is at least 27 in size.
        boolean isStorage = false;
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof PlayerInventory) && slot.inventory.size() >= 27) {
                isStorage = true;
                break;
            }
        }

        if (!isStorage) {
            return;
        }

        boolean isEnderChest = false;
        if (handler instanceof GenericContainerScreenHandler && this.getTitle().equals(Text.translatable("container.enderchest"))) {
            isEnderChest = true;
        }

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
                this.x + this.backgroundWidth + 5 + buttonsXOffset :
                this.x - ((buttonsStyle == ButtonsStyle.DEFAULT ? buttonsWidth : 70) + 5) + buttonsXOffset;
        // Get the height of the container, excluding the player's inventory portion whose height is 94.
        int containerHeight = this.backgroundHeight - 94;
        int buttonSectionHeight = buttonActions.length * buttonsHeight + (buttonActions.length-1) * buttonsSpacing;
        // Centering the buttons vertically alongside the container gui.
        int buttonY = this.y - (buttonSectionHeight - containerHeight) / 2 + buttonsYOffset;

        if (ClientConfigManager.getInstance().getConfig().getButtonsTooltip()) {
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                Tooltip buttonTooltip = LocalizedTextProvider.buttonTooltipCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createStorageButton(storageAction, buttonX, buttonY, buttonsWidth, buttonsHeight, buttonText, buttonsStyle);
                storageButton.setTooltip(buttonTooltip);

                this.addDrawableChild(storageButton);
                buttonY += buttonsHeight + buttonsSpacing;
            }
        }
        else {
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                StorageButtonWidget storageButton = StorageButtonCreator.createStorageButton(storageAction, buttonX, buttonY, buttonsWidth, buttonsHeight, buttonText, buttonsStyle);

                this.addDrawableChild(storageButton);
                buttonY += buttonsHeight + buttonsSpacing;
            }
        }

        // Add the options buttons if it is enabled.
        if (ClientConfigManager.getInstance().getConfig().getDisplayOptionsButton()) {
            int optionsButtonX = (this.width - 120) / 2;
            int optionsButtonY = this.y - 20;
            ButtonWidget optionsButtonWidget = ButtonWidget.builder(
                            Text.translatable("terrastorage.button.options"),
                            onPress -> {
                                client.execute(() -> {
                                    client.setScreen(new TerrastorageOptionsScreen(client.currentScreen));
                                });
                            })
                    .size(120, 15)
                    .position(optionsButtonX, optionsButtonY)
                    .build();
            optionsButtonWidget.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.options")));

            this.addDrawableChild(optionsButtonWidget);
        }
    }

    /**
     * Provides the ability to favorite items stacks.
     */
    @Inject(method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (button != 0 || slot == null || !slot.hasStack() || !handler.getCursorStack().isEmpty()) {
            return;
        }

        boolean modifierIsPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(TerrastorageKeybindings.favoriteItemModifier).getCode());
        boolean playerOwnedSlot = slot.inventory instanceof PlayerInventory;

        if (modifierIsPressed && playerOwnedSlot) {
            ItemStack slotStack = slot.getStack();
            int slotId = this.handler instanceof CreativeInventoryScreen.CreativeScreenHandler ? slot.getIndex() : slot.id;

            boolean toggledValue = !ItemFavoritingUtils.isFavorite(slotStack);
            if (ClientNetworkHandler.sendItemFavoritedPacket(slotId, toggledValue)) {
                ItemFavoritingUtils.setFavorite(slotStack, toggledValue);
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
    private void mouseClickedTail(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir, boolean bl, Slot slot) {
        if (slot == null || slot.inventory.size() < 27) {
            return;
        }

        if (TerrastorageKeybindings.sortInventoryBind.matchesMouse(button)) {
            ClientNetworkHandler.sendSortPacket(slot.inventory instanceof PlayerInventory);
        }
    }

    /**
     * Calls the ScreenInteractionUtils to process a slot click.
     */
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        ScreenInteractionUtils.processSlotClick(this.client, handler.getCursorStack(), slot, slotId, button, actionType, ci);
    }

    /**
     * Provides the ability to sort inventories through the sort inventory keybind.
     * Injected at TAIL to allow any other logic related to the same keybind to happen before the sorting.
     */
    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (focusedSlot == null || focusedSlot.inventory.size() < 27) {
            return;
        }

        if (TerrastorageKeybindings.sortInventoryBind.matchesKey(keyCode, scanCode)) {
            ClientNetworkHandler.sendSortPacket(focusedSlot.inventory instanceof PlayerInventory);
        }
    }

    /**
     * Draws the favorite border on slots that hold a favorite item stack.
     */
    @Inject(method = "drawSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci, int i, int j, ItemStack itemStack) {
        if (!(slot.inventory instanceof PlayerInventory) || !ItemFavoritingUtils.isFavorite(itemStack)) {
            return;
        }

        context.drawTexture(favoriteBorder, i, j, 0, 0, 16, 16, 16, 16);
    }
}
