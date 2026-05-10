package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class SetPos1Command extends SubCommand {
    private static final String KEY = "pos1";
    @NotNull
    private final IRebarWorldEdit plugin;

    public SetPos1Command(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            plugin.send(commandSender, "error.player-only");
            return false;
        }

        plugin.getCommandManager().setPos1(player.getUniqueId(), player.getLocation().getBlock().getLocation());
        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());
        if (pos2 != null) {
            plugin.send(player, "command.setpos1.success-with-range", WorldUtils.locationToString(pos1), WorldUtils.locationRange(pos1, pos2));
        } else {
            plugin.send(player, "command.setpos1.success", WorldUtils.locationToString(pos1));
        }
        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }
}
