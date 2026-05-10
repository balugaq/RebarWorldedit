package com.balugaq.rw.core.managers;

import com.balugaq.rw.api.Backup;
import com.balugaq.rw.api.CachedRequest;
import com.balugaq.rw.api.Content;
import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.core.commands.ClearCommand;
import com.balugaq.rw.core.commands.ClearPosCommand;
import com.balugaq.rw.core.commands.ClearProjectileCommand;
import com.balugaq.rw.core.commands.CloneCommand;
import com.balugaq.rw.core.commands.ConfirmCommand;
import com.balugaq.rw.core.commands.HelpCommand;
import com.balugaq.rw.core.commands.LoadFileCommand;
import com.balugaq.rw.core.commands.MoveCommand;
import com.balugaq.rw.core.commands.PasteCommand;
import com.balugaq.rw.core.commands.ReloadCommand;
import com.balugaq.rw.core.commands.RuleCommand;
import com.balugaq.rw.core.commands.SaveFileCommand;
import com.balugaq.rw.core.commands.SetPos1Command;
import com.balugaq.rw.core.commands.SetPos2Command;
import com.balugaq.rw.core.commands.SubCommand;
import com.balugaq.rw.core.commands.UndoCommand;
import com.balugaq.rw.core.commands.VersionCommand;
import com.balugaq.rw.utils.ParticleUtil;
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
import java.util.function.Consumer;

public class CommandManager implements IManager {
    private final Map<UUID, Backup> actions = new HashMap<>();
    private final Map<String, Queue<CachedRequest>> cachedRequests = new HashMap<>();
    private final Map<UUID, Pair<Location, Location>> selection = new HashMap<>();
    private final List<SubCommand> commands = new ArrayList<>();
    @Nullable
    private IRebarWorldEdit plugin;

    public CommandManager(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommands() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(command);
        }
        return true;
    }

    public void iter(@NotNull Consumer<SubCommand> consumer) {
        commands.forEach(consumer);
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

                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> ParticleUtil.drawRegionOutline(plugin, Particle.WAX_OFF, 0, pos1, pos2), Slimefun.getTickerTask().getTickRate());
            }
        }, 0, Slimefun.getTickerTask().getTickRate());
    }

    @Override
    public void onLoad() {
        commands.add(new HelpCommand(plugin));
        commands.add(new SetPos1Command(plugin));
        commands.add(new SetPos2Command(plugin));
        commands.add(new ClearPosCommand(plugin));
        commands.add(new CloneCommand(plugin));
        commands.add(new PasteCommand(plugin));
        commands.add(new ClearCommand(plugin));
        commands.add(new ReloadCommand(plugin));
        commands.add(new VersionCommand(plugin));
        if (plugin.getConfigManager().isAllowUndo()) {
            commands.add(new UndoCommand(plugin));
        }
        commands.add(new RuleCommand(plugin));
        commands.add(new ConfirmCommand(plugin));
        commands.add(new SaveFileCommand(plugin));
        commands.add(new LoadFileCommand(plugin));
        commands.add(new MoveCommand(plugin));
        commands.add(new ClearProjectileCommand(plugin));
        runParticleTask();
    }

    @Override
    public void onUnload() {
        for (SubCommand command : commands) {
            plugin.getCommand("sfw" + command.getKey()).setExecutor(null);
        }
        cachedRequests.clear();
        selection.clear();
        commands.clear();
        this.plugin = null;
    }

    public void initBackup(UUID playerUUID) {
        if (actions.containsKey(playerUUID)) {
            return;
        }
        actions.put(playerUUID, new Backup(playerUUID));
    }

    public void addBackup(@NotNull UUID playerUUID, @NotNull List<Content> backup) {
        initBackup(playerUUID);
        actions.get(playerUUID).addContent(backup);
    }

    @NotNull
    public List<Content> leftBackup(@NotNull UUID playerUUID) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getLeftContentAndDecreasePointer(new ArrayList<>());
    }

    @NotNull
    public List<Content> rightBackup(@NotNull UUID playerUUID) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getRightContentAndIncreasePointer(new ArrayList<>());
    }

    @NotNull
    public List<Content> getBackup(@NotNull UUID playerUUID, int pointer) {
        initBackup(playerUUID);
        return actions.get(playerUUID).getContent(pointer);
    }


    public void clearBackup(@NotNull UUID playerUUID) {
        initBackup(playerUUID);
        actions.get(playerUUID).clear();
    }
}
