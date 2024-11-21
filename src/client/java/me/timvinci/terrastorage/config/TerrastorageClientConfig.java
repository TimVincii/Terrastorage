package me.timvinci.terrastorage.config;

import me.timvinci.terrastorage.util.ButtonsStyle;
import me.timvinci.terrastorage.util.ButtonsPlacement;
import me.timvinci.terrastorage.util.QuickStackMode;
import me.timvinci.terrastorage.util.SortType;

/**
 * Defines and holds the client config properties.
 */
public class TerrastorageClientConfig {
    @ConfigProperty(key = "display_options_button", comment = "Whether to display the options button in the storage screens")
    private boolean displayOptionsButton = true;

    @ConfigProperty(key = "hotbar_protection", comment = "Whether to protect hotbar items from the storage options")
    private boolean hotbarProtection = true;

    @ConfigProperty(key = "buttons_tooltip", comment = "Whether the storage option buttons have description tooltips.")
    @SubProperty
    private boolean buttonsTooltip = true;

    @ConfigProperty(key = "buttons_style", comment = "The style of the storage option buttons")
    @SubProperty
    private ButtonsStyle buttonsStyle = ButtonsStyle.DEFAULT;

    @ConfigProperty(key = "buttons_placement", comment = "The placement of the storage option buttons")
    @SubProperty
    private ButtonsPlacement buttonsPlacement = ButtonsPlacement.RIGHT;

    @ConfigProperty(key = "buttons_x_offset", comment = "The horizontal offset of the storage option buttons")
    @PropertyRange(min = -100, max = 100)
    @SubProperty
    private int buttonsXOffset = 0;

    @ConfigProperty(key = "buttons_y_offset", comment = "The vertical offset aof the storage option buttons")
    @PropertyRange(min = -100, max = 100)
    @SubProperty
    private int buttonsYOffset = 0;

    @ConfigProperty(key = "buttons_width", comment = "The width of the storage option buttons")
    @PropertyRange(min = 20, max = 150)
    @SubProperty
    private int buttonsWidth = 70;

    @ConfigProperty(key = "buttons_height", comment = "The height of the storage option buttons")
    @PropertyRange(min = 5, max = 50)
    private int buttonsHeight = 15;

    @ConfigProperty(key = "buttons_spacing", comment = "The vertical spacing between the storage option buttons")
    @PropertyRange(min = 0, max = 20)
    private int buttonsSpacing = 2;

    @ConfigProperty(key = "sort_type", comment = "The property by which items will be sorted")
    private SortType sortType = SortType.ITEM_GROUP;

    @ConfigProperty(key = "storage_quick_stack_mode", comment = "The quick stacking mode used when quick stacking into a single storage")
    @SingleOption
    private QuickStackMode storageQuickStackMode = QuickStackMode.SMART_DEPOSIT;

    @ConfigProperty(key = "nearby_quick_stack_mode", comment = "The quick stacking mode used when quick stacking to nearby storages")
    @SingleOption
    private QuickStackMode nearbyQuickStackMode = QuickStackMode.SMART_DEPOSIT;


    public boolean getDisplayOptionsButton() { return displayOptionsButton; }

    public boolean getHotbarProtection() { return hotbarProtection; }

    public boolean getButtonsTooltip() { return buttonsTooltip; }

    public void setButtonsTooltip(boolean buttonsTooltip) { this.buttonsTooltip = buttonsTooltip; }

    public ButtonsStyle getButtonsStyle() { return buttonsStyle; }

    public void setButtonsStyle(ButtonsStyle buttonsStyle) { this.buttonsStyle = buttonsStyle; }

    public ButtonsPlacement getButtonsPlacement() { return buttonsPlacement; }

    public void setButtonsPlacement(ButtonsPlacement buttonsPlacement) { this.buttonsPlacement = buttonsPlacement; }

    public int getButtonsXOffset() { return buttonsXOffset; }

    public void setButtonsXOffset(int buttonsXOffset) { this.buttonsXOffset = buttonsXOffset; }

    public int getButtonsYOffset() {return buttonsYOffset; }

    public void setButtonsYOffset(int buttonsYOffset) { this.buttonsYOffset = buttonsYOffset; }

    public int getButtonsWidth() { return buttonsWidth; }

    public void setButtonsWidth(int buttonsWidth) { this.buttonsWidth = buttonsWidth; }

    public int getButtonsHeight() { return buttonsHeight; }

    public void setButtonsHeight(int buttonsHeight) { this.buttonsHeight = buttonsHeight; }

    public int getButtonsSpacing() { return this.buttonsSpacing; }

    public void setButtonsSpacing(int buttonsSpacing) { this.buttonsSpacing = buttonsSpacing; }

    public SortType getSortType() { return sortType; }

    public QuickStackMode getStorageQuickStackMode() { return this.storageQuickStackMode; }

    public QuickStackMode getNearbyQuickStackMode() { return this.nearbyQuickStackMode; }
}
