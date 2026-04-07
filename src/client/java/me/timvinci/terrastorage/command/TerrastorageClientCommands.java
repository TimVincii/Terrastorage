package me.timvinci.terrastorage.command;

import me.timvinci.terrastorage.gui.TerrastorageOptionsScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

/**
 * Provides a method for registering client commands.
 */
public class TerrastorageClientCommands {

    /**
     * Registers the client command for opening the options screen.
     */
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("tsclient")
                .then(ClientCommands.literal("options")
                    .executes(context -> {
                        Minecraft client = context.getSource().getClient();
                        client.schedule(() -> {
                            client.setScreen(new TerrastorageOptionsScreen(client.screen));
                        });
                        return 1;
                    })
                )
            );
        });
    }
}
