package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SetPos1Command {
    public static final String KEY = "pos1";
    @NotNull
    private final IRebarWorldedit plugin;

    public SetPos1Command(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @ParametersAreNonnullByDefault
    public void execute(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        plugin.getCommandManager().setPos1(player.getUniqueId(), player.getLocation().getBlock().getLocation());
        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());
        if (pos2 != null) {
            plugin.send(player, "command.setpos1.success-with-range", "pos", WorldUtils.fineLocStr(pos1), "range", WorldUtils.locationRange(pos1, pos2));
        } else {
            plugin.send(player, "command.setpos1.success", "pos", WorldUtils.fineLocStr(pos1));
        }
    }


    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()) && source.getSender() instanceof Player)
                .executes(ctx -> {
                    execute(ctx);
                    return SINGLE_SUCCESS;
                });
    }
    
    @NotNull
    public String getKey() {
        return KEY;
    }
}
