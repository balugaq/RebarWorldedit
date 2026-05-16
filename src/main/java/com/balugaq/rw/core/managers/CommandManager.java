package com.balugaq.rw.core.managers;

import com.balugaq.rw.api.CachedRequest;
import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.core.commands.RebarWorldeditCommand;
import com.balugaq.rw.utils.ParticleUtil;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import kotlin.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class CommandManager implements IManager {
    private final Map<String, Queue<CachedRequest>> cachedRequests = new HashMap<>();
    private final Map<UUID, Pair<Location, Location>> selection = new HashMap<>();
    private IRebarWorldedit plugin;

    public CommandManager(@NotNull IRebarWorldedit plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(RebarWorldeditCommand.ROOT);
            event.registrar().register(RebarWorldeditCommand.ROOT_ALIAS);
        });
        return true;
    }

    public void clearSelection(@NotNull UUID player) {
        selection.remove(player);
    }

    public void setPos1(@NotNull UUID player, @NotNull Location pos1) {
        selection.put(player, new Pair<>(pos1, getPos2(player)));
    }

    public void setPos2(@NotNull UUID player, @NotNull Location pos2) {
        selection.put(player, new Pair<>(getPos1(player), pos2));
    }

    @NotNull
    public List<String> getSubCommands() {
        return new ArrayList<>();
    }

    @Nullable
    public Location getPos1(@NotNull UUID player) {
        Pair<Location, Location> pair = selection.get(player);
        if (pair == null) {
            return null;
        }
        return selection.get(player).getFirst();
    }

    @Nullable
    public Location getPos2(@NotNull UUID player) {
        Pair<Location, Location> pair = selection.get(player);
        if (pair == null) {
            return null;
        }
        return selection.get(player).getSecond();
    }

    public void addCachedRequest(@NotNull CommandSender sender, @NotNull CachedRequest request) {
        String key = sender.getName();
        if (!cachedRequests.containsKey(key)) {
            cachedRequests.put(key, new LinkedList<>());
        }

        cachedRequests.get(key).add(request);
    }

    @Nullable
    public CachedRequest pullCachedRequest(@NotNull CommandSender sender) {
        if (cachedRequests.isEmpty()) {
            return null;
        }

        String key = sender.getName();
        if (!cachedRequests.containsKey(key)) {
            return null;
        }

        return cachedRequests.get(key).poll();
    }

    public void runParticleTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : selection.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }

                Location pos1 = getPos1(uuid);
                Location pos2 = getPos2(uuid);
                if (pos1 == null || pos2 == null) {
                    return;
                }

                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> ParticleUtil.drawRegionOutline(plugin, Particle.WAX_OFF, 0, pos1, pos2), RebarConfig.DEFAULT_TICK_INTERVAL);
            }
        }, 0, RebarConfig.DEFAULT_TICK_INTERVAL);
    }

    @Override
    public void onLoad() {
        runParticleTask();
    }

    @Override
    public void onUnload() {
        cachedRequests.clear();
        selection.clear();
        this.plugin = null;
    }
}
