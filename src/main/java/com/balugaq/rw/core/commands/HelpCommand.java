package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.implementation.RebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand {
    public static final String KEY = "help";
    @NotNull
    private final IRebarWorldedit plugin;

    public HelpCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }


    @NotNull
    public String getKey() {
        return KEY;
    }


    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx, @Nullable String subcommand) {
        CommandSender sender = ctx.getSource().getSender();
        if (subcommand == null) {
            plugin.send(sender, "command.help.content");
            return;
        }

        plugin.send(sender, "error.unknown-subcommand", subcommand);
    }


    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()) && source.getSender() instanceof Player)
                .executes(ctx -> {
                    execute(ctx, null);
                    return SINGLE_SUCCESS;
                })
                .then(Commands.argument("subcommand", StringArgumentType.word())
                              .suggests((ctx, builder) -> {
                                  for (String subcommand : RebarWorldedit.getInstance().getCommandManager().getSubCommands()) {
                                      builder.suggest(subcommand);
                                  }
                                  return builder.buildFuture();
                              })
                              .executes(ctx -> {
                                  execute(
                                          ctx,
                                          StringArgumentType.getString(ctx, "subcommand")
                                  );
                                  return SINGLE_SUCCESS;
                              }));
    }
}
