package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class VersionCommand {
    public static final String KEY = "version";
    @NotNull
    private final IRebarWorldedit plugin;

    public VersionCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }

    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx) {
        plugin.send(ctx.getSource().getSender(), "command.version.content", "name", plugin.getJavaPlugin().getName(), "version", plugin.getDescription().getVersion());
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
