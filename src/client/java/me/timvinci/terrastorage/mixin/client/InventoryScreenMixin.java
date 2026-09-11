package me.timvinci.terrastorage.mixin.client;

import me.timvinci.terrastorage.gui.widget.StorageButtonCreator;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.Component;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the InventoryScreen class, adds the inventory storage buttons to the survival inventory screen.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {
    @Unique
    private ImageButton quickStackButton;
    @Unique
    private ImageButton sortInventoryButton;

    public InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBook, Inventory inventory, Component title) {
        super(menu, recipeBook, inventory, title);
    }

    /**
     * Injects into {@code InventoryScreen#init} at TAIL.
     * Adds the sort inventory and quick stack to nearby chests buttons once the inventory screen is initializing.
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInitTail(CallbackInfo ci) {
        // Return if the player is in spectator mode.
        if (minecraft.player.isSpectator()) {
            return;
        }

        int buttonX = this.leftPos + 128;
        int buttonY = this.height / 2 - 22;
        Pair<ImageButton, ImageButton> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getFirst();
        this.addRenderableWidget(quickStackButton);

        sortInventoryButton = buttons.getSecond();
        this.addRenderableWidget(sortInventoryButton);
    }

    /**
     * Injects into {@code InventoryScreen#onRecipeBookButtonClick} at TAIL.
     * Repositions the inventory buttons once the recipe book is toggled.
     */
    @Inject(method = "onRecipeBookButtonClick", at = @At("TAIL"))
    private void onRecipeBookButtonClickTail(CallbackInfo ci) {
        int buttonX = this.leftPos + 128;
        quickStackButton.setPosition(buttonX, quickStackButton.getY());
        buttonX += 24;
        sortInventoryButton.setPosition(buttonX, sortInventoryButton.getY());
    }
}
