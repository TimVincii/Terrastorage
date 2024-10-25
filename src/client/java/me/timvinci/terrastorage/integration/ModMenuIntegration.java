package me.timvinci.terrastorage.integration;

import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Integrates Mod Menu functionality.
 */
public class ModMenuIntegration implements ModMenuApi {

    /**
     * Register the options screen to be used by Mod Menu.
     * @return A new instance of the Terrastorage options screen.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TerrastorageOptionsScreen::new;
    }
}
