package me.timvinci.command;

import me.timvinci.gui.TerrastorageOptionsScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

/**
 * Provides a method for registering client commands.
 */
public class TerrastorageClientCommands {

    /**
     * Registers the client command for opening the options screen.
     */
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("tsclient")
                .then(ClientCommandManager.literal("options")
                    .executes(context -> {
                        MinecraftClient client = context.getSource().getClient();
                        client.send(() -> {
                            client.setScreen(new TerrastorageOptionsScreen(client.currentScreen));
                        });
                        return 1;
                    })
                )
            );
        });
    }
}
