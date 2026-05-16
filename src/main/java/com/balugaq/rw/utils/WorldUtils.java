package com.balugaq.rw.utils;

import com.balugaq.rw.api.ChunkData;
import com.balugaq.rw.api.Facing;
import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.implementation.RebarWorldedit;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.pylonmc.rebar.block.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author m1919810
 * @author balugaq
 */
public class WorldUtils {
    public static final IRebarWorldedit plugin = RebarWorldedit.getInstance();
    @Nullable
    protected static Class<?> craftBlockStateClass;
    @Nullable
    protected static Field interfaceBlockDataField;
    @Nullable
    protected static Field blockPositionField;
    @Nullable
    protected static Field worldField;
    @Nullable
    protected static Field weakWorldField;
    @Nullable
    protected static boolean success = false;

    static {
        try {
            World sampleWorld = Bukkit.getWorlds().get(0);
            BlockState blockstate = sampleWorld.getBlockAt(0, 0, 0).getState();
            var result = ReflectionUtil.getDeclaredFieldsRecursively(blockstate.getClass(), "data");
            interfaceBlockDataField = result.getFirst();
            interfaceBlockDataField.setAccessible(true);
            craftBlockStateClass = result.getSecond();
            blockPositionField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "position").getFirst();
            blockPositionField.setAccessible(true);
            worldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "world").getFirst();
            worldField.setAccessible(true);
            weakWorldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "weakWorld").getFirst();
            weakWorldField.setAccessible(true);
            success = true;
        } catch (Throwable ignored) {

        }
    }

    @CanIgnoreReturnValue
    public static boolean copyBlockState(@NotNull BlockState copy, @NotNull Block toBlock) {
        if (!success) {
            return false;
        }

        BlockState toState = toBlock.getState();
        if (!craftBlockStateClass.isInstance(toState) || !craftBlockStateClass.isInstance(copy)) {
            return false;
        }

        try {
            blockPositionField.set(copy, blockPositionField.get(toState));
            worldField.set(copy, worldField.get(toState));
            weakWorldField.set(copy, weakWorldField.get(toState));
            copy.update(true, false);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @NotNull
    public static String locationToString(@NotNull Location l) {
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    public static long locationRange(@NotNull Location pos1, @NotNull Location pos2) {
        if (pos1 == null || pos2 == null) {
            return 0;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return (long) (Math.abs(upX - downX) + 1) * (Math.abs(upY - downY) + 1) * (Math.abs(upZ - downZ) + 1);
    }

    public static void doWorldEdit(@Nullable Player player, @NotNull Location pos1, @NotNull Location pos2, @NotNull Consumer<Location> consumer) {
        doWorldEdit(player, pos1, pos2, consumer, null);
    }

    public static void doWorldEdit(@Nullable Player player, @NotNull Location pos1, @NotNull Location pos2, @NotNull Consumer<Location> consumer, @Nullable Runnable ending) {
        if (pos1 == null || pos2 == null) {
            return;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        final World world = pos1.getWorld();
        final Map<ChunkData, Set<Location>> chunks = new HashMap<>();

        for (int x = downX; x <= upX; x++) {
            for (int y = downY; y <= upY; y++) {
                for (int z = downZ; z <= upZ; z++) {
                    final ChunkData chunkData = new ChunkData(world, x >> 4, z >> 4);
                    if (chunks.containsKey(chunkData)) {
                        chunks.get(chunkData).add(new Location(world, x, y, z));
                    } else {
                        final Set<Location> locations = new HashSet<>();
                        locations.add(new Location(world, x, y, z));
                        chunks.put(chunkData, locations);
                    }
                }
            }
        }

        final Iterator<ChunkData> iterator = chunks.keySet().iterator();
        final int chunkLimitPerSecond = RebarWorldedit.getInstance().getConfigManager().getModificationChunkPerSecond();
        for (int i = 0; i < chunks.size() && iterator.hasNext(); i += chunkLimitPerSecond) {
            plugin.debug("WorldEdit: processing chunk " + i + "/" + chunks.size());
            Bukkit.getScheduler().runTaskLater(
                    RebarWorldedit.getInstance(), () -> {
                plugin.debug("WorldEdit: processing task...");
                for (int j = 0; j < chunkLimitPerSecond && iterator.hasNext(); j++) {
                    final ChunkData chunkData = iterator.next();
                    final Set<Location> locations = chunks.get(chunkData);
                    for (Location location : locations) {
                        consumer.accept(location);
                    }
                }
            }, 20L * i / chunkLimitPerSecond);
        }

        if (player != null) {
            Bukkit.getScheduler().runTaskLater(
                    RebarWorldedit.getInstance(), () -> {
                if (ending != null) {
                    ending.run();
                }
            }, 20L * chunks.size() / chunkLimitPerSecond);
        }
    }

    public static void doSimpleWorldEdit(@NotNull Location pos1, @NotNull Location pos2, @NotNull Consumer<Location> consumer, @NotNull Runnable ending) {
        if (pos1 == null || pos2 == null) {
            return;
        }

        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        final World world = pos1.getWorld();
        final Map<ChunkData, Set<Location>> chunks = new HashMap<>();

        for (int x = downX; x <= upX; x++) {
            for (int y = downY; y <= upY; y++) {
                for (int z = downZ; z <= upZ; z++) {
                    final ChunkData chunkData = new ChunkData(world, x >> 4, z >> 4);
                    if (chunks.containsKey(chunkData)) {
                        chunks.get(chunkData).add(new Location(world, x, y, z));
                    } else {
                        final Set<Location> locations = new HashSet<>();
                        locations.add(new Location(world, x, y, z));
                        chunks.put(chunkData, locations);
                    }
                }
            }
        }

        final Iterator<ChunkData> iterator = chunks.keySet().iterator();
        final int chunkLimitPerSecond = RebarWorldedit.getInstance().getConfigManager().getModificationChunkPerSecond();
        for (int i = 0; i < chunks.size() && iterator.hasNext(); i += chunkLimitPerSecond) {
            plugin.debug("WorldEdit: processing chunk " + i + "/" + chunks.size());
            Bukkit.getScheduler().runTaskLater(
                    RebarWorldedit.getInstance(), () -> {
                plugin.debug("WorldEdit: processing task...");
                for (int j = 0; j < chunkLimitPerSecond && iterator.hasNext(); j++) {
                    final ChunkData chunkData = iterator.next();
                    final Set<Location> locations = chunks.get(chunkData);
                    for (Location location : locations) {
                        consumer.accept(location);
                    }
                }
            }, 20L * i / chunkLimitPerSecond);
        }

        Bukkit.getScheduler().runTaskLater(RebarWorldedit.getInstance(), ending, 20L * chunks.size() / chunkLimitPerSecond);
    }

    public static long getRange(@NotNull Location pos1, @NotNull Location pos2) {
        if (pos1 == null || pos2 == null) {
            return 0;
        }
        final int downX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        final int upX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        final int downY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        final int upY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        final int downZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        final int upZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return (long) (Math.abs(upX - downX) + 1) * (Math.abs(upY - downY) + 1) * (Math.abs(upZ - downZ) + 1);
    }

    public static boolean isRebarBlock(@NotNull Location location) {
        return BlockStorage.get(location) != null;
    }

    @NotNull
    public static Facing getFacing(float yaw, float pitch) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        if (pitch > 45) {
            return Facing.NY;
        } else if (pitch < -45) {
            return Facing.PY;
        }

        if (yaw >= 315 || yaw < 45) {
            return Facing.PZ;
        } else if (yaw >= 45 && yaw < 135) {
            return Facing.NX;
        } else if (yaw >= 135 && yaw < 225) {
            return Facing.NZ;
        } else {
            return Facing.PX;
        }
    }
}