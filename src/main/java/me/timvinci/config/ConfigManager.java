package me.timvinci.config;

import me.timvinci.Terrastorage;
import me.timvinci.util.Reference;

/**
 * Manages the server config.
 */
public class ConfigManager extends BaseConfigManager<TerrastorageConfig> {
    private static ConfigManager instance;

    public ConfigManager() {
        super(Reference.MOD_ID + ".toml", Terrastorage.LOGGER);
    }

    public static void init() {
        instance = new ConfigManager();
        instance.loadConfig();
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    @Override
    protected TerrastorageConfig getDefaultConfig() {
        return new TerrastorageConfig();
    }
}