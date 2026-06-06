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

public class PasteCommand {
    public static final String KEY = "paste";
    @NotNull
    private final IRebarWorldedit plugin;

    public PasteCommand(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }


    public void execute(CommandContext<CommandSourceStack> ctx, @Nullable RebarBlockSchema schema, boolean override, boolean withoutblock) {
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

        plugin.send(player, "command.paste.start", "pos1", WorldUtils.fineLocStr(pos1), "pos2", WorldUtils.fineLocStr(pos2), "block", RebarRegistry.BLOCKS.get(blockId).getNameTranslationKey());

        final long currentMillSeconds = System.currentTimeMillis();

        final AtomicInteger count = new AtomicInteger();

        WorldUtils.doWorldEdit(player, pos1, pos2, (location -> {
            final Block targetBlock = location.getBlock();
            if (override) {
                BlockStorage.breakBlock(location, RWBlockBreakContext.create(location));
            }
            if (BlockStorage.get(location) == null) {
                BlockStorage.placeBlock(location, blockId, RWBlockCreateContext.create(targetBlock, !withoutblock));
                count.addAndGet(1);
            }
        }), () -> {
            plugin.send(player, "command.paste.success", "blocks", count.get(), "block", RebarRegistry.BLOCKS.get(blockId).getNameTranslationKey(), "time", System.currentTimeMillis() - currentMillSeconds);
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
                    execute(ctx, null, false, false);
                    return SINGLE_SUCCESS;
                })
                .then(Commands.argument("blockId", new RegistryCommandArgument<>(RebarRegistry.BLOCKS))
                    .executes(ctx -> {
                        execute(
                                ctx, ctx.getArgument("blockId", RebarBlockSchema.class), false, false
                        );
                        return SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("override", BoolArgumentType.bool())
                        .executes(ctx -> {
                            execute(
                                    ctx, ctx.getArgument("blockId", RebarBlockSchema.class), BoolArgumentType.getBool(ctx, "override"), false
                            );
                            return SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("withoutblock", BoolArgumentType.bool())
                            .executes(ctx -> {
                                execute(
                                    ctx, ctx.getArgument("blockId", RebarBlockSchema.class), BoolArgumentType.getBool(ctx, "override"), BoolArgumentType.getBool(ctx, "withoutblock")
                                );
                                return SINGLE_SUCCESS;
                             }))));
    }
}
