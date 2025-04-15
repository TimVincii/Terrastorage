package me.timvinci.terrastorage.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * Simple text styling utility.
 */
public class TextStyler {

    private static final Formatting TITLE_COLOR = Formatting.AQUA;
    private static final Formatting TEXT_COLOR = Formatting.WHITE;
    private static final Formatting VALUE_COLOR = Formatting.YELLOW;
    private static final Formatting ERROR_COLOR = Formatting.RED;
    private static final Formatting WARNING_COLOR = Formatting.GOLD;

    private static final Formatting ENABLED_COLOR = Formatting.GREEN;
    private static final Formatting DISABLED_COLOR = Formatting.RED;

    private static final int[] ENUM_COLORS = new int[] {
            0xB8F296, // Bright green
            0xFFE28A, // Bright amber
            0xFF8888, // Bright red
            0x9DE6F2, // Bright cyan
            0xFFB27B, // Bright orange
            0xE0A4F5, // Bright purple
            0x8CB4FF, // Bright blue
            0xFFE599  // Bright gold
    };

    public static MutableText styleTitle(String title) {
        return Text.literal(title).styled(style -> style.withBold(true).withColor(TITLE_COLOR));
    }

    public static MutableText styleText(MutableText text) {
        return text.styled(style -> style.withBold(false).withColor(TEXT_COLOR));
    }

    public static <T> MutableText styleGetProperty(String propertyName, T value, String valueUnit) {
        return styleTitle(propertyName + ": ")
                    .append(Text.literal(value + valueUnit)
                    .styled(style -> style.withBold(false).withColor(VALUE_COLOR))
        );
    }

    public static <T> MutableText stylePropertyUpdated(String propertyName, T value, String valueUnit) {
        return styleTitle(propertyName + " Updated\n")
                    .append(Text.literal("New value: ")
                    .styled(style -> style.withBold(false).withColor(TEXT_COLOR)))
                    .append(Text.literal(value + valueUnit)
                    .styled(style -> style.withColor(VALUE_COLOR))
        );
    }

    public static MutableText styleBooleanValue(boolean value) {
        return Text.translatable("terrastorage.option." + (value ? "enabled" : "disabled"))
                .styled(style -> style.withColor(value ? ENABLED_COLOR : DISABLED_COLOR));
    }

    public static <T extends Enum<T>> MutableText styleEnumValue(String propertyKey, T value) {
        return Text.translatable("terrastorage.option." + propertyKey + "." + value.name().toLowerCase(Locale.ENGLISH))
                .styled(style -> style.withColor(ENUM_COLORS[value.ordinal() % ENUM_COLORS.length]));
    }

    public static MutableText error(String messageKey) {
        return Text.translatable(messageKey).formatted(ERROR_COLOR);
    }

    public static MutableText warning(String messageKey) {
        return Text.translatable(messageKey).formatted(WARNING_COLOR);
    }
}
