package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import io.github.pylonmc.rebar.Rebar;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {
    private static final String KEY = "reload";
    @NotNull
    private final IRebarWorldEdit plugin;

    public ReloadCommand(@NotNull IRebarWorldEdit plugin) {
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
        Bukkit.getServer().getScheduler().runTask(Rebar.INSTANCE, () -> {
            final String message = plugin.getString("messages.command.reload.success");

            Bukkit.getServer().getPluginManager().disablePlugin(plugin.getJavaPlugin());
            Bukkit.getServer().getPluginManager().enablePlugin(plugin.getJavaPlugin());
            commandSender.sendMessage(message);
        });
        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
