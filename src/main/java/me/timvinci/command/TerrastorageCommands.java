package me.timvinci.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.timvinci.config.ConfigManager;
import me.timvinci.config.TerrastorageConfig;
import me.timvinci.network.NetworkHandler;
import me.timvinci.util.Reference;
import me.timvinci.util.TextStyler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Provides a method for registering commands.
 */
public class TerrastorageCommands {

    /**
     * Registers the commands for modifying the server config properties.
     */
    public static void registerCommands() {
        TerrastorageConfig config = ConfigManager.getInstance().getConfig();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal(Reference.MOD_ID)
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("action-cooldown")
                    .executes(context -> executeGetValue(context, config::getActionCooldown, "action cooldown", " ticks"))
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(2, 100))
                        .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setActionCooldown, "Action Cooldown", " ticks")))
                )
                .then(CommandManager.literal("line-of-sight-check")
                    .executes(context -> executeGetValue(context, config::getLineOfSightCheck, "line of sight check", ""))
                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> executeSetValue(context, BoolArgumentType.getBool(context, "value"), config::setLineOfSightCheck, "Line Of Sight Check", "")))
                )
                .then(CommandManager.literal("quick-stack-range")
                    .executes(context -> executeGetValue(context, config::getQuickStackRange, "quick stack range", " blocks"))
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(3, 16))
                        .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setQuickStackRange, "Quick Stack Range", " blocks"))
                    )
                )
                .then(CommandManager.literal("item-animation-length")
                    .executes(context -> executeGetValue(context, config::getItemAnimationLength, "item animation length", " ticks"))
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(10, 200))
                        .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setItemAnimationLength, "Item Animation Length", " ticks"))
                    )
                )
                .then(CommandManager.literal("item-animation-interval")
                    .executes(context -> executeGetValue(context, config::getItemAnimationInterval, "item animation interval", " ticks"))
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 20))
                            .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setItemAnimationInterval, "Item Animation Interval", " ticks"))
                    )
                )
            )
        );
    }

    /**
     * Sends the value of a property to the command issuer.
     * @param context The command context.
     * @param getter The getter of the property.
     * @param propertyName The name of the property.
     * @param valueUnit The unit of the property value.
     * @return 1, to state a successful command use.
     */
    private static <T> int executeGetValue(CommandContext<ServerCommandSource> context, Supplier<T> getter, String propertyName, String valueUnit) {
        T currentValue = getter.get();
        context.getSource().sendFeedback(
                () -> TextStyler.styleKeyValue("Current " + propertyName, currentValue + valueUnit + "."),
                false
        );

        return 1;
    }

    /**
     * Changes the value of a property, and sends a reply to the command issuer.
     * @param context The command context.
     * @param value The new value.
     * @param setter The setter of the property.
     * @param propertyName The name of the property.
     * @param valueUnit The unit of the property value.
     * @return 1, to state a successful command use.
     */
    private static <T> int executeSetValue(CommandContext<ServerCommandSource> context, T value, Consumer<T> setter, String propertyName, String valueUnit) {
        setter.accept(value);
        if (ConfigManager.getInstance().saveConfig()) {
            context.getSource().sendFeedback(
                    () -> TextStyler.styleTitle(propertyName + " Updated")
                            .append("\n")
                            .append(TextStyler.styleKeyValue("New value", value + valueUnit + ".")),
                    true
            );

            // Action cooldown is the only value that is held on the client side.
            if (propertyName.equals("Action Cooldown")) {
                NetworkHandler.sendGlobalServerConfigPayload(context.getSource().getServer());
            }
        }
        else {
            context.getSource().sendFeedback(() -> TextStyler.styleError("terrastorage.message.server_saving_error"),false);
        }

        return 1;
    }
}
