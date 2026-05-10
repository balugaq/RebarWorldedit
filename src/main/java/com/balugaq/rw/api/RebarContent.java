package com.balugaq.rw.api;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

@Getter
public class RebarContent extends BukkitContent {
    private final NamespacedKey id;

    public RebarContent(@NotNull Location location, @NotNull BlockState state, NamespacedKey id) {
        super(location, state);
        this.id = id;
    }

    public static int getIdentifier() {
        return BukkitContent.getIdentifier() | 4;
    }

    @Override
    public void load() {
        super.load();

        // todo
    }
}
