package com.balugaq.rw.api;

import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public record RWBlockBreakContext(Block block) implements BlockBreakContext {
    public static RWBlockBreakContext create(Block block) {
        return new RWBlockBreakContext(block);
    }

    public static RWBlockBreakContext create(Location location) {
        return new RWBlockBreakContext(location.getBlock());
    }

    @Override
    public boolean normallyDrops() {
        return false;
    }

    @Override
    public boolean shouldSetToAir() {
        return false;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }
}
