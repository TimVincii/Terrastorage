package me.timvinci;

import me.timvinci.command.TerrastorageClientCommands;
import me.timvinci.config.ClientConfigManager;
import me.timvinci.network.ClientReceiverRegistry;
import me.timvinci.render.BarrelBlockEntityRenderer;
import me.timvinci.util.Reference;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint class for the client side.
 */
public class TerrastorageClient implements ClientModInitializer {
	public static final Logger CLIENT_LOGGER = LoggerFactory.getLogger(Reference.MOD_ID + "_client");

	/**
	 * Initializes the client config manager, client commands, client global receivers, and the custom barrel block
	 * entity renderer.
	 */
	@Override
	public void onInitializeClient() {
		ClientConfigManager.init();
		TerrastorageClientCommands.registerCommands();
		ClientReceiverRegistry.registerReceivers();
		BlockEntityRendererFactories.register(BlockEntityType.BARREL, BarrelBlockEntityRenderer::new);
	}
}