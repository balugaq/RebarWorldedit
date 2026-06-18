package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.api.RWBlockBreakContext;
import com.balugaq.rw.api.RWBlockCreateContext;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.WorldUtils;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CloneCommand {
    public static final String KEY = "clone";

    @NotNull
    private final IRebarWorldedit plugin;

    public CloneCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    
    public void execute(CommandContext<CommandSourceStack> ctx, boolean override) {
        Player player = (Player) ctx.getSource().getSender();
        
        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
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

        plugin.send(player, "command.clone.start", "pos1", WorldUtils.fineLocStr(pos1), "pos2", WorldUtils.fineLocStr(pos2));
        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();
        final Location playerLocation = player.getLocation();

        final int dx = playerLocation.getBlockX() - pos1.getBlockX();
        final int dy = playerLocation.getBlockY() - pos1.getBlockY();
        final int dz = playerLocation.getBlockZ() - pos1.getBlockZ();

        WorldUtils.doWorldEdit(player, pos1, pos2, (fromLocation -> {
            final Block fromBlock = fromLocation.getBlock();
            final Block toBlock = playerLocation.getWorld().getBlockAt(fromLocation.getBlockX() + dx, fromLocation.getBlockY() + dy, fromLocation.getBlockZ() + dz);
            final Location toLocation = toBlock.getLocation();

            final RebarBlock fromRebarBlock = BlockStorage.get(fromLocation);

            // If vanilla block, just copy block state.
            // If Rebar block, should create Rebar Block first, then copy BlockState.
            if (fromRebarBlock == null) {
                // Block Data
                WorldUtils.copyBlockState(fromBlock.getState(), toBlock);
            }

            // Count means successful pasting block data. Not including Rebar data.
            count.addAndGet(1);

            // Rebar Data
            if (fromRebarBlock == null) {
                return;
            }

            if (override) {
                BlockStorage.breakBlock(toLocation, RWBlockBreakContext.create(toLocation));
            }

            // Rebar Block
            BlockStorage.placeBlock(toLocation, fromRebarBlock.getKey(), RWBlockCreateContext.create(player, toBlock, false));

            // Copy BlockState after creating Rebar block
            WorldUtils.copyBlockState(fromBlock.getState(), toBlock);

        }), () -> {
            plugin.send(player, "command.clone.success", "blocks", count.get(), "time", System.currentTimeMillis() - currentMillSeconds);
        });
    }

    
    @NotNull
    public String getKey() {
        return KEY;
    }

    public @NotNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal(getKey())
                .requires(source -> PermissionUtil.hasPermission(source.getSender(), getKey()) && source.getSender() instanceof Player)
                .executes(ctx -> {
                    execute(ctx, false);
                    return SINGLE_SUCCESS;
                })
                .then(Commands.argument("override", BoolArgumentType.bool())
                              .suggests((ctx, builder) -> builder.suggest("true").suggest("false").buildFuture())
                              .executes(ctx -> {
                                  execute(
                                          ctx,
                                          BoolArgumentType.getBool(ctx, "override")
                                  );
                                  return SINGLE_SUCCESS;
                              }));
    }
}
