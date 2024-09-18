package me.timvinci.util;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

/**
 * Helper class that involves getting text from the language file.
 */
public class LocalizedTextProvider {

    public static Text[] getButtonsText(boolean removeRename) {
        String[] buttonKeys = {
                "terrastorage.button.loot_all",
                "terrastorage.button.deposit_all",
                "terrastorage.button.quick_stack",
                "terrastorage.button.restock",
                "terrastorage.button.sort_items",
                "terrastorage.button.rename"
        };

        Text[] buttonsText = new Text[removeRename ? buttonKeys.length - 1 : buttonKeys.length];
        for (int i = 0; i < buttonsText.length; i++) {
            buttonsText[i] = Text.translatable(buttonKeys[i]);
        }

        return buttonsText;
    }

    public static Tooltip[] getButtonsTooltip(boolean removeRename) {
        String[] tooltipKeys = {
                "terrastorage.button.tooltip.loot_all",
                "terrastorage.button.tooltip.deposit_all",
                "terrastorage.button.tooltip.quick_stack",
                "terrastorage.button.tooltip.restock",
                "terrastorage.button.tooltip.sort_items",
                "terrastorage.button.tooltip.rename"
        };

        Tooltip[] buttonsTooltips = new Tooltip[removeRename ? tooltipKeys.length - 1 : tooltipKeys.length];
        for (int i = 0; i < buttonsTooltips.length; i++) {
            buttonsTooltips[i] = Tooltip.of(Text.translatable(tooltipKeys[i]));
        }

        return buttonsTooltips;
    }

    public static Tooltip[] getOptionButtonsTooltip() {
        String[] tooltipKeys = {
                "terrastorage.option.tooltip.display_options_button",
                "terrastorage.option.tooltip.hotbar_protection",
                "terrastorage.option.tooltip.buttons_style",
                "terrastorage.option.tooltip.buttons_placement",
                "terrastorage.option.tooltip.sort_type"
        };

        Tooltip[] configButtonsTooltips = new Tooltip[tooltipKeys.length];
        for (int i = 0; i < tooltipKeys.length; i++) {
            configButtonsTooltips[i] = Tooltip.of(Text.translatable(tooltipKeys[i]));
        }

        return configButtonsTooltips;
    }

    /**
     * Retrieves the text that is displayed on boolean options in the Terrastorage options screen.
     * @param propertyKey The key of the option.
     * @param currentValue The current value of the option.
     * @return The text to be displayed on the button.
     */
    public static Text getBooleanOptionText(String propertyKey, boolean currentValue) {
        return Text.translatable("terrastorage.option." + propertyKey)
                .append(": ")
                .append(Text.translatable("terrastorage.option." + propertyKey + "." + currentValue));
    }

    /**
     * Retrieves the text that is displayed on enum options in the Terrastorage options screen.
     * @param propertyKey The key of the option.
     * @param currentValue The current value of the option.
     * @return The text to be displayed on the button.
     * @param <T> The enum class of the option.
     */
    public static <T extends Enum<T>> Text getEnumOptionText(String propertyKey, T currentValue) {
        return Text.translatable("terrastorage.option." + propertyKey)
                .append(": ")
                .append(Text.translatable("terrastorage.option." + propertyKey + "." + currentValue.name().toLowerCase()));
    }
}
