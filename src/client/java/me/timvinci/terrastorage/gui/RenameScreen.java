package me.timvinci.terrastorage.gui;

import me.timvinci.terrastorage.network.ClientNetworkHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * The rename screen.
 */
public class RenameScreen extends Screen {
    private final Screen parent;
    private final String currentName;

    public RenameScreen(Screen parent, String currentName) {
        super(Text.empty());
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

        int textFieldX = (this.width - (textFieldWidth + 5 + resetButtonWidth)) / 2;
        int textFieldY = (this.height - height) / 2;
        TextFieldWidget nameTextField = new TextFieldWidget(
            textRenderer,
            textFieldX,
            textFieldY,
            textFieldWidth,
            height,
            Text.empty());
        nameTextField.setMaxLength(25);
        nameTextField.setText(currentName);
        this.addDrawableChild(nameTextField);

        ButtonWidget resetButtonWidget = ButtonWidget.builder(
            Text.translatable("terrastorage.button.reset"),
            onPress -> {
                nameTextField.setText("");
            })
            .size(resetButtonWidth, height)
            .position(textFieldX + textFieldWidth + 5, textFieldY)
        .build();
        resetButtonWidget.setTooltip(Tooltip.of(Text.translatable("terrastorage.button.tooltip.reset")));
        this.addDrawableChild(resetButtonWidget);

        int renameButtonX = (this.width - renameButtonWidth) / 2;
        int renameButtonY = this.height - 40;
        ButtonWidget renameButtonWidget = ButtonWidget.builder(
            Text.translatable("terrastorage.button.rename"),
            onPress -> rename(nameTextField.getText()))
            .size(renameButtonWidth, height)
            .position(renameButtonX, renameButtonY)
        .build();
        this.addDrawableChild(renameButtonWidget);
    }

    /**
     * Adds background rendering.
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        // Set the screen to the parent screen when this screen is closed.
        this.client.setScreen(parent);
    }

    /**
     * Stops the screen from pausing the game.
     */
    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * Sends the rename payload.
     * @param newName The new name of the storage.
     */
    private void rename(String newName) {
        if (!newName.equals(currentName)) {
            ClientNetworkHandler.sendRenamePacket(newName);
        }

        this.close();
    }
}
