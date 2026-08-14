package me.timvinci.terrastorage.gui.widget;

import me.timvinci.terrastorage.mixin.client.AbstractButtonAccessor;
import me.timvinci.terrastorage.util.ButtonsStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * A customized button widget.
 */
public class StorageButtonWidget extends Button {
    private ButtonsStyle buttonStyle;

    public StorageButtonWidget(int x, int y, int width, int height, Component message, ButtonsStyle buttonStyle, OnPress onPress) {
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
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        // Draw the button background if the option buttons style is set to default.
        if (buttonStyle == ButtonsStyle.DEFAULT) {
            context.blitSprite(RenderType::guiTextured, AbstractButtonAccessor.getTextures().get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        }
        // Change the text color to yellow if the button is hovered.
        int i = this.isHovered ? 16776960 : 16777215;
        this.renderString(context, minecraftClient.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
