package com.balugaq.rw.api;

import com.balugaq.rw.utils.WorldUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class BukkitContent extends Content {
    @Nullable
    private BlockState state;

    public BukkitContent(@NotNull Location location, @NotNull BlockState state) {
        super(location);
        try {
            this.state = state.copy(location);
        } catch (NoSuchMethodError e) {
            try {
                this.state = state.copy();
            } catch (NoSuchMethodError e1) {
                this.state = state;
            }
        }
    }

    public static int getIdentifier() {
        return 3;
    }

    @Override
    public void load() {
        if (state != null) {
            WorldUtils.copyBlockState(state, this.getLocation().getBlock());
        }

        BlockStorage.breakBlock(getLocation());
    }
}
