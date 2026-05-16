package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ClearPosCommand {
    public static final String KEY = "clearpos";
    @NotNull
    private final IRebarWorldedit plugin;

    public ClearPosCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @ParametersAreNonnullByDefault
    public void execute(@NotNull CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        plugin.getCommandManager().clearSelection(player.getUniqueId());
    }
    
    @NotNull
    public String getKey() {
        return KEY;
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
