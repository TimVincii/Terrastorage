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

---

![Showcase Quick Stack And Restock](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_quick_stack_and_restock.gif)
**Quick Stack and Restock**:
* **Quick Stack** - Transfers items from the player's inventory into existing stacks in the storage.
* **Restock** - Transfers items from the storage into existing stacks in the player's inventory.

⏺️ Items are combined up to their max stack size, so empty slots in the receiving inventory remain unaffected.

---

![Showcase Sort Items And Rename](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_sort_items_and_rename.gif)
**Sort Items and Rename**:
* **Sort Items** - Sorts the items in the storage.
* **Rename** - Renames the storage block.

✳️ The sorting method for both Sort Items and Sort Inventory is controlled by a single configurable setting.

⏺️ Renamed storage blocks display nametags either above them or on the side facing the player if there’s a block above.

---

![Showcase Sort Inventory And Qstns](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/showcase_sort_inventory_and_qstns.gif)
**Sort Inventory and Quick Stack To Nearby Chests**:
* **Sort Inventory** - Sorts the items in the player's inventory.
* **Quick Stack To Nearby Storages** - Finds all nearby storages and performs the Quick Stack operation on each.

✳️ The sorting method for both Sort Items and Sort Inventory is controlled by a single configurable setting.

✳️ By default, the Quick Stack To Nearby Storages feature only considers storages within the player's line of sight, this is configurable.

✳️ The Quick Stack To Nearby Storages feature includes an animation where items fly from the player to the storage. Both the animation length and the time between flying items are configurable.

# Customization
![Options Screen](https://github.com/TimVincii/Terrastorage/raw/HEAD/.assets/options_screen.png)

### Client side:
The client side features an options screen that can be accessed with [mod menu](https://github.com/TerraformersMC/ModMenu) or with the `/tsclient options` command, it allows for the configuration of the following settings:
* **Options Button** - Controls whether an "Options" button, which takes the player to the options screen, is added to the storage screens.
* **Hotbar Protection** - Determines whether the hotbar is excluded from storage actions.
* **Buttons Style** - Controls the style of the buttons added to storage inventory screens. It can be set to:

  * Default - The standard Minecraft button style.
  * Text Only - Displays only the button text without a background, similar to Terraria’s storage buttons.
* **Buttons Placement** - Controls which side of the screen the storage option buttons are added to, and can be set to:

  * Right - Buttons are placed on the right side of storage screens.
  * Left - Buttons are placed on the left side of storage screens.
* **Sort By** - Determines the property by which items are sorted when using "Sort Items" or "Sort Inventory". It can be set to:

  * Item Group - Items are sorted by their group.
  * Item Count - Items are sorted by quantity (from highest to lowest).
  * Item Rarity - Items are sorted by their rarity (from higher to lower).
  * Item Name - Items are sorted alphabetically by name.
  * Item Id - Items are sorted by their ID (from lower to higher).

In addition to the options screen, these settings can be directly modified through the client configuration file. This file, designed for easy manual editing, is located at `.\config\terrastorage_client.toml`. Below are its default contents:
```toml
#Whether to display the options button in the storage screens
#Default: true
display_options_button = true
#==========
#Whether to protect hotbar items from the storage options
#Default: true
hotbar_protection = true
#==========
#The style of the storage option buttons
#Default: DEFAULT
buttons_style = "DEFAULT"
#==========
#The placement of the storage option buttons
#Default: RIGHT
buttons_placement = "RIGHT"
#==========
#The property by which items will be sorted
#Default: ITEM_GROUP
sort_type = "ITEM_GROUP"
```

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

These settings can be directly modified through the configuration file, located at `.\config\terrastorage.toml`. Below are its default contents:
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
```

# Compatibility
### Supported Storages
Terrastorage works smoothly with all the vanilla storage blocks like chests, barrels, shulker boxes, and even things like chest minecarts and chest boats.

Compatibility with modded storage blocks varies. Some will work without any issues, while others might not be compatible right away. If you come across a storage block from another mod that doesn’t seem to work with Terrastorage, feel free to open an issue on GitHub, and I’ll see what I can do!

### Known Issues
**I'm actively working on addressing these issues and aim to implement fixes in the near future. Thank you for your patience and understanding!**
* Nametags of renamed block entities that lack a custom BlockEntityRenderer are not rendered correctly. This issue is noticeable with barrels from the [Reinforced Barrels mod](https://github.com/Aton-Kish/reinforced-barrels).
* By default, when using the Quick Stack to Nearby Storages feature, the entire inventory of a double chest is considered, even if only one half is in the player’s line of sight. However, for double chests whose block entities do not extend LidOpenable, the feature only recognizes the individual inventory of the visible chest block entity. This results in items only being stacked into the visible half of the double chest, instead of the entire inventory.

  This issue is particularly noticeable with chests from the [Expanded Storage mod](https://github.com/quinn-semele/expanded-storage), consider disabling the line of sight check to mitigate this issue by using `/terrastorage line-of-sight-check false`.
* Renaming of block entities that don't extend LockableContainerBlockEntity is currently not supported.

# Feedback & Support
If you've got a question, a suggestion, or run into any issues, don't hesitate to [submit an issue](https://github.com/TimVincii/Terrastorage/issues)!

I’ll do my best to get back to you as quickly as possible!
