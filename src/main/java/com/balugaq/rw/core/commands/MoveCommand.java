package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.Facing;
import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.api.Preparable;
import com.balugaq.rw.core.managers.DisplayManager;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MoveCommand extends SubCommand implements Preparable {
    private static final String KEY = "move";
    @NotNull
    private final IRebarWorldEdit plugin;

    public MoveCommand(@NotNull IRebarWorldEdit plugin) {
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

        UUID uuid = player.getUniqueId();
        int distance = 1;
        if (args.length >= 1) {
            String s = args[0];
            if (s.equalsIgnoreCase("cancel")) {
                DisplayManager.killDisplays(uuid);
                plugin.send(player, "command.move.success");
                return true;
            }
            try {
                distance = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                plugin.send(player, "error.invalid-argument", s);
                return false;
            }
        }

        Facing facing = WorldUtils.getFacing(player.getLocation().getYaw(), player.getLocation().getPitch());
        move(uuid, facing, distance);

        plugin.send(player, "command.move.success");

        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        return new ArrayList<>();
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }
}
