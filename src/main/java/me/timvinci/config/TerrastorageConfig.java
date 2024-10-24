package me.timvinci.config;

/**
 * Defines and holds the server config properties.
 */
public class TerrastorageConfig {
    @ConfigProperty(key = "action_cooldown", comment = "The cooldown of all storage actions, in game ticks")
    @PropertyRange(min = 2, max = 100)
    private int actionCooldown = 10;
    @ConfigProperty(key = "line_of_sight_check", comment = "Whether the Quick Stack To Nearby Storages feature only considers storages within the player's line of sight.")
    private boolean lineOfSightCheck = true;
    @ConfigProperty(key = "quick_stack_range", comment = "The range of the Quick Stack to Nearby Storages feature, in blocks")
    @PropertyRange(min = 3, max = 16)
    private int quickStackRange = 8;
    @ConfigProperty(key = "item_animation_length", comment = "The length of the flying item animation that occurs when Quick Stack To Nearby Storages is used, in game ticks")
    @PropertyRange(min = 10, max = 200)
    private int itemAnimationLength = 20;
    @ConfigProperty(key = "item_animation_interval", comment = "The interval between animated flying items, in game ticks")
    @PropertyRange(min = 0, max = 20)
    private int itemAnimationInterval = 5;
    @ConfigProperty(key = "keep_favorites_on_drop", comment = "Whether items will keep their favorite status once they are dropped as an item entity.")
    private boolean keepFavoritesOnDrop = true;

    public int getActionCooldown() { return actionCooldown; }

    public void setActionCooldown(int actionCooldown) { this.actionCooldown = actionCooldown; }

    public boolean getLineOfSightCheck() { return lineOfSightCheck; }

    public void setLineOfSightCheck(boolean lineOfSightCheck) { this.lineOfSightCheck = lineOfSightCheck; }

    public int getQuickStackRange() { return quickStackRange; }

    public void setQuickStackRange(int quickStackRange) { this.quickStackRange = quickStackRange; }

    public int getItemAnimationLength() { return itemAnimationLength; }

    public void setItemAnimationLength(int itemAnimationLength) { this.itemAnimationLength = itemAnimationLength; }

    public int getItemAnimationInterval() { return itemAnimationInterval; }

    public void setItemAnimationInterval(int itemAnimationInterval) { this.itemAnimationInterval = itemAnimationInterval; }

    public boolean getKeepFavoritesOnDrop() { return keepFavoritesOnDrop; }

    public void setKeepFavoritesOnDrop(boolean keepFavoritesOnDrop) { this.keepFavoritesOnDrop = keepFavoritesOnDrop; }
}