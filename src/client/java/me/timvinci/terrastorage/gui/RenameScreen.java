package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.network.ClientNetworkHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * The rename screen.
 */
public class RenameScreen extends Screen {
    private final Screen parent;
    private final String currentName;

    public RenameScreen(Screen parent, String currentName) {
        super(Component.empty());
        this.parent = parent;
        this.currentName = currentName;
    }

    /**
     * Creates the widgets displayed on the rename screen.
     */
    @Override
    protected void init() {
        super.init();

        // Set the sizes of the widgets.
        int textFieldWidth = 220;
        int resetButtonWidth = 40;
        int renameButtonWidth = 150;
        int height = 20;

        // Name field widget.
        int textFieldX = (this.width - (textFieldWidth + 5 + resetButtonWidth)) / 2;
        int textFieldY = (this.height - height) / 2;
        EditBox nameTextField = new EditBox(
            this.font,
            textFieldX,
            textFieldY,
            textFieldWidth,
            height,
            Component.empty());
        nameTextField.setMaxLength(25);
        nameTextField.setValue(currentName);

        this.addRenderableWidget(nameTextField);
        this.setFocused(nameTextField);

        // Reset button widget.
        Button resetButtonWidget = Button.builder(
            Component.translatable("terrastorage.button.reset"),
            onPress -> {
                nameTextField.setValue("");
            })
            .size(resetButtonWidth, height)
            .pos(textFieldX + textFieldWidth + 5, textFieldY)
        .build();
        resetButtonWidget.setTooltip(Tooltip.create(Component.translatable("terrastorage.button.tooltip.reset")));
        this.addRenderableWidget(resetButtonWidget);

        // Rename button widget.
        int renameButtonX = (this.width - renameButtonWidth) / 2;
        int renameButtonY = this.height - 40;
        Button renameButtonWidget = Button.builder(
            Component.translatable("terrastorage.button.rename"),
            onPress -> rename(nameTextField.getValue()))
            .size(renameButtonWidth, height)
            .pos(renameButtonX, renameButtonY)
        .build();
        this.addRenderableWidget(renameButtonWidget);
    }

    @Override
    public void onClose() {
        // Set the screen to the parent screen when this screen is closed.
        this.minecraft.setScreen(parent);
    }

    /**
     * Stops the screen from pausing the game.
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Sends the rename payload.
     * @param newName The new name of the storage.
     */
    private void rename(String newName) {
        if (!newName.equals(currentName)) {
            ClientNetworkHandler.sendRenamePayload(newName);
        }

        this.onClose();
    }
}
