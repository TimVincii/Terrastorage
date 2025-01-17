package me.timvinci.terrastorage.config;

import me.timvinci.terrastorage.TerrastorageClient;
import me.timvinci.terrastorage.gui.ButtonsCustomizationScreen;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.Reference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    public TerrastorageClientConfig getDefaultConfig() {
        return new TerrastorageClientConfig();
    }

    /**
     * Iterates over the client config properties, and creates button widgets for modifying those properties.
     * @return A list of ClickableWidgets, containing the buttons.
     */
    @SuppressWarnings("unchecked")
    public List<Pair<ClickableWidget, Boolean>> asOptions() {
        List<ClickableWidget> options = new ArrayList<>();
        List<Boolean> singleOptionOrder = new ArrayList<>();
        Tooltip[] optionButtonsTooltip = LocalizedTextProvider.getOptionButtonsTooltip();
        int i = 0;

        for (Field field : config.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigProperty.class) || field.isAnnotationPresent(SubProperty.class)) {
                continue;
            }


            if (field.trySetAccessible()) {
                ConfigProperty property = field.getAnnotation(ConfigProperty.class);
                String propertyKey = property.key();
                boolean singleOption = field.isAnnotationPresent(SingleOption.class);

                Object fieldValue = getFieldValue(field, config, propertyKey);
                if (fieldValue != null) {
                    ButtonWidget optionButton = null;
                    int finalI = i;

                    if (fieldValue.getClass().equals(Boolean.class)) {
                        optionButton = ButtonWidget.builder(
                                LocalizedTextProvider.getBooleanOptionText(propertyKey, (Boolean) fieldValue),
                                onPress -> {
                                    boolean currentValue = (Boolean) getFieldValue(field, config, propertyKey);
                                    currentValue = !currentValue;
                                    setFieldValue(field, currentValue, propertyKey);
                                    options.get(finalI).setMessage(LocalizedTextProvider.getBooleanOptionText(propertyKey, currentValue));
                                }
                        ).build();
                    } else if (fieldValue.getClass().isEnum()) {
                        optionButton = ButtonWidget.builder(
                                LocalizedTextProvider.getEnumOptionText(propertyKey, (Enum) fieldValue),
                                onPress -> {
                                    Enum<?> currentValue = (Enum<?>) getFieldValue(field, config, propertyKey);
                                    try {
                                        // Get the next method of the enum class.
                                        Method nextMethod = currentValue.getClass().getMethod("next", currentValue.getClass());
                                        // Use the next method to iterate over the enum constants.
                                        currentValue = (Enum) nextMethod.invoke(currentValue.getClass(), currentValue);
                                        setFieldValue(field, currentValue, propertyKey);
                                        options.get(finalI).setMessage(LocalizedTextProvider.getEnumOptionText(propertyKey, (Enum) currentValue));
                                    } catch (NoSuchMethodException | InvocationTargetException |
                                             IllegalAccessException e) {
                                        logger.error("Failed to find or invoke the next method for the '{}' enum class.", currentValue.getClass().getName(), e);
                                    }
                                }
                        ).build();
                    }

                    if (optionButton != null) {
                        optionButton.setTooltip(optionButtonsTooltip[i]);
                        options.add(i, optionButton);
                        singleOptionOrder.add(i++, singleOption);
                    }
                }
            }
            else {
                logger.error("Failed to set field '{}' as accessible.", field.getName());
            }
        }

        List<Pair<ClickableWidget, Boolean>> result = new ArrayList<>();
        for (int j = 0; j < options.size(); j++) {
            result.add(new Pair<>(options.get(j), singleOptionOrder.get(j)));
        }

        return result;
    }
}
