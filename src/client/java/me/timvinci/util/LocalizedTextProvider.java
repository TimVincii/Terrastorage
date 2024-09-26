package me.timvinci.util;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class that involves getting text from the language file.
 */
public class LocalizedTextProvider {
    public static final Map<StorageAction, Text> buttonTextCache = new EnumMap<>(StorageAction.class);
    public static final Map<StorageAction, Tooltip> buttonTooltipCache = new EnumMap<>(StorageAction.class);

    public static void initializeButtonCaches() {
        for (StorageAction action : StorageAction.values()) {
            buttonTextCache.put(action, Text.translatable("terrastorage.button." + action.name().toLowerCase(Locale.ENGLISH)));
            buttonTooltipCache.put(action, Tooltip.of(Text.translatable("terrastorage.button.tooltip." + action.name().toLowerCase(Locale.ENGLISH))));
        }
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
                .append(Text.translatable("terrastorage.option." + propertyKey + "." + currentValue.name().toLowerCase(Locale.ENGLISH)));
    }
}
