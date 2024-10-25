package me.timvinci.terrastorage.config;

import me.timvinci.terrastorage.util.ButtonsStyle;
import me.timvinci.terrastorage.util.ButtonsPlacement;
import me.timvinci.terrastorage.util.SortType;

/**
 * Defines and holds the client config properties.
 */
public class TerrastorageClientConfig {
    @ConfigProperty(key = "display_options_button", comment = "Whether to display the options button in the storage screens")
    private boolean displayOptionsButton = true;
    @ConfigProperty(key = "hotbar_protection", comment = "Whether to protect hotbar items from the storage options")
    private boolean hotbarProtection = true;
    @ConfigProperty(key = "buttons_style", comment = "The style of the storage option buttons")
    private ButtonsStyle buttonsStyle = ButtonsStyle.DEFAULT;
    @ConfigProperty(key = "buttons_placement", comment = "The placement of the storage option buttons")
    private ButtonsPlacement buttonsPlacement = ButtonsPlacement.RIGHT;
    @ConfigProperty(key = "sort_type", comment = "The property by which items will be sorted")
    private SortType sortType = SortType.ITEM_GROUP;

    public boolean getDisplayOptionsButton() { return displayOptionsButton; }

    public boolean getHotbarProtection() {
        return hotbarProtection;
    }

    public ButtonsStyle getButtonsStyle() {
        return buttonsStyle;
    }

    public ButtonsPlacement getButtonsPlacement() {
        return buttonsPlacement;
    }

    public SortType getSortType() { return sortType; }
}
