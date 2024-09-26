package me.timvinci.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * A base config manager class, used by both the client and server config managers.
 * NightConfig is used to create, access, and modify the toml configuration files created by Terrastorage.
 * @param <T> The Terrastorage config type.
 */
public abstract class BaseConfigManager<T> {
    protected final Path configFilePath;
    // Define an instance of the config.
    protected T config;
    protected final Logger logger;

    protected BaseConfigManager(String configFileName, Logger logger) {
        this.configFilePath = FabricLoader.getInstance().getConfigDir().resolve(configFileName);
        this.config = getDefaultConfig();
        this.logger = logger;
    }

    protected abstract T getDefaultConfig();

    /**
     * Loads the config properties from the config file to the config instance.
     */
    public void loadConfig() {
        CommentedFileConfig fileConfig = buildFileConfig(configFilePath);
        if (loadFileConfigFailed(fileConfig, "using default values for all properties")) {
            return;
        }

        if (!fileConfig.getFile().exists() || fileConfig.isEmpty()) {
            fileConfig.close();
            saveConfig();
        } else {
            loadAnnotatedProperties(fileConfig);
            fileConfig.close();
        }
    }

    /**
     * Saves the config properties of the config instance to the config file.
     * @return True if the save was carried out without any errors, false otherwise.
     */
    public boolean saveConfig() {
        CommentedFileConfig fileConfig = buildFileConfig(configFilePath);
        if (loadFileConfigFailed(fileConfig, "couldn't save values to config file")) {
            return false;
        }

        boolean hasNoErrors = writeAnnotatedProperties(fileConfig);
        fileConfig.save();
        fileConfig.close();
        return hasNoErrors;
    }

    /**
     * Loads the value of each property in the config file, to its annotated field in the config instance.
     * @param fileConfig The CommentedFileConfig created from the config file.
     */
    @SuppressWarnings("unchecked")
    protected void loadAnnotatedProperties(CommentedFileConfig fileConfig) {
        for (Field field : config.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigProperty.class)) {
                continue;
            }

            if (field.trySetAccessible()) {
                ConfigProperty property = field.getAnnotation(ConfigProperty.class);
                String propertyKey = property.key();

                if (field.isAnnotationPresent(PropertyRange.class)) {
                    PropertyRange range = field.getAnnotation(PropertyRange.class);
                    loadIntegerProperty(fileConfig, propertyKey, value -> setFieldValue(field, value, propertyKey), range.min(), range.max());
                }
                else if (field.getType().isEnum()) {
                    loadEnumProperty(fileConfig, propertyKey, value -> setFieldValue(field, value, propertyKey), (Class<Enum>) field.getType());
                }
                else {
                    loadProperty(fileConfig, propertyKey, value -> setFieldValue(field, value, propertyKey));
                }
            }
            else {
                logger.error("Failed to set field '{}' as accessible, please submit a github bug report about this.", field.getName());
            }
        }
    }

    /**
     * Sets the value of a field in the config instance.
     * @param field The setter method of the field.
     * @param value The new value.
     * @param propertyKey The key of the field.
     */
    protected void setFieldValue(Field field, Object value, String propertyKey) {
        try {
            field.set(config, value);
        } catch (IllegalAccessException e) {
            logger.error("Failed to set field value of configuration property '{}'.", propertyKey, e);
        }
    }

    /**
     * Writes the key, comment, and value of each annotated field in the config instance, to the config file.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @return True if there were no errors, false otherwise.
     */
    protected boolean writeAnnotatedProperties(CommentedFileConfig fileConfig) {
        T newConfigInstance = getDefaultConfig();
        boolean hasNoErrors = true;
        boolean firstField = true;
        for (Field field : config.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigProperty.class)) {
                continue;
            }

            if (field.trySetAccessible()) {
                ConfigProperty property = field.getAnnotation(ConfigProperty.class);
                String propertyKey = property.key();
                Object fieldValue = getFieldValue(field, config, propertyKey);
                if (fieldValue != null) {
                    fileConfig.set(propertyKey, fieldValue);
                    String propertyComment = property.comment();
                    if (!firstField) {
                        propertyComment = "==========\n" + propertyComment;
                    }
                    if (field.isAnnotationPresent(PropertyRange.class)) {
                        PropertyRange range = field.getAnnotation(PropertyRange.class);
                        propertyComment += "\nRange: " + range.min() + " to " + range.max() + ", inclusive";
                    }
                    propertyComment += "\nDefault: " + getFieldValue(field, newConfigInstance, propertyKey).toString();
                    fileConfig.setComment(propertyKey, propertyComment);
                }
                else if (hasNoErrors) {
                    hasNoErrors = false;
                }
            }
            else {
                logger.error("Failed to set field '{}' as accessible, please submit a github bug report about this.", field.getName());
                if (hasNoErrors) {
                    hasNoErrors = false;
                }
            }

            if (firstField) {
                firstField = false;
            }
        }

        return hasNoErrors;
    }

    /**
     * Gets the value of a field in a config instance.
     * @param field The field.
     * @param instance An instance of the config class containing the field.
     * @param propertyKey The key of the field.
     * @return The value of the field.
     */
    protected Object getFieldValue(Field field, T instance, String propertyKey) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            logger.error("Failed to get field value of configuration property '{}'.", propertyKey, e);
            return null;
        }
    }

    /**
     * Loads a property from the config file to its field in the config instance.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @param propertyKey The key of the property.
     * @param propertySetter The setter method of the field.
     * @param <P> The property type.
     */
    protected <P> void loadProperty(CommentedFileConfig fileConfig, String propertyKey, Consumer<P> propertySetter) {
        if (missingProperty(fileConfig, propertyKey)) {
            return;
        }

        try {
            propertySetter.accept(fileConfig.get(propertyKey));
        } catch (ClassCastException e) {
            logger.error("Configuration property '{}' is of the wrong type, using default value instead.", propertyKey);
        }
    }

    /**
     * Loads an enum property from the config file to its field in the config instance.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @param propertyKey The key of the property.
     * @param propertySetter The setter method of the field.
     * @param enumClass The enum class of the property.
     * @param <P> The property type.
     */
    protected <P extends Enum<P>> void loadEnumProperty(CommentedFileConfig fileConfig, String propertyKey, Consumer<P> propertySetter, Class<P> enumClass) {
        if (missingProperty(fileConfig, propertyKey)) {
            return;
        }

        String enumAsString = fileConfig.get(propertyKey);
        try {
            P enumValue = P.valueOf(enumClass, enumAsString);
            propertySetter.accept(enumValue);
        }
        catch (IllegalArgumentException e) {
            logger.error("Configuration property '{}' isn't properly set in the config file, using default value instead.", propertyKey);
        }
    }

    /**
     * Loads an integer property from the config file to its field in the config instance.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @param propertyKey The key of the property.
     * @param propertySetter The setter method of the field.
     * @param min The minimum value allowed for the property.
     * @param max The maximum value allowed for the property.
     */
    protected void loadIntegerProperty(CommentedFileConfig fileConfig, String propertyKey, Consumer<Integer> propertySetter, int min, int max) {
        if (missingProperty(fileConfig, propertyKey)) {
            return;
        }

        int propertyValue = fileConfig.getInt(propertyKey);
        if (propertyValue >= min && propertyValue <= max) {
            propertySetter.accept(propertyValue);
        } else {
            logger.error("Configuration property '{}' isn't in its bound range, using default value instead.", propertyKey);
        }
    }

    /**
     * Checks if a property is missing from the config file, and logs it as missing if it is.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @param propertyKey The key of the property.
     * @return True if the property is missing, false otherwise.
     */
    private boolean missingProperty(CommentedFileConfig fileConfig, String propertyKey) {
        if (!fileConfig.contains(propertyKey)) {
            logger.error("Configuration property '{}' is missing from the config file, using default value instead.", propertyKey);
            return true;
        }
        return false;
    }

    /**
     * Checks if the loading of the config file failed.
     * @param fileConfig The CommentedFileConfig created from the config file.
     * @param additionalInfo Additional info to add the error message.
     * @return True if the loading failed, false otherwise.
     */
    private boolean loadFileConfigFailed(CommentedFileConfig fileConfig, String additionalInfo) {
        try {
            fileConfig.load();
            return false;
        } catch (ParsingException e) {
            logger.error("Configuration parsing failed, {}. Exception message: {}", additionalInfo, e.getMessage(), e);
            return true;
        }
    }

    /**
     * Builds a CommentedFileConfig from the config file.
     * @param configFilePath The path to the config file.
     * @return The built CommentedFileConfig.
     */
    private CommentedFileConfig buildFileConfig(Path configFilePath) {
        return CommentedFileConfig.builder(configFilePath)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
    }

    /**
     * Gets the config instance held by this class.
     * @return The config instance.
     */
    public T getConfig() {
        return config;
    }
}
