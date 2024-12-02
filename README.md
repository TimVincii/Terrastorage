<div align="center">

![Terrastorage Banner](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/banner.png)
[![Available On Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/available/modrinth_46h.png)](https://modrinth.com/mod/terrastorage)
[![Available On Curseforge](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/available/curseforge_46h.png)](https://www.curseforge.com/minecraft/mc-mods/terrastorage)
[![Support Me On Ko-Fi](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/donate/kofi-singular_46h.png)](https://ko-fi.com/timvinci)
<br>
[![Available For Fabric](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/supported/fabric_46h.png)](https://fabricmc.net)
[![Available For Quilt](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/supported/quilt_46h.png)](https://quiltmc.org/en)
[![Requires Fabric API](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/requires/fabric-api_46h.png)](https://github.com/FabricMC/fabric)
</div>

# Terrastorage

Terrastorage is a customizable server and client-side mod that brings the amazing storage options from Terraria to Minecraft, including the incredibly useful **Quick Stack To Nearby Chests** feature. It currently supports Fabric and Quilt.

Note that Terrastorage needs to be installed on **both** the client and server to work properly.
With that out of the way, let's dive into the feature showcase:

# Features
![Showcase Loot And Deposit](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_loot_and_deposit.gif)
**Loot and Deposit All**:
* **Loot All** - Transfers all items from the storage to the player's inventory.
* **Deposit All** - Transfers all items from the player's inventory to the storage.

⏺️ Items are first stacked with existing ones in the opposite inventory before being moved to an empty slot.

⏺️ Favorite items will not be affected by **Deposit All**.

---

![Showcase Quick Stack And Restock](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_quick_stack_and_restock.gif)
**Quick Stack and Restock**:
* **Quick Stack** - Transfers items from the player's inventory to the storage using one of two modes:

  * **Smart Deposit** - Similar to Fill Up, but also moves all matching items into empty slots, creating new stacks as needed.

  * **Fill Up** - Adds items to existing stacks until they reach their maximum capacity, keeping empty slots in the receiving inventory unaffected.

* **Restock** - Transfers items from the storage into existing stacks in the player's inventory.

⏺️ Favorite items will not be affected by **Quick Stack**.

✳️ The Quick Stack mode can be configured through the **Storage Quick Stack Mode** option in the **Options Screen**.

---

![Showcase Item Favoriting](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_item_favoriting.gif)
**Item Favoriting**:

* **Item Favoriting** - You can favorite items in your inventory by holding the modifier key for Item Favoriting (Left Alt by default) and left-clicking them.

✳️ The modifier key is customizable via the controls menu.

⏺️ Favorite items are protected from being:
* Thrown out of the inventory.
* Shift-pressed to a different inventory.
* Swapped from the hotbar to a different inventory during a hotbar keybind press.
* Deleted in the creative inventory screen.
* Moved by **Deposit All** or **Sort Inventory**.
* Modified by **Quick Stack** or **Quick Stack To Nearby Storages**.

---

![Showcase Sort Items And Rename](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_sort_items_and_rename.gif)
**Sort Items and Rename**:
* **Sort Items** - Sorts the items in the storage.
* **Rename** - Renames the storage block.

✳️ The sorting method for both **Sort Items** and **Sort Inventory** is controlled by a single configurable setting.

⏺️ **Sort Items** can also be activated by hovering over a slot and pressing the sort inventory keybind (R by default, customizable in the controls menu).

⏺️ Renamed storage blocks display nametags either above them or on the side facing the player if there’s a block above.

---

![Showcase Sort Inventory And Qstns](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_sort_inventory_and_qstns.gif)
**Sort Inventory and Quick Stack To Nearby Storages**:
* **Sort Inventory** - Sorts the items in the player's inventory.
* **Quick Stack To Nearby Storages** - Finds all nearby storages and performs the Quick Stack operation on each.

✳️ The sorting method for both **Sort Items** and **Sort Inventory** is controlled by a single configurable setting.

⏺️ **Sort Inventory** can also be activated by hovering over a slot and pressing the sort inventory keybind (R by default, customizable in the controls menu).

✳️ By default, the **Quick Stack To Nearby Storages** feature only considers storages within the player's line of sight, this is configurable.

✳️ The **Quick Stack To Nearby Storages** feature includes an animation where items fly from the player to the storage. Both the animation length and the time interval between flying items are configurable.

⏺️ Favorite items will not be moved by **Sort Inventory** or modified by **Quick Stack To Nearby Storages**.

# Customization
![Options Screen](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/options_screen.png)

### Client side:
The client side features an options screen that can be accessed with [mod menu](https://github.com/TerraformersMC/ModMenu) or with the `/tsclient options` command, it allows for the configuration of the following settings:
* **Options Button** - Controls whether an "Options" button, which takes the player to the options screen, is added to the storage screens.
* **Hotbar Protection** - Determines whether the hotbar is excluded from storage actions.
* **Sort By** - Determines the property by which items are sorted when using **Sort Items** or **Sort Inventory**. It can be set to:

  * Item Group - Items are sorted by their group.
  * Item Count - Items are sorted by quantity (from highest to lowest).
  * Item Rarity - Items are sorted by their rarity (from higher to lower).
  * Item Name - Items are sorted alphabetically by name.
  * Item Id - Items are sorted by their ID (from lower to higher).
* **Storage Quick Stack Mode** - Configures the quick stack mode used for single storage operations. (See Quick Stack and Restock for details.)
* **Nearby Storage Quick Stack Mode** - Configures the quick stack mode used for nearby storages. (See Quick Stack and Restock for details.)

![Buttons Customization Screen](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/buttons_customization_screen.png)

In addition to the **Options Screen**, a dedicated buttons customization screen allows for customizing the appearance of the storage option buttons displayed in storage inventory screens. It includes:
* **Buttons Tooltip** - Toggles the visibility of the tooltips for the buttons.
* **Buttons Style** - Sets the visual style of the buttons:

  * Default - The standard Minecraft button style.
  * Text Only - Displays only the button text without a background, similar to Terraria’s storage buttons.
* **Buttons Placement** - Determines the side of the screen where buttons are displayed:
  * Right - Buttons are placed on the right side of storage screens.
  * Left - Buttons are placed on the left side of storage screens.
* **X and Y Offset** - Adjusts the position of the buttons along the horizontal and vertical axes.
* **Width and Height** - Controls the size of the buttons.
* **Spacing** - Sets the vertical space between each button.

⏺️ All these options are customizable through the client configuration file, located at `.\config\terrastorage_client.toml`. This file is designed for easy manual editing and includes the default values of each property, as well as the range of integer properties.

---

### Server Side
The following server settings can be modified via in-game commands. To change a setting, use the command `/terrastorage [option] [new value]`. To view the current value, use `/terrastorage [option]`.

The available options are:
* **action-cooldown**

  *Sets the cooldown for all storage actions, in game ticks.*

  Default: 10
* **line-of-sight-check**

  *Determines whether the Quick Stack To Nearby Storages feature only considers storages within the player's line of sight.*

  Default: true
* **quick-stack-range**

  *Specifies the range of the Quick Stack to Nearby Storages feature, in blocks.*

  Default: 8
* **item-animation-length**

  *Sets the length of the flying item animation when Quick Stack to Nearby Storages is used, in game ticks.*

  Default: 20
* **item-animation-interval**

  *Specifies the time interval between animated flying items in ticks.*

  Default: 5

* **keep-favorites-on-drop**

  *Determines whether items will keep their favorite status once they are dropped as an item entity.*

  Default: true

⏺️ These settings can be directly modified through the configuration file, located at `.\config\terrastorage.toml`. Just like the client configuration file, it is designed for easy manual editing. Below are its default contents:
```toml
#The cooldown of all storage actions, in game ticks
#Range: 2 to 100, inclusive
#Default: 10
action_cooldown = 10
#==========
#Whether the Quick Stack To Nearby Storages feature only considers storages within the player's line of sight.
#Default: true
line_of_sight_check = true
#==========
#The range of the Quick Stack to Nearby Storages feature, in blocks
#Range: 3 to 16, inclusive
#Default: 8
quick_stack_range = 8
#==========
#The length of the flying item animation that occurs when Quick Stack To Nearby Storages is used, in game ticks
#Range: 10 to 200, inclusive
#Default: 20
item_animation_length = 20
#==========
#The interval between animated flying items, in game ticks
#Range: 0 to 20, inclusive
#Default: 5
item_animation_interval = 5
#==========
#Whether items will keep their favorite status once they are dropped as an item entity.
#Default: true
keep_favorites_on_drop = true
```

# Compatibility
### Supported Storages
Terrastorage works smoothly with all the vanilla storage blocks like chests, barrels, shulker boxes, and even things like chest minecarts and chest boats.

Compatibility with modded storage blocks varies. Some will work without any issues, while others might not be compatible right away. If you come across a storage block from another mod that doesn’t seem to work with Terrastorage, feel free to open an issue on GitHub, and I’ll see what I can do!

### Feature Limitations
Renaming modded storage blocks that do not extend `LockableContainerBlockEntity` or modded storage entities that do not extend `VehicleInventory` is currently not supported. This is a limitation that would be difficult to overcome and is likely to remain for the foreseeable future.

# Feedback & Support
If you've got a question, a suggestion, or run into any issues, don't hesitate to [submit an issue](https://github.com/TimVincii/Terrastorage/issues)!

Please note: As of **December 2nd, 2024,**, I’ve started my studies, which will keep me busy for the next few years. This means I will have significantly less time to actively work on the Terrastorage or respond to feedback and issues.

I’ll still do my best to address important issues and consider suggestions when possible, but response times may be slower.
