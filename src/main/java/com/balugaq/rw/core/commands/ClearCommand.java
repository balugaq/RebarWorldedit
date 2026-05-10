package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.api.RWBlockBreakContext;
import com.balugaq.rw.utils.CommandUtil;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand extends SubCommand {
    private static final String KEY = "clear";
    @NotNull
    private final IRebarWorldEdit plugin;

    public ClearCommand(@NotNull IRebarWorldEdit plugin) {
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

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(player, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(player, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(player, "error.too-many-blocks", range, max);
            return false;
        }

        plugin.send(player, "command.clear.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final boolean skipVanilla = CommandUtil.hasFlag(args, "skipvanilla") || CommandUtil.hasFlag(args, "v");
        final boolean skipRebar = CommandUtil.hasFlag(args, "skiprebar") || CommandUtil.hasFlag(args, "s");
        if (skipVanilla && skipRebar) {
            plugin.send(player, "error.both-skipped");
            return false;
        }
        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        WorldUtils.doWorldEdit(player, pos1, pos2, (location -> {
            final Block targetBlock = pos1.getWorld().getBlockAt(location);
            if (!skipRebar) {
                if (BlockStorage.get(location) != null) {
                    BlockStorage.breakBlock(location, RWBlockBreakContext.create(targetBlock));
                }
            }
            if (!skipVanilla) {
                if (BlockStorage.get(location) == null) {
                    targetBlock.setType(Material.AIR);
                }
                count.addAndGet(1);
            }
        }), () -> {
            plugin.send(player, "command.clear.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
        });

        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        final List<String> completions = new ArrayList<>();
        if (!CommandUtil.hasFlag(args, "c") && !CommandUtil.hasFlag(args, "callhandler")) {
            completions.add("-callhandler");
            completions.add("-c");
        }
        if (!CommandUtil.hasFlag(args, "v") && !CommandUtil.hasFlag(args, "skipvanilla")) {
            completions.add("-skipvanilla");
            completions.add("-v");
        }
        if (!CommandUtil.hasFlag(args, "s") && !CommandUtil.hasFlag(args, "skiprebar")) {
            completions.add("-skiprebar");
            completions.add("-s");
        }
        return completions;
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }
}
