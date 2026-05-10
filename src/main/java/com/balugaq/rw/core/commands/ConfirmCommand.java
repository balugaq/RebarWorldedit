package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.CachedRequest;
import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class ConfirmCommand extends SubCommand {
    private static final String KEY = "confirm";
    @NotNull
    private final IRebarWorldEdit plugin;

    public ConfirmCommand(@NotNull IRebarWorldEdit plugin) {
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

        CachedRequest request = plugin.getCommandManager().pullCachedRequest(commandSender);
        if (request == null) {
            plugin.send(commandSender, "error.no-request");
            return false;
        }

        plugin.send(commandSender, "command.confirm.success");
        request.execute();
        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
