package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.CachedRequest;
import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ConfirmCommand {
    public static final String KEY = "confirm";
    @NotNull
    private final IRebarWorldedit plugin;

    public ConfirmCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }

    
    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        CachedRequest request = plugin.getCommandManager().pullCachedRequest(sender);
        if (request == null) {
            plugin.send(sender, "error.no-request");
            return;
        }

        plugin.send(sender, "command.confirm.success");
        request.execute();
    }


    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()))
                .executes(ctx -> {
                    execute(ctx);
                    return SINGLE_SUCCESS;
                });
    }
}
