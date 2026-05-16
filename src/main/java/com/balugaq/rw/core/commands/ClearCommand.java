package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.api.RWBlockBreakContext;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ClearCommand {
    public static final String KEY = "clear";
    @NotNull
    private final IRebarWorldedit plugin;

    public ClearCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }
    
    private void execute(CommandContext<CommandSourceStack> ctx, boolean skipVanilla, boolean skipRebar) {
        CommandSender commandSender = ctx.getSource().getSender();
        if (!PermissionUtil.hasPermission(commandSender, KEY)) {
            plugin.send(commandSender, "error.no-permission");
            return;
        }

        if (!(commandSender instanceof Player player)) {
            plugin.send(commandSender, "error.player-only");
            return;
        }

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(player, "error.no-selection");
            return;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(player, "error.world-mismatch");
            return;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(player, "error.too-many-blocks", range, max);
            return;
        }

        plugin.send(player, "command.clear.start", "pos1", WorldUtils.fineLocStr(pos1), "pos2", WorldUtils.fineLocStr(pos2));

        final long currentMillSeconds = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger();
        WorldUtils.doWorldEdit(
                player, pos1, pos2, (location -> {
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
                    plugin.send(player, "command.clear.success", "blocks", count.get(), "time", System.currentTimeMillis() - currentMillSeconds);
                }
        );
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()) && source.getSender() instanceof Player)
                .executes(ctx -> {
                    execute(ctx, false, false);
                    return SINGLE_SUCCESS;
                })
                .then(Commands.argument("skip", StringArgumentType.word())
                    .executes(ctx -> {
                        execute(
                            ctx,
                            "-skip-vanilla".equalsIgnoreCase(StringArgumentType.getString(ctx, "skip")),
                            "-skip-rebar".equalsIgnoreCase(StringArgumentType.getString(ctx, "skip"))
                        );
                        return SINGLE_SUCCESS;
                    }));
    }
}
