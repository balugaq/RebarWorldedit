package com.balugaq.rw.utils;

import com.balugaq.rw.core.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class PermissionUtil {
    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        if (sender.isOp() || sender.hasPermission(Constants.PERMISSION_ADMIN) || sender.hasPermission(Constants.PERMISSION_COMMAND_ADMIN) || sender.hasPermission(permission)) {
            return true;
        }

        return false;
    }

    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull SubCommand subCommand) {
        return hasPermission(sender, "rebarWorldedit.command." + subCommand.getKey().toLowerCase());
    }
}
