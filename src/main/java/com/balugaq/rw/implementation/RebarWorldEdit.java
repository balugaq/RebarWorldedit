package com.balugaq.rw.implementation;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.core.managers.CommandManager;
import com.balugaq.rw.core.managers.ConfigManager;
import com.balugaq.rw.core.managers.DisplayManager;
import com.balugaq.rw.utils.Debug;
import com.balugaq.rw.utils.MinecraftVersion;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
@Getter
public class RebarWorldEdit extends JavaPlugin implements IRebarWorldEdit, Listener {
    private static final MinecraftVersion RECOMMENDED_MC_VERSION = MinecraftVersion.V1_21_11;
    @Nullable
    private static RebarWorldEdit instance;
    @NotNull
    private final String username;
    @NotNull
    private final String repo;
    @NotNull
    private final String branch;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private DisplayManager displayManager;
    private MinecraftVersion minecraftVersion;


    public RebarWorldEdit() {
        this.username = "balugaq";
        this.repo = "RebarWorldedit";
        this.branch = "master";
    }

    @NotNull
    public static MinecraftVersion getMinecraftVersion() {
        return getInstance().minecraftVersion;
    }

    @NotNull
    public static RebarWorldEdit getInstance() {
        Preconditions.checkArgument(instance != null, "RebarWorldedit has not been enabled yet！");
        return RebarWorldEdit.instance;
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

        info("startup.load-config-manager");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.onLoad();

        // Checking environment compatibility
        boolean isCompatible = environmentCheck();

        if (!isCompatible) {
            warning("startup.incompatible");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        info("startup.trying-update");
        tryUpdate();

        info("startup.loading-items");
        new RWSetup(this);

        info("startup.registering-commands");
        this.commandManager = new CommandManager(this);
        this.commandManager.onLoad();

        if (!commandManager.registerCommands()) {
            warning("startup.register-commands-failed");
        }
        
        info("startup.loading-display-manager");
        this.displayManager = new DisplayManager(this);
        this.displayManager.onLoad();

        info("startup.done");
    }

    @Override
    public void onDisable() {
        Preconditions.checkArgument(instance != null, "RebarWorldedit has not been enabled yet！");

        if (this.displayManager != null) {
            this.displayManager.onUnload();
        }
        this.displayManager = null;

        if (this.commandManager != null) {
            this.commandManager.onUnload();
        }
        this.commandManager = null;
        
        info("shutdown.goodbye");

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
            warning("startup.auto-update-failed");
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            warning("startup.unsupported-guizhanlib-version");
            Debug.trace(e);
        }
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public void debug(@NotNull String message) {
        if (getConfigManager().isDebug()) {
            Debug.warning("[DEBUG] " + message);
        }
    }

    @NotNull
    public String getVersion() {
        return getDescription().getVersion();
    }

    private boolean environmentCheck() {
        this.minecraftVersion = MinecraftVersion.current();

        if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            warning("startup.unknown-minecraft-version");
        }

        return minecraftVersion.isAtLeast(RECOMMENDED_MC_VERSION);
    }

    @NotNull
    public NamespacedKey newKey(@NotNull String key) {
        return new NamespacedKey(this, key);
    }

    @Override
    public @NotNull Set<Locale> getLanguages() {
        return Set.of(
                Locale.of("en"),
                Locale.of("zh")
        );
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.DIAMOND_AXE;
    }
}