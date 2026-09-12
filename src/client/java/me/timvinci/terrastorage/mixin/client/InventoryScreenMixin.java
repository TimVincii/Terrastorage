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

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        Tuple<ImageButton, ImageButton> buttons = StorageButtonCreator.createInventoryButtons(buttonX, buttonY);
        quickStackButton = buttons.getA();
        this.addRenderableWidget(quickStackButton);

        sortInventoryButton = buttons.getB();
        this.addRenderableWidget(sortInventoryButton);
    }

    /**
     * Modifies the press action argument of the {@link ImageButton} constructor inside {@code InventoryScreen#init}.
     * This version has no dedicated recipe book toggle method to inject into, so the press action of the recipe book
     * button is wrapped to also reposition the sort inventory and quick stack to nearby chests buttons.
     */
    @ModifyArg(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ImageButton;<init>(IIIIIIILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/gui/components/Button$OnPress;)V"
    )
    )
    private Button.OnPress modifyInitImageButtonOnPress(Button.OnPress original) {
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
