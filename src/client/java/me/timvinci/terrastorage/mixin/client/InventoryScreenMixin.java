package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the InventoryScreen class, adds the inventory storage buttons to the survival inventory screen.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private ImageButton quickStackButton;
    @Unique
    private ImageButton sortInventoryButton;

    public InventoryScreenMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    /**
     * Adds the sort inventory and quick stack to nearby chests buttons once the inventory screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode.
        if (minecraft.player.isSpectator()) {
            return;
        }

        int buttonX = this.leftPos + 128;
        int buttonY = this.height / 2 - 22;
        Tuple<ImageButton, ImageButton> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getA();
        this.addRenderableWidget(quickStackButton);

        sortInventoryButton = buttons.getB();
        this.addRenderableWidget(sortInventoryButton);
    }

    /**
     * Modifies the press action of the recipe book to include the re-positioning of the sort inventory and quick
     * stack to nearby chests buttons.
     * @param original The original press action.
     * @return The modified press action.
     */
    @ModifyArg(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ImageButton;<init>(IIIIIIILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/gui/components/Button$OnPress;)V"
    )
    )
    private Button.OnPress modifyRecipeBookButtonPress(Button.OnPress original) {
        if (minecraft.player.isSpectator()) {
            return original;
        }

        return button -> {
            // Call the original press action.
            original.onPress(button);
            // Reposition the buttons.
            int buttonX = this.leftPos + 128;
            quickStackButton.setPosition(buttonX, quickStackButton.getY());
            buttonX += 24;
            sortInventoryButton.setPosition(buttonX, sortInventoryButton.getY());
        };
    }
}
