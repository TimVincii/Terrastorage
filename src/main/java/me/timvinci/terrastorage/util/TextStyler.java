package me.timvinci.terrastorage.util;

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
    private static final Formatting WARNING_COLOR = Formatting.GOLD;

    public static MutableText styleTitle(String title) {
        return Text.literal(title).styled(style -> style.withBold(true).withColor(TITLE_COLOR));
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

    public static MutableText error(String messageKey) {
        return Text.translatable(messageKey).formatted(ERROR_COLOR);
    }

    public static MutableText warning(String messageKey) {
        return Text.translatable(messageKey).formatted(WARNING_COLOR);
    }
}
