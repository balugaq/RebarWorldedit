package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.api.Preparable;
import com.balugaq.rw.api.RWBlockBreakContext;
import com.balugaq.rw.api.RWBlockCreateContext;
import com.balugaq.rw.implementation.RebarWorldEdit;
import com.balugaq.rw.utils.CommandUtil;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

public class PasteCommand extends SubCommand implements Preparable {
    private static final String KEY = "paste";
    private static final List<String> FLAGS = List.of("override", "force", "withoutblock");
    @NotNull
    private final IRebarWorldEdit plugin;

    public PasteCommand(@NotNull IRebarWorldEdit plugin) {
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

        final boolean override = CommandUtil.hasFlag(args, "override") || CommandUtil.hasFlag(args, "o");
        final boolean force = CommandUtil.hasFlag(args, "force") || CommandUtil.hasFlag(args, "f");
        final boolean withoutblock = CommandUtil.hasFlag(args, "withoutblock") || CommandUtil.hasFlag(args, "w");
        int length = args.length;
        if (override) {
            length--;
        }
        if (force) {
            length--;
        }

        final NamespacedKey blockId;
        if (length < 1) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            RebarItem item = RebarItem.fromStack(hand);
            if (item == null || item.getSchema().getRebarBlockKey() == null) {
                plugin.send(player, "error.missing-argument", "rebar-block-id");
                return false;
            } else {
                blockId = item.getSchema().getRebarBlockKey();
            }
        } else {
            blockId = NamespacedKey.fromString(args[0]);
        }

        if (blockId == null || RebarRegistry.BLOCKS.get(blockId) == null) {
            plugin.send(player, "error.invalid-rebar-block-id");
            return false;
        }

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
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

        plugin.send(player, "command.paste.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));

        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        final Material t = RebarRegistry.BLOCKS.get(blockId).getMaterial();

        final UUID uuid = player.getUniqueId();
        final boolean prepareMode = hasPreparedArgs(args);
        if (isPreparing(uuid) || !prepareMode) {
            removeDisplayGroupFor(uuid);
        }

        WorldUtils.doWorldEdit(player, pos1, pos2, (location -> {
            if (prepareMode) {
                display(uuid, location, t);
                return;
            }

            final Block targetBlock = location.getBlock();
            if (override) {
                BlockStorage.breakBlock(location, RWBlockBreakContext.create(location));
            }
            if (BlockStorage.get(location) == null) {
                if (!withoutblock) {
                    targetBlock.setType(t);
                }
                BlockStorage.placeBlock(location, blockId, RWBlockCreateContext.create(targetBlock));
                count.addAndGet(1);
            }
        }), () -> {
            if (prepareMode) {
                getDisplayGroup(uuid).getDisplays().forEach((name, display) -> {
                    display.setMetadata(RebarWorldEdit.getInstance().getName(), new FixedMetadataValue(RebarWorldEdit.getInstance(), true));
                });
            }
            plugin.send(player, "command.paste.success", count.get(), System.currentTimeMillis() - currentMillSeconds);
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

        if (args.length == 1) {
            return RebarRegistry.BLOCKS.stream().map(s -> s.getKey().toString()).toList();
        }

        List<String> left = new ArrayList<>();
        for (String flag : FLAGS) {
            if (!CommandUtil.hasFlag(args, flag)) {
                left.add("-" + flag);
            }
        }

        left.addAll(prepareArgs(args));
        return left;
    }
}
