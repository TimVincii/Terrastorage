package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;
import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientNetworkHandler;
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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    private final Identifier favoriteBorder = Identifier.of(Reference.MOD_ID, "textures/gui/sprites/favorite_border.png");
    @Shadow
    protected T handler;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected int x;
    @Shadow protected int y;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    /**
     * Adds the storage option buttons once the handled screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode.
        if (MinecraftClient.getInstance().player.isSpectator())
            return;

        // Return if the slot count (excluding the 36 player inventory slots) is smaller than 27.
        if (handler.slots.size() - 36 < 27) {
            return;
        }

        boolean isEnderChest = false;
        if (handler instanceof GenericContainerScreenHandler && this.getTitle().equals(Text.translatable("container.enderchest"))) {
            isEnderChest = true;
        }

        StorageAction[] buttonActions = getButtonsActions(isEnderChest);

        // Set the button sizes and their spacing.
        int buttonWidth = 70;
        int buttonHeight = 15;
        int buttonSpacing = 2;

        // Place the buttons on the side of the container gui.
        int buttonX = ClientConfigManager.getInstance().getConfig().getButtonsPlacement() == ButtonsPlacement.RIGHT?
                x + backgroundWidth + 5 :
                x - (buttonWidth + 5);
        // Get the height of the container, excluding the player's inventory portion whose height is 94.
        int containerHeight = backgroundHeight - 94;
        int buttonSectionHeight = buttonActions.length * buttonHeight + (buttonActions.length-1) * buttonSpacing;
        // Centering the buttons vertically alongside the container gui.
        int buttonY = y - (buttonSectionHeight - containerHeight) / 2;

        if (ClientConfigManager.getInstance().getConfig().getButtonsStyle() == ButtonsStyle.TEXT_ONLY) {
            // Create the buttons with the width of their text.
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);
                buttonWidth = textRenderer.getWidth(buttonText) + 6;
                Tooltip buttonTooltip = LocalizedTextProvider.buttonTooltipCache.get(storageAction);
                StorageButtonWidget button = StorageButtonCreator.createStorageButton(storageAction, buttonText, buttonTooltip, buttonX, buttonY, buttonWidth, buttonHeight);
                this.addDrawableChild(button);

                buttonY += buttonHeight + buttonSpacing;
            }
        } else {
            // Create the buttons with a set width.
            for (StorageAction storageAction : buttonActions) {
                Text buttonText = LocalizedTextProvider.buttonTextCache.get(storageAction);;
                Tooltip buttonTooltip = LocalizedTextProvider.buttonTooltipCache.get(storageAction);
                StorageButtonWidget button = StorageButtonCreator.createStorageButton(storageAction, buttonText, buttonTooltip, buttonX, buttonY, buttonWidth, buttonHeight);
                this.addDrawableChild(button);

                buttonY += buttonHeight + buttonSpacing;
            }
        }

        // Add the options buttons if it is enabled.
        if (ClientConfigManager.getInstance().getConfig().getDisplayOptionsButton()) {
            int optionsButtonX = (client.currentScreen.width - 120) / 2;
            int optionsButtonY = y - 20;
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

    @Unique
    private StorageAction[] getButtonsActions(boolean isEnderChest) {
        StorageAction[] allActions = StorageAction.values();
        return Arrays.copyOf(allActions, allActions.length - (isEnderChest ? 2 : 1));
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
            if (ClientNetworkHandler.sendItemFavoritedPayload(slotId, toggledValue)) {
                ItemFavoritingUtils.setFavorite(slotStack, toggledValue);
            }

            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /**
     * Calls the ScreenInteractionUtils to process a slot click.
     */
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        ScreenInteractionUtils.processSlotClick(this.client, this.handler.getCursorStack(), slot, slotId, button, actionType, ci);
    }

    /**
     * Draws the favorite border on slots that hold a favorite item stack.
     */
    @Inject(method = "drawSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci, int i, int j, ItemStack itemStack) {
        if (!(slot.inventory instanceof PlayerInventory) || !ItemFavoritingUtils.isFavorite(itemStack)) {
            return;
        }

        context.drawTexture(RenderLayer::getGuiTextured, favoriteBorder, i, j, 0, 0, 16, 16, 16, 16);
    }
}
