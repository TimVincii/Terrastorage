package me.timvinci.terrastorage;

import me.timvinci.terrastorage.command.TerrastorageClientCommands;
import me.timvinci.terrastorage.config.ClientConfigManager;
import me.timvinci.terrastorage.keybinding.TerrastorageKeybindings;
import me.timvinci.terrastorage.network.ClientReceiverRegistry;
import me.timvinci.terrastorage.render.BlockEntityRendererManager;
import me.timvinci.terrastorage.util.LocalizedTextProvider;
import me.timvinci.terrastorage.util.Reference;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Entrypoint class for the client side.
 */
public class TerrastorageClient implements ClientModInitializer {
	public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(Reference.MOD_ID + "_client");
	private final Identifier RELOAD_LISTENER_ID = Identifier.of(Reference.MOD_ID, "text_cache_reload");

	/**
	 * Executes various tasks while Terrastorage is initializing on the client side.
	 */
	@Override
	public void onInitializeClient() {
		ClientConfigManager.init();
		TerrastorageClientCommands.registerCommands();
		ClientReceiverRegistry.registerReceivers();
		TerrastorageKeybindings.registerKeybindings();
		
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> BlockEntityRendererManager.registerLootableRenderers());
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(
                Identifier.of(Reference.MOD_ID, "localized_text_provider"),
                (store, prepareExecutor, synchronizer, applyExecutor) -> {

                    // Prepare phase (nothing to prepare)
                    CompletableFuture<Void> prepare = CompletableFuture.completedFuture(null);

                    // Synchronize with reload pipeline
                    return synchronizer.whenPrepared(prepare)
                            .thenRunAsync(
                                    LocalizedTextProvider::initializeButtonCaches,
                                    applyExecutor
                            );
                }
        );
	}
}
