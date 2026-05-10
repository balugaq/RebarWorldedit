package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HelpCommand extends SubCommand {
    private static final String KEY = "help";
    @NotNull
    private final IRebarWorldEdit plugin;

    public HelpCommand(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (args.length == 0) {
            plugin.sendList(commandSender, "messages.command.help.content");
            return true;
        }

        final String subCommand = args[0];
        final AtomicBoolean found = new AtomicBoolean(false);
        plugin.getCommandManager().iter(cmd -> {
            if (cmd.getKey().equals(subCommand)) {
                plugin.sendList(commandSender, "messages.command.help.usage." + cmd.getKey());
                found.set(true);
            }
        });

        if (!found.get()) {
            plugin.send(commandSender, "error.unknown-subcommand", subCommand);
        }

        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        if (args.length <= 1) {
            final List<String> result = new ArrayList<>();
            plugin.getCommandManager().iter(cmd -> result.add(cmd.getKey()));
            return result;
        }
        return new ArrayList<>();
    }
}
