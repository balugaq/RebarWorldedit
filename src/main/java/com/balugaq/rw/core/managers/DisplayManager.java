package com.balugaq.rw.core.managers;

import com.balugaq.rw.implementation.RebarWorldEdit;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Getter
public class DisplayManager implements IManager {
    private final Map<UUID, DisplayGroup> displays = new HashMap<>();
    private final JavaPlugin plugin;
    private boolean running;

    public DisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static DisplayGroup getDisplayGroup(UUID uuid) {
        return RebarWorldEdit.getInstance().getDisplayManager().getDisplayGroup0(uuid);
    }

    public static void killDisplays(UUID uuid) {
        RebarWorldEdit.getInstance().getDisplayManager().killDisplays0(uuid);
    }

    public static void registerDisplayGroup(UUID uuid, DisplayGroup group) {
        RebarWorldEdit.getInstance().getDisplayManager().registerDisplayGroup0(uuid, group);
    }

    public static void halt() {
        RebarWorldEdit.getInstance().getDisplayManager().halt0();
    }

    public static Map<UUID, DisplayGroup> getDisplayGroups() {
        return RebarWorldEdit.getInstance().getDisplayManager().getDisplayGroups0();
    }

    public void halt0() {
        running = false;
        for (UUID uuid : new HashSet<>(displays.keySet())) {
            killDisplays(uuid);
        }
    }

    public void killDisplays0(UUID uuid) {
        if (running) {
            DisplayGroup group = displays.get(uuid);
            if (group != null) {
                group.remove();
            }
            displays.remove(uuid);
        }
    }

    public void registerDisplayGroup0(UUID uuid, DisplayGroup group) {
        displays.put(uuid, group);
    }

    public DisplayGroup getDisplayGroup0(UUID uuid) {
        return displays.get(uuid);
    }

    @Override
    public void onLoad() {
        running = true;
    }

    @Override
    public void onUnload() {
        halt();
    }

    @NotNull
    public Map<UUID, DisplayGroup> getDisplayGroups0() {
        return displays;
    }
}
