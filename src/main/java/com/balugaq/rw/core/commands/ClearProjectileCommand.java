package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.core.managers.DisplayManager;
import com.balugaq.rw.implementation.RebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClearProjectileCommand extends SubCommand {
    private static final String KEY = "clearprojectile";
    private final IRebarWorldEdit plugin;

    public ClearProjectileCommand(IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getKey() {
        return KEY;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
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

        RebarWorldEdit plugin = RebarWorldEdit.getInstance();
        DisplayManager.halt();
        plugin.getDisplayManager().onLoad();

        player.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof Display display) {
                List<MetadataValue> metadata = display.getMetadata(RebarWorldEdit.getInstance().getName());
                if (!metadata.isEmpty() && metadata.get(0).asBoolean()) {
                    display.remove();
                }
            }
        });

        plugin.send(player, "commands.clearprojectile.success");

        return true;
    }
}
