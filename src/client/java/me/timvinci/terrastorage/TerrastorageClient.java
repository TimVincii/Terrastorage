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
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint class for the client side.
 */
public class TerrastorageClient implements ClientModInitializer {
	public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(Reference.MOD_ID + "_client");
	private final ResourceLocation RELOAD_LISTENER_ID = new ResourceLocation(Reference.MOD_ID, "text_cache_reload");

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
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return RELOAD_LISTENER_ID;
			}

			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				LocalizedTextProvider.initializeButtonCaches();
			}
		});
	}
}