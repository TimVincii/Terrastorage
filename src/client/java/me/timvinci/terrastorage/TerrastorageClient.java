package me.timvinci.terrastorage;

import me.timvinci.terrastorage.command.TerrastorageClientCommands;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientPacketRegistry;
import me.timvinci.terrastorage.render.BlockEntityRendererManager;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.Reference;

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
	 * Executes various tasks while Terrastorage is initializing on the client side.
	 */
	@Override
	public void onInitializeClient() {
		ClientConfigManager.init();
		TerrastorageClientCommands.registerCommands();
		ClientPacketRegistry.registerReceivers();
		LocalizedTextProvider.initializeButtonCaches();
		TerrastorageKeybindings.registerKeybindings();

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> BlockEntityRendererManager.registerLootableRenderers());
	}
}