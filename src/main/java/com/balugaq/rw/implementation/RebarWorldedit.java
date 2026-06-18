package com.balugaq.rw.implementation;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.core.managers.CommandManager;
import com.balugaq.rw.core.managers.ConfigManager;
import com.balugaq.rw.utils.MinecraftVersion;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
@Getter
public class RebarWorldedit extends JavaPlugin implements IRebarWorldedit, Listener {
    private static final MinecraftVersion RECOMMENDED_MC_VERSION = MinecraftVersion.V1_21_11;
    @Nullable
    private static RebarWorldedit instance;
    @NotNull
    private final String username;
    @NotNull
    private final String repo;
    @NotNull
    private final String branch;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private MinecraftVersion minecraftVersion;


    public RebarWorldedit() {
        this.username = "balugaq";
        this.repo = "RebarWorldedit";
        this.branch = "master";
    }

    @NotNull
    public static MinecraftVersion getMinecraftVersion() {
        return getInstance().minecraftVersion;
    }

    @NotNull
    public static RebarWorldedit getInstance() {
        Preconditions.checkArgument(instance != null, "RebarWorldedit has not been enabled yet！");
        return RebarWorldedit.instance;
    }

    @Override
    @NotNull
    public ConfigManager getConfigManager() {
        return getInstance().configManager;
    }

    @Override
    @NotNull
    public CommandManager getCommandManager() {
        return getInstance().commandManager;
    }

    @Override
    public void onEnable() {
        Preconditions.checkArgument(instance == null, "RebarWorldedit already has been enabled！");
        instance = this;
        registerWithRebar();

        info("Loading config manager");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.onLoad();

        // Checking environment compatibility
        boolean isCompatible = environmentCheck();

        if (!isCompatible) {
            warning("Incompatible environment! The plugin has been disabled!");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        info("Attempting to update automatically...");
        tryUpdate();

        info("Loading items...");
        new RWSetup(this);

        info("Registering commands...");
        this.commandManager = new CommandManager(this);
        this.commandManager.onLoad();

        if (!commandManager.registerCommands()) {
            warning("Failed to register commands!");
        }

        info("Successfully enabled this addon");
    }

    @Override
    public void onDisable() {
        Preconditions.checkArgument(instance != null, "RebarWorldedit has not been enabled yet！");

        if (this.commandManager != null) {
            this.commandManager.onUnload();
        }
        this.commandManager = null;
        
        info("Successfully disabled this addon");

        this.minecraftVersion = null;

        if (this.configManager != null) {
            this.configManager.onUnload();
        }
        this.configManager = null;
        
        instance = null;
    }

    public void tryUpdate() {
        try {
            if (configManager.isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                // TODO
            }
        } catch (NoClassDefFoundError | NullPointerException e) {
            warning("Automatic update failed!");
            trace(e);
        } catch (UnsupportedOperationException e) {
            warning("Unable to compatible with GuizhanPluginLib, automatic update failed!");
            trace(e);
        }
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @NotNull
    public String getVersion() {
        return getDescription().getVersion();
    }

    private boolean environmentCheck() {
        this.minecraftVersion = MinecraftVersion.current();

        if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            warning("Unable to recognize Minecraft version! Use this plugin with caution!");
        }

        return minecraftVersion.isAtLeast(RECOMMENDED_MC_VERSION);
    }

    @Override
    public @NotNull Set<Locale> getLanguages() {
        return Set.of(
                Locale.of("en")
        );
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.DIAMOND_AXE;
    }
}