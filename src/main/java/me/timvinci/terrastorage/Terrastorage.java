package me.timvinci.terrastorage;

import me.timvinci.terrastorage.command.TerrastorageCommands;
import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.inventory.InventoryUtils;
import me.timvinci.terrastorage.network.NetworkHandler;
import me.timvinci.terrastorage.network.PacketRegistry;
import me.timvinci.terrastorage.item.ItemGroupCache;
import me.timvinci.terrastorage.util.Reference;
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
	 * Executes various tasks while Terrastorage is initializing.
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

		ServerLifecycleEvents.SERVER_STARTED.register((listener) -> {
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