package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.api.Preparable;
import com.balugaq.rw.api.RWBlockBreakContext;
import com.balugaq.rw.implementation.RebarWorldEdit;
import com.balugaq.rw.utils.CommandUtil;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CloneCommand extends SubCommand implements Preparable {
    private static final String KEY = "clone";
    private final List<String> FLAGS = List.of("override");
    @NotNull
    private final IRebarWorldEdit plugin;

    public CloneCommand(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
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

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(player, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(commandSender, "error.too-many-blocks", range, max);
            return false;
        }

        plugin.send(player, "command.clone.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));
        final long currentMillSeconds = System.currentTimeMillis();

        final boolean override = CommandUtil.hasFlag(args, "override") || CommandUtil.hasFlag(args, "o");
        final AtomicInteger count = new AtomicInteger();
        final Location playerLocation = player.getLocation();
        final ItemStack itemInHand = player.getInventory().getItemInMainHand();

        final int dx = playerLocation.getBlockX() - pos1.getBlockX();
        final int dy = playerLocation.getBlockY() - pos1.getBlockY();
        final int dz = playerLocation.getBlockZ() - pos1.getBlockZ();

        final UUID uuid = player.getUniqueId();
        final boolean prepareMode = hasPreparedArgs(args);
        if (isPreparing(uuid) || !prepareMode) {
            removeDisplayGroupFor(uuid);
        }

        WorldUtils.doWorldEdit(player, pos1, pos2, (fromLocation -> {
            final Block fromBlock = fromLocation.getBlock();
            final Block toBlock = playerLocation.getWorld().getBlockAt(fromLocation.getBlockX() + dx, fromLocation.getBlockY() + dy, fromLocation.getBlockZ() + dz);
            final Location toLocation = toBlock.getLocation();
            if (prepareMode) {
                display(uuid, toLocation, fromBlock.getBlockData());
                return;
            }

            final RebarBlock rebarBlock = BlockStorage.get(fromLocation);

            // If vanilla block, just copy block state.
            // If Rebar block, should create Rebar Block first, then copy BlockState.
            if (rebarBlock == null) {
                // Block Data
                WorldUtils.copyBlockState(fromBlock.getState(), toBlock);
            }

            // Count means successful pasting block data. Not including Rebar data.
            count.addAndGet(1);

            // Rebar Data
            if (rebarBlock == null) {
                return;
            }

            final RebarBlock fromRebarBlock = BlockStorage.get(fromLocation);
            if (override) {
                BlockStorage.breakBlock(toLocation, RWBlockBreakContext.create(toLocation));
            }

            // Rebar Block
            BlockStorage.placeBlock(toLocation, fromRebarBlock.getKey(), new BlockCreateContext.PluginGenerate(RebarWorldEdit.getInstance(), BlockFace.NORTH, BlockFace.NORTH, toLocation.getBlock(), null));

            // Copy BlockState after creating Rebar block
            WorldUtils.copyBlockState(fromBlock.getState(), toBlock);

        }), () -> {
            if (prepareMode) {
                getDisplayGroup(uuid).getDisplays().forEach((name, display) -> {
                    display.setMetadata(RebarWorldEdit.getInstance().getName(), new FixedMetadataValue(RebarWorldEdit.getInstance(), true));
                });
            }
            plugin.send(player, "command.clone.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
        });

        return true;
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        if (args.length == 0) {
            List<String> left = new ArrayList<>();
            for (String flag : FLAGS) {
                if (!CommandUtil.hasFlag(args, flag)) {
                    left.add(flag);
                }
            }
            return left;
        }
        return new ArrayList<>();
    }
}
