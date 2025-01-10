package me.timvinci.terrastorage.config;

import me.timvinci.terrastorage.network.s2c.ServerConfigPayload;

/**
 * Holds the synced server config properties.
 */
public class ServerConfigHolder {
    public static int actionCooldown = 10;
    public static boolean enableItemFavoriting = true;

    public static void apply(ServerConfigPayload serverConfigPayload) {
        actionCooldown = serverConfigPayload.actionCooldown();
        enableItemFavoriting = serverConfigPayload.enableItemFavoriting();
    }
}
