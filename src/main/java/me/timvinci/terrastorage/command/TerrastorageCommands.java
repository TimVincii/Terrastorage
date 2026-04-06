package me.timvinci.terrastorage.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.timvinci.terrastorage.Terrastorage;
import me.timvinci.terrastorage.config.ConfigManager;
import me.timvinci.terrastorage.config.TerrastorageConfig;
import me.timvinci.terrastorage.network.NetworkHandler;
import me.timvinci.terrastorage.util.Reference;
import me.timvinci.terrastorage.util.TextStyler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionTypes;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(Reference.MOD_ID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("action-cooldown")
                    .executes(context -> executeGetValue(context, config::getActionCooldown, "Action Cooldown", " ticks"))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                            .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setActionCooldown, "Action Cooldown", " ticks"))
                    )
                )
                .then(Commands.literal("line-of-sight-check")
                    .executes(context -> executeGetValue(context, config::getLineOfSightCheck, "Line Of Sight Check", ""))
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(context -> executeSetValue(context, BoolArgumentType.getBool(context, "value"), config::setLineOfSightCheck, "Line Of Sight Check", ""))
                    )
                )
                .then(Commands.literal("quick-stack-range")
                    .executes(context -> executeGetValue(context, config::getQuickStackRange, "Quick Stack Range", " blocks"))
                    .then(Commands.argument("value", IntegerArgumentType.integer(3, 48))
                            .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setQuickStackRange, "Quick Stack Range", " blocks"))
                    )
                )
                .then(Commands.literal("item-animation-length")
                    .executes(context -> executeGetValue(context, config::getItemAnimationLength, "Item Animation Length", " ticks"))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 200))
                            .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setItemAnimationLength, "Item Animation Length", " ticks"))
                    )
                )
                .then(Commands.literal("item-animation-interval")
                    .executes(context -> executeGetValue(context, config::getItemAnimationInterval, "Item Animation Interval", " ticks"))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                            .executes(context -> executeSetValue(context, IntegerArgumentType.getInteger(context, "value"), config::setItemAnimationInterval, "Item Animation Interval", " ticks"))
                    )
                )
                .then(Commands.literal("keep-favorites-on-drop")
                    .executes(context -> executeGetValue(context, config::getKeepFavoritesOnDrop, "Keep Favorites On Drop", ""))
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(context -> executeSetValue(context, BoolArgumentType.getBool(context, "value"), config::setKeepFavoritesOnDrop, "Keep Favorites On Drop", ""))
                    )
                );

            if (Terrastorage.environmentIsServer) {
                command.then(Commands.literal("enable-item-favoriting")
                        .executes(context -> executeGetValue(context, config::getEnableItemFavoriting, "Enable Item Favoriting", ""))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> executeUpdateItemFavoriting(context, BoolArgumentType.getBool(context, "value"), config::setEnableItemFavoriting))
                        )
                );
            }

            dispatcher.register(command);
        });
    }

    /**
     * Sends the value of a property to the command issuer.
     * @param context The command context.
     * @param getter The getter of the property.
     * @param propertyName The name of the property.
     * @param valueUnit The unit of the property value.
     * @return 1, to state a successful command use.
     */
    private static <T> int executeGetValue(CommandContext<CommandSourceStack> context, Supplier<T> getter, String propertyName, String valueUnit) {
        T currentValue = getter.get();
        context.getSource().sendSuccess(
                () -> TextStyler.styleGetProperty(propertyName, currentValue, valueUnit),
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
    private static <T> int executeSetValue(CommandContext<CommandSourceStack> context, T value, Consumer<T> setter, String propertyName, String valueUnit) {
        setter.accept(value);
        if (ConfigManager.getInstance().saveConfig()) {
            context.getSource().sendSuccess(
                    () -> TextStyler.stylePropertyUpdated(propertyName, value, valueUnit),
                    true
            );

            // Action cooldown is the only value that is held on the client side.
            if (propertyName.equals("Action Cooldown")) {
                NetworkHandler.sendGlobalServerConfigPayload(context.getSource().getServer());
            }
            else if (propertyName.equals("Quick Stack Range") && (Integer)value > 16) {
                context.getSource().sendSuccess(() -> TextStyler.warning("terrastorage.message.high_quick_stack_range"), false);
            }
        }
        else {
            context.getSource().sendSuccess(() -> TextStyler.error("terrastorage.message.server_saving_error"), false);
        }

        return 1;
    }

    private static int executeUpdateItemFavoriting(CommandContext<CommandSourceStack> context, boolean value, Consumer<Boolean> setter) {
        setter.accept(value);
        if (ConfigManager.getInstance().saveConfig()) {
            context.getSource().sendSuccess(() ->
                    TextStyler.styleTitle("Item Favoriting Updated\n")
                        .append(TextStyler.styleText(Component.translatable("terrastorage.message." + (value ? "enabled" : "disabled") + "_item_favoriting")))
                    ,false);
        }
        else {
            context.getSource().sendSuccess(() -> TextStyler.error("terrastorage.message.server_saving_error"), false);
        }

        return 1;
    }
}
