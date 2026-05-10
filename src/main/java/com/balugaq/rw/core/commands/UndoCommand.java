package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.Content;
import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UndoCommand extends SubCommand {
    private static final String KEY = "undo";
    @NotNull
    private final IRebarWorldEdit plugin;

    public UndoCommand(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }

    @Override
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
        List<Content> contents = plugin.getCommandManager().leftBackup(uuid);
        if (contents.isEmpty()) {
            plugin.send(player, "error.no-undo");
            return false;
        }

        for (Content content : contents) {
            content.load();
        }

        plugin.send(player, "command.undo.success");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
