package me.timvinci.terrastorage.gui.widget;

import me.timvinci.terrastorage.mixin.client.PressableWidgetAccessor;
import me.timvinci.terrastorage.util.ButtonsStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Alignment;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

/**
 * A customized button widget.
 */
public class StorageButtonWidget extends ButtonWidget {
    private ButtonsStyle buttonStyle;

    protected StorageButtonWidget(int x, int y, int width, int height, net.minecraft.text.Text message, ButtonsStyle buttonStyle, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
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
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // Draw the button background if the option buttons style is set to default.
        if (this.buttonStyle == ButtonsStyle.DEFAULT)
            this.drawButton(context);

        // Change the text color to yellow if the button is hovered.
        int color = hovered ?
                ColorHelper.getArgb(MathHelper.ceil(this.alpha * 255.0F), 255, 255, 0) :
                ColorHelper.getWhite(this.alpha);

        // Draw text.
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int x = this.getX() + (this.getWidth() - textRenderer.getWidth(this.getMessage())) / 2;
        int y = this.getY() + (this.getHeight() - 9) / 2;

        context.drawText(textRenderer, this.getMessage(), x, y, color, false);
    }
}
