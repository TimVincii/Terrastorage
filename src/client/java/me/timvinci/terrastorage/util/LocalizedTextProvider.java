package me.timvinci.terrastorage.util;

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

    public static Text[] getOptionButtonsTooltipText() {
        String[] tooltipKeys = {
                "terrastorage.option.tooltip.display_options_button",
                "terrastorage.option.tooltip.hotbar_protection",
                "terrastorage.option.tooltip.buttons_style",
                "terrastorage.option.tooltip.buttons_placement",
                "terrastorage.option.tooltip.sort_type"
        };

        Text[] optionButtonsTooltipText = new Text[tooltipKeys.length];
        for (int i = 0; i < tooltipKeys.length; i++) {
            optionButtonsTooltipText[i] = Text.translatable(tooltipKeys[i]);
        }

        return optionButtonsTooltipText;
    }
}
