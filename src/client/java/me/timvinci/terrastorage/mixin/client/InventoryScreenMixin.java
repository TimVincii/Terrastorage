package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
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
public abstract class InventoryScreenMixin extends RecipeBookScreen<PlayerScreenHandler> {
    @Unique
    private TexturedButtonWidget quickStackButton;
    @Unique
    private TexturedButtonWidget sortInventoryButton;

    public InventoryScreenMixin(PlayerScreenHandler handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
        super(handler, recipeBook, inventory, title);
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
     * Repositions the inventory buttons once the recipe book is toggled.
     */
    @Inject(method = "onRecipeBookToggled", at = @At("TAIL"))
    private void onRecipeBookToggledTail(CallbackInfo ci) {
        int buttonX = this.x + 128;
        quickStackButton.setPosition(buttonX, quickStackButton.getY());
        buttonX += 24;
        sortInventoryButton.setPosition(buttonX, sortInventoryButton.getY());
    }
}
