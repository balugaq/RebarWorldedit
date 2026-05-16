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
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.command.RegistryCommandArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SetblockCommand {
    public static final String KEY = "setblock";
    @NotNull
    private final IRebarWorldedit plugin;

    public SetblockCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSourceStack> ctx, @Nullable BlockPosition bpos, @Nullable RebarBlockSchema schema, boolean withoutblock) {
        Player player = (Player) ctx.getSource().getSender();

        NamespacedKey blockId;
        if (schema == null) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            RebarItem item = RebarItem.fromStack(hand);
            if (item == null || item.getSchema().getRebarBlockKey() == null) {
                plugin.send(player, "error.missing-argument", "rebar-block-id");
                return;
            } else {
                blockId = item.getSchema().getRebarBlockKey();
            }
        } else{
            blockId = schema.getKey();
        }

        final Location pos;

        if (bpos == null) {
            if (plugin.getCommandManager().getPos1(player.getUniqueId()) != null) {
                pos = plugin.getCommandManager().getPos1(player.getUniqueId()).toLocation(player.getWorld());
            }
            else if (plugin.getCommandManager().getPos2(player.getUniqueId()) != null) {
                pos = plugin.getCommandManager().getPos1(player.getUniqueId()).toLocation(player.getWorld());
            } else {
                pos = null;
            }
        } else {
            pos = bpos.toLocation(player.getWorld());
        }

        if (pos == null) {
            plugin.send(player, "error.no-selection");
            return;
        }

        plugin.send(player, "command.setblock.start", "block", WorldUtils.locationToString(pos));

        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();

        WorldUtils.doWorldEdit(player, pos, pos, (location -> {
            final Block targetBlock = location.getBlock();
            if (BlockStorage.get(location) == null) {
                BlockStorage.placeBlock(location, blockId, RWBlockCreateContext.create(targetBlock, !withoutblock));
                count.addAndGet(1);
            }
        }), () -> {
            plugin.send(player, "command.setblock.success", "block", RebarRegistry.BLOCKS.get(blockId).getNameTranslationKey(), "time", System.currentTimeMillis() - currentMillSeconds);
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
                    execute(ctx, null, null, false);
                    return SINGLE_SUCCESS;
                })
                .then(Commands.argument("blockId", new RegistryCommandArgument<>(RebarRegistry.BLOCKS))
                    .executes(ctx -> {
                        execute(
                              ctx, null, ctx.getArgument("blockId", RebarBlockSchema.class), false
                        );
                        return SINGLE_SUCCESS;
                    })
                        .then(Commands.argument("withoutblock", BoolArgumentType.bool()))
                        .executes(ctx -> {
                            execute(
                                  ctx, null, ctx.getArgument("blockId", RebarBlockSchema.class), BoolArgumentType.getBool(ctx, "withoutblock")
                            );
                            return SINGLE_SUCCESS;
                        }))
                .then(Commands.argument("pos", ArgumentTypes.blockPosition())
                      .executes(ctx -> {
                          execute(
                                ctx, ctx.getArgument("pos", BlockPosition.class), null, false
                          );
                          return SINGLE_SUCCESS;
                      })
                      .then(Commands.argument("blockId", new RegistryCommandArgument<>(RebarRegistry.BLOCKS))
                            .executes(ctx -> {
                                execute(
                                        ctx, null, ctx.getArgument("blockId", RebarBlockSchema.class), false
                                );
                                return SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("withoutblock", BoolArgumentType.bool()))
                            .executes(ctx -> {
                                execute(
                                        ctx, null, ctx.getArgument("blockId", RebarBlockSchema.class), BoolArgumentType.getBool(ctx, "withoutblock")
                                );
                                return SINGLE_SUCCESS;
                            })));
    }
}
