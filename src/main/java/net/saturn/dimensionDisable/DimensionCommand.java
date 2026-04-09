package net.saturn.dimensionDisable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
public class DimensionCommand {

    private final DimensionDisable plugin;

    public DimensionCommand(DimensionDisable plugin) {
        this.plugin = plugin;
    }

    public void register(Commands commands) {
        LiteralCommandNode<CommandSourceStack> node = Commands.literal("dimensiondisable")
                .requires(source -> source.getSender().hasPermission("dimensiondisable.admin"))
                .executes(ctx -> {
                    sendHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("dimension", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("nether");
                            builder.suggest("end");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("action", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("enable");
                                    builder.suggest("disable");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String dimension = StringArgumentType.getString(ctx, "dimension").toLowerCase();
                                    String action    = StringArgumentType.getString(ctx, "action").toLowerCase();
                                    CommandSender sender = ctx.getSource().getSender();

                                    if (!dimension.equals("nether") && !dimension.equals("end")) {
                                        sender.sendMessage(Component.text("Unknown dimension. Use nether or end.", NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    boolean disable = switch (action) {
                                        case "disable", "true",  "on"  -> true;
                                        case "enable",  "false", "off" -> false;
                                        default -> {
                                            sender.sendMessage(Component.text("Unknown action. Use enable or disable.", NamedTextColor.RED));
                                            yield dimension.equals("nether")
                                                    ? plugin.isNetherDisabled()
                                                    : plugin.isEndDisabled();
                                        }
                                    };

                                    if (dimension.equals("nether")) {
                                        plugin.setNetherDisabled(disable);
                                        sender.sendMessage(Component.text(
                                                "The Nether has been " + (disable ? "disabled" : "enabled") + ".",
                                                disable ? NamedTextColor.RED : NamedTextColor.GREEN));
                                    } else {
                                        plugin.setEndDisabled(disable);
                                        sender.sendMessage(Component.text(
                                                "The End has been " + (disable ? "disabled" : "enabled") + ".",
                                                disable ? NamedTextColor.RED : NamedTextColor.GREEN));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();

        commands.register(node, "Toggle Nether/End access", java.util.List.of("dd"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== DimensionDisable ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/dimensiondisable <nether|end> <enable|disable>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Nether: ", NamedTextColor.GRAY)
                .append(Component.text(plugin.isNetherDisabled() ? "Disabled" : "Enabled",
                        plugin.isNetherDisabled() ? NamedTextColor.RED : NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("End:    ", NamedTextColor.GRAY)
                .append(Component.text(plugin.isEndDisabled() ? "Disabled" : "Enabled",
                        plugin.isEndDisabled() ? NamedTextColor.RED : NamedTextColor.GREEN)));
    }
}