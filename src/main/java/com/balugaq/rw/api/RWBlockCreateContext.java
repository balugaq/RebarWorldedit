package com.balugaq.rw.api;

import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record RWBlockCreateContext(@Nullable Player player, Block block, boolean shouldSetType) implements BlockCreateContext {
    public static RWBlockCreateContext create(@Nullable Player player, Block block, boolean shouldSetType) {
        return new RWBlockCreateContext(player, block, shouldSetType);
    }
    
    @Override
    public @Nullable Player getPlayer() {
        return player;
    }

    @Override
    public BlockFace getFacing() {
        return BlockFace.NORTH;
    }

    @Override
    public BlockFace getFacingVertical() {
        return BlockFace.NORTH;
    }

    @Override
    public @Nullable ItemStack getItem() {
        return null;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldSetType() {
        return shouldSetType;
    }
}
