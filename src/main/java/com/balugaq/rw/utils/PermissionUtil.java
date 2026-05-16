package com.balugaq.rw.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class PermissionUtil {
    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        return sender.isOp() || sender.hasPermission(Constants.PERMISSION_ADMIN) || sender.hasPermission(Constants.PERMISSION_COMMAND_ADMIN) || sender.hasPermission(permission);
    }
}
