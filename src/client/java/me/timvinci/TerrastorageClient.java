package me.timvinci;

import me.timvinci.command.TerrastorageClientCommands;
import me.timvinci.config.ClientConfigManager;
import me.timvinci.network.ClientReceiverRegistry;
import me.timvinci.util.BlockEntityRendererManager;
import me.timvinci.util.LocalizedTextProvider;
import me.timvinci.util.Reference;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint class for the client side.
 */
public class TerrastorageClient implements ClientModInitializer {
	public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(Reference.MOD_ID + "_client");

	/**
	 * Initializes the client config manager, client commands, client global receivers, button cache, and registers
	 * lootable renderers once the client starts.
	 */
	@Override
	public void onInitializeClient() {
		ClientConfigManager.init();
		TerrastorageClientCommands.registerCommands();
		ClientReceiverRegistry.registerReceivers();
		LocalizedTextProvider.initializeButtonCaches();

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> BlockEntityRendererManager.registerLootableRenderers());
	}
}