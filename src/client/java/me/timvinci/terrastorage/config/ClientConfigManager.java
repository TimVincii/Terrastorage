package me.timvinci.terrastorage.config;

import com.mojang.serialization.Codec;
import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.Reference;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Manages the client config.
 */
public class ClientConfigManager extends BaseConfigManager<TerrastorageClientConfig> {
    private static ClientConfigManager instance;

    public ClientConfigManager() {
        super(Reference.MOD_ID + "_client.toml", TerrastorageClient.CLIENT_LOGGER);
    }

    public static void init() {
        instance = new ClientConfigManager();
        instance.loadConfig();
    }

    public static ClientConfigManager getInstance() {
        return instance;
    }

    @Override
    protected TerrastorageClientConfig getDefaultConfig() {
        return new TerrastorageClientConfig();
    }

    /**
     * Iterates over the client config properties, and creates button widgets for modifying those properties.
     * @return A list of ClickableWidgets, containing the buttons.
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> SimpleOption<?>[] asOption() {
        List<SimpleOption<?>> options = new ArrayList<>();
        Text[] optionButtonTooltipText = LocalizedTextProvider.getOptionButtonsTooltipText();
        int i = 0;

        for (Field field : config.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigProperty.class)) {
                continue;
            }

            if (field.trySetAccessible()) {
                ConfigProperty property = field.getAnnotation(ConfigProperty.class);
                String propertyKey = property.key();
                String translationKey = "terrastorage.option." + propertyKey;

                Object fieldValue = getFieldValue(field, config, propertyKey);
                if (fieldValue != null) {
                    SimpleOption<?> option = null;
                    if (fieldValue.getClass().equals(Boolean.class)) {
                        option = SimpleOption.ofBoolean(
                                translationKey,
                                SimpleOption.constantTooltip(optionButtonTooltipText[i]),
                                (text, value) -> Text.translatable(translationKey + (value ? ".true" : ".false")),
                                (Boolean) fieldValue,
                                newValue -> setFieldValue(field, newValue,propertyKey)
                        );
                    }
                    else if (fieldValue.getClass().isEnum()) {
                        E currentValue = (E)fieldValue;
                        Class<E> enumClass = currentValue.getDeclaringClass();
                        option = new SimpleOption<>(
                                translationKey,
                                SimpleOption.constantTooltip(optionButtonTooltipText[i]),
                                (text, value) -> Text.translatable(translationKey + "." + value.name().toLowerCase(Locale.ENGLISH)),
                                new SimpleOption.PotentialValuesBasedCallbacks<>(
                                        Arrays.asList(enumClass.getEnumConstants()),
                                        Codec.STRING.xmap(
                                                string -> Arrays.stream(enumClass.getEnumConstants())
                                                        .filter(e -> e.name().equalsIgnoreCase(string))
                                                        .findFirst()
                                                        .orElse(null),
                                                newValue -> newValue.name().toLowerCase(Locale.ENGLISH)
                                        )
                                ),
                                currentValue,
                                newValue -> setFieldValue(field, newValue, propertyKey)
                        );
                    }

                    if (option != null) {
                        options.add(i++, option);
                    }
                }
            }
            else {
                logger.error("Failed to set field '{}' as accessible, please submit a github bug report about this.", field.getName());
            }
        }

        return options.toArray(SimpleOption[]::new);
    }
}
