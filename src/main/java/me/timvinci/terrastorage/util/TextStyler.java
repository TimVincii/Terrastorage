package me.timvinci.terrastorage.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * Simple text styling utility.
 */
public class TextStyler {

    private static final ChatFormatting TITLE_COLOR = ChatFormatting.AQUA;
    private static final ChatFormatting TEXT_COLOR = ChatFormatting.WHITE;
    private static final ChatFormatting VALUE_COLOR = ChatFormatting.YELLOW;
    private static final ChatFormatting ERROR_COLOR = ChatFormatting.RED;
    private static final ChatFormatting WARNING_COLOR = ChatFormatting.GOLD;

    private static final ChatFormatting ENABLED_COLOR = ChatFormatting.GREEN;
    private static final ChatFormatting DISABLED_COLOR = ChatFormatting.RED;

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

    public static MutableComponent styleTitle(String title) {
        return Component.literal(title).withStyle(style -> style.withBold(true).withColor(TITLE_COLOR));
    }

    public static MutableComponent styleText(MutableComponent text) {
        return text.withStyle(style -> style.withBold(false).withColor(TEXT_COLOR));
    }

    public static <T> MutableComponent styleGetProperty(String propertyName, T value, String valueUnit) {
        return styleTitle(propertyName + ": ")
                    .append(Component.literal(value + valueUnit)
                    .withStyle(style -> style.withBold(false).withColor(VALUE_COLOR))
        );
    }

    public static <T> MutableComponent stylePropertyUpdated(String propertyName, T value, String valueUnit) {
        return styleTitle(propertyName + " Updated\n")
                    .append(Component.literal("New value: ")
                    .withStyle(style -> style.withBold(false).withColor(TEXT_COLOR)))
                    .append(Component.literal(value + valueUnit)
                    .withStyle(style -> style.withColor(VALUE_COLOR))
        );
    }

    public static MutableComponent styleBooleanValue(boolean value) {
        return Component.translatable("terrastorage.option." + (value ? "enabled" : "disabled"))
                .withStyle(style -> style.withColor(value ? ENABLED_COLOR : DISABLED_COLOR));
    }

    public static <T extends Enum<T>> MutableComponent styleEnumValue(String propertyKey, T value) {
        return Component.translatable("terrastorage.option." + propertyKey + "." + value.name().toLowerCase(Locale.ENGLISH))
                .withStyle(style -> style.withColor(ENUM_COLORS[value.ordinal() % ENUM_COLORS.length]));
    }

    public static MutableComponent error(String messageKey) {
        return Component.translatable(messageKey).withStyle(ERROR_COLOR);
    }

    public static MutableComponent warning(String messageKey) {
        return Component.translatable(messageKey).withStyle(WARNING_COLOR);
    }
}
