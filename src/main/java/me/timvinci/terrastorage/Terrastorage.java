package me.timvinci.terrastorage;

import me.timvinci.terrastorage.command.TerrastorageCommands;
import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.network.NetworkHandler;
import me.timvinci.terrastorage.network.PayloadRegistry;
import me.timvinci.terrastorage.item.ItemGroupCache;
import me.timvinci.terrastorage.api.ItemFavoritingUtils;
import me.timvinci.terrastorage.util.Reference;
import net.fabricmc.api.EnvType;
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
	public static boolean environmentIsServer;
	public static boolean itemFavoritingEnabled = false;

	/**
	 * Executes various tasks while Terrastorage is initializing.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + Reference.MOD_NAME + " [" + Reference.MOD_VERSION + "].");
		environmentIsServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;


		ConfigManager.init();
		TerrastorageCommands.registerCommands();
		PayloadRegistry.registerPayloads();

		if (!environmentIsServer || ConfigManager.getInstance().getConfig().getEnableItemFavoriting()) {
			ItemFavoritingUtils.initializeComponentType();
			itemFavoritingEnabled = true;
		}

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
			NetworkHandler.sendServerConfigPayload(handler.player);
		});
	}
}