package me.timvinci;

import me.timvinci.command.TerrastorageCommands;
import me.timvinci.config.ConfigManager;
import me.timvinci.inventory.InventoryUtils;
import me.timvinci.network.NetworkHandler;
import me.timvinci.network.PacketRegistry;
import me.timvinci.item.ItemGroupCache;
import me.timvinci.util.Reference;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroups;
import net.minecraft.resource.featuretoggle.FeatureFlags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint class.
 */
public class Terrastorage implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);
	private boolean populatedItemGroups = false;

	/**
	 * Initializes the config manager, registers the commands, registers the payloads, registers an event listener
	 * for when the server starts and populates the item groups once it does, and registers an event listener for when
	 * a player joins the server to send them the server config.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + Reference.MOD_NAME + " [" + Reference.MOD_VERSION + "].");

		if (FabricLoader.getInstance().isModLoaded("expandedstorage")) {
			InventoryUtils.expandedStorageLoaded = true;
		}

		ConfigManager.init();
		TerrastorageCommands.registerCommands();
		PacketRegistry.registerPacketReceivers();

		ServerLifecycleEvents.SERVER_STARTING.register((listener) -> {
			if (populatedItemGroups) {
				return;
			}

			// Forcing the population of item groups.
			ItemGroups.updateDisplayContext(FeatureFlags.DEFAULT_ENABLED_FEATURES, false, listener.getRegistryManager());
			ItemGroupCache.init();

			populatedItemGroups = true;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			NetworkHandler.sendServerConfigPacket(handler.player);
		});
	}
}