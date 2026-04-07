package me.timvinci.terrastorage.gui.widget;

import me.timvinci.terrastorage.util.ButtonsStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * A customized button widget.
 */
public class StorageButtonWidget extends Button {
    private ButtonsStyle buttonStyle;

    protected StorageButtonWidget(int x, int y, int width, int height, net.minecraft.network.chat.Component message, ButtonsStyle buttonStyle, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.setButtonStyle(buttonStyle);
    }

    public void setButtonStyle(ButtonsStyle buttonsStyle) {
        this.buttonStyle = buttonsStyle;
    }

    /**
     * Sets the button text color to yellow when the button is hovered, similarly to how it is in Terraria.
     * Supports not drawing the background of the button.
     */
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Draw the button background if the option buttons style is set to default.
        if (this.buttonStyle == ButtonsStyle.DEFAULT)
            this.extractDefaultSprite(graphics);

        // Change the text color to yellow if the button is hovered.
        int color = isHovered ?
                ARGB.color(Mth.ceil(this.alpha * 255.0F), 255, 255, 0) :
                ARGB.white(this.alpha);

        // Draw text.
        Font textRenderer = Minecraft.getInstance().font;
        int x = this.getX() + (this.getWidth() - textRenderer.width(this.getMessage())) / 2;
        int y = this.getY() + (this.getHeight() - 9) / 2;

        graphics.text(textRenderer, this.getMessage(), x, y, color, false);
    }
}
