package com.balugaq.rw.api;

import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RWBlockCreateContext(@NotNull Block block, boolean shouldSetType) implements BlockCreateContext {
    public static RWBlockCreateContext create(@NotNull Block block, boolean shouldSetType) {
        return new RWBlockCreateContext(block, shouldSetType);
    }

    public static RWBlockCreateContext create(@NotNull Block block) {
        return new RWBlockCreateContext(block, true);
    }

    @Override
    public @NotNull BlockFace getFacing() {
        return BlockFace.NORTH;
    }

    @Override
    public @NotNull BlockFace getFacingVertical() {
        return BlockFace.NORTH;
    }

    @Override
    public @Nullable ItemStack getItem() {
        return null;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldSetType() {
        return shouldSetType;
    }
}
