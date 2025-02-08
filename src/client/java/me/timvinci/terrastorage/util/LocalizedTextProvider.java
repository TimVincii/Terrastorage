package me.timvinci.terrastorage.util;

import me.timvinci.terrastorage.config.ClientConfigManager;
import net.minecraft.client.MinecraftClient;
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

            if (action == StorageAction.QUICK_STACK) {
                updateQuickStackTooltip(ClientConfigManager.getInstance().getConfig().getStorageQuickStackMode());
            }
            else {
                buttonTooltipCache.put(action, Tooltip.of(Text.translatable("terrastorage.button.tooltip." + action.name().toLowerCase(Locale.ENGLISH))));
            }
        }
    }

    public static void updateQuickStackTooltip(QuickStackMode quickStackMode) {
        String translationKey = "terrastorage.button.tooltip.quick_stack." + quickStackMode.name().toLowerCase(Locale.ENGLISH);
        buttonTooltipCache.put(StorageAction.QUICK_STACK, Tooltip.of(Text.translatable(translationKey)));
    }

    /**
     * Retrieves the text that is displayed on boolean options.
     * @param propertyKey The key of the option.
     * @param currentValue The current value of the option.
     * @return The text to be displayed on the button.
     */
    public static Text getBooleanOptionText(String propertyKey, boolean currentValue) {
        return Text.translatable("terrastorage.option." + propertyKey)
                .append(": ")
                .append(Text.translatable("terrastorage.option." + (currentValue ? "enabled" : "disabled")));
    }

    /**
     * Retrieves the text that is displayed on enum options.
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

    public static void sendUnsupportedMessage() {
        MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.unsupported_payload"));
    }

    public static void sendCooldownMessage() {
        MinecraftClient.getInstance().player.sendMessage(Text.translatable("terrastorage.message.payload_cooldown"));
    }
}
