package me.timvinci.terrastorage.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.util.ButtonsStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * A customized button widget.
 */
public class StorageButtonWidget extends ButtonWidget {

    public StorageButtonWidget(int x, int y, int width, int height, Text message, Tooltip toolTip, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(toolTip);
    }

    /**
     * Sets the button text color to yellow when the button is hovered, similarly to how it is in Terraria.
     * Supports not drawing the background of the button.
     */
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        // Draw the button background if the option buttons style is set to default.
        if (ClientConfigManager.getInstance().getConfig().getButtonsStyle() == ButtonsStyle.DEFAULT) {
            context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, getTextureY());
        }
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // Change the text color to yellow if the button is hovered.
        int i = this.hovered ? 16776960 : 16777215;
        this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isSelected()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}
