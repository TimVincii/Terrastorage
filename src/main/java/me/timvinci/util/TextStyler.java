package me.timvinci.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Simple text styling utility.
 */
public class TextStyler {

    private static final Formatting TITLE_COLOR = Formatting.AQUA;
    private static final Formatting TEXT_COLOR = Formatting.WHITE;
    private static final Formatting VALUE_COLOR = Formatting.YELLOW;
    private static final Formatting ERROR_COLOR = Formatting.RED;

    public static MutableText styleTitle(String text) {
        return Text.literal(text).formatted(TITLE_COLOR, Formatting.BOLD);
    }

    public static MutableText styleText(String text) {
        return Text.literal(text).formatted(TEXT_COLOR);
    }

    public static MutableText styleValue(String value) {
        return Text.literal(value).formatted(VALUE_COLOR);
    }

    public static MutableText styleKeyValue(String key, String value) {
        return styleText(key + ": ").append(styleValue(value));
    }

    public static MutableText styleError(String errorMessageKey) {
        return Text.translatable(errorMessageKey).formatted(ERROR_COLOR);
    }
}
