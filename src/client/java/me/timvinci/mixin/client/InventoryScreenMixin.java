package me.timvinci.mixin.client;

import me.timvinci.network.ClientNetworkHandler;
import me.timvinci.util.Reference;
import me.timvinci.util.StorageAction;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin of the InventoryScreen class, adds the inventory option buttons to the inventory screen.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private TexturedButtonWidget sortInventoryButton;
    @Unique
    private TexturedButtonWidget quickStackButton;
    @Unique
    private final ButtonTextures sortButtonTexture = new ButtonTextures(
            Identifier.of(Reference.MOD_ID, "sort_inventory"),
            Identifier.of(Reference.MOD_ID, "sort_inventory_highlighted")
    );
    @Unique
    private final ButtonTextures quickStackButtonTexture = new ButtonTextures(
            Identifier.of(Reference.MOD_ID, "quick_stack"),
            Identifier.of(Reference.MOD_ID, "quick_stack_highlighted")
    );

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
        quickStackButton = new TexturedButtonWidget(
                buttonX,
                buttonY,
                20,
                18,
                quickStackButtonTexture,
                onPress -> {
                    ClientNetworkHandler.sendActionPayload(StorageAction.QUICK_STACK_TO_NEARBY);
                }
        );
        quickStackButton.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.quick_stack_to_nearby")));
        this.addDrawableChild(quickStackButton);

        buttonX += 24;
        sortInventoryButton = new TexturedButtonWidget(
                buttonX,
                buttonY,
                20,
                18,
                sortButtonTexture,
                onPress -> {
                    ClientNetworkHandler.sendPlayerSortPayload();
                }
        );
        sortInventoryButton.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.sort_inventory")));
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
