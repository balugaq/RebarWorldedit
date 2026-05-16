package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class RuleCommand {
    public static final String KEY = "rule";

    @NotNull
    private final IRebarWorldedit plugin;

    public RuleCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx, String rule, Object value) {
        plugin.getConfigManager().setConfig(rule, value);
        plugin.send(ctx.getSource().getSender(), "command.rule.success", "rule", rule, "value", value);
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()))
                .then(Commands.argument("rule", StringArgumentType.word())
                              .suggests((ctx, builder) ->
                                  builder.suggest("worldedit.modification-block-limit").buildFuture()
                              )
                              .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                  .executes(ctx -> {
                                      execute(ctx, "worldedit.modification-block-limit", IntegerArgumentType.getInteger(ctx, "value"));
                                      return SINGLE_SUCCESS;
                                  })))

                .then(Commands.argument("rule", StringArgumentType.word())
                              .suggests((ctx, builder) ->
                                  builder.suggest("worldedit.modification-chunk-limit-per-second").buildFuture()
                              )
                              .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                  .executes(ctx -> {
                                      execute(ctx, "worldedit.modification-chunk-limit-per-second", IntegerArgumentType.getInteger(ctx, "value"));
                                      return SINGLE_SUCCESS;
                                  })));

    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }
}
