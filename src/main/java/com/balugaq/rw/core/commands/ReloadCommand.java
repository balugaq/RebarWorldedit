package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.pylonmc.rebar.Rebar;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ReloadCommand {
    public static final String KEY = "reload";
    @NotNull
    private final IRebarWorldedit plugin;

    public ReloadCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }

    
    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx) {
        Bukkit.getServer().getScheduler().runTask(Rebar.INSTANCE, () -> {
            Bukkit.getServer().getPluginManager().disablePlugin(plugin.getJavaPlugin());
            Bukkit.getServer().getPluginManager().enablePlugin(plugin.getJavaPlugin());
            plugin.send(ctx.getSource().getSender(), "command.reload.success");
        });
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()) && source.getSender() instanceof Player)
                .executes(ctx -> {
                    execute(ctx);
                    return SINGLE_SUCCESS;
                });
    }
}
