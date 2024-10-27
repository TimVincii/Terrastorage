package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
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
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private TexturedButtonWidget quickStackButton;
    @Unique
    private TexturedButtonWidget sortInventoryButton;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    /**
     * Adds the sort inventory and quick stack to nearby chests buttons once the inventory screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        // Return if the player is in spectator mode.
        if (client.player.isSpectator()) {
            return;
        }

        int buttonX = this.x + 128;
        int buttonY = this.height / 2 - 22;
        Pair<TexturedButtonWidget, TexturedButtonWidget> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getLeft();
        this.addDrawableChild(quickStackButton);

        sortInventoryButton = buttons.getRight();
        this.addDrawableChild(sortInventoryButton);
    }

    /**
     * Modifies the press action of the recipe book to include the re-positioning of the sort inventory and quick
     * stack to nearby chests buttons.
     * @param original The original press action.
     * @return The modified press action.
     */
    @ModifyArg(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIILnet/minecraft/client/gui/screen/ButtonTextures;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"
        )
    )
    private ButtonWidget.PressAction modifyRecipeBookButtonPress(ButtonWidget.PressAction original) {
        if (client.player.isSpectator()) {
            return original;
        }

        return button -> {
            // Call the original press action.
            original.onPress(button);
            // Reposition the buttons.
            int buttonX = this.x + 128;
            quickStackButton.setPosition(buttonX, quickStackButton.getY());
            buttonX += 24;
            sortInventoryButton.setPosition(buttonX, sortInventoryButton.getY());
        };
    }
}
