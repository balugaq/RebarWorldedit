package com.balugaq.rw.api;

import com.balugaq.rw.core.managers.CommandManager;
import com.balugaq.rw.core.managers.ConfigManager;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface IRebarWorldedit extends RebarAddon, Plugin {
    @NotNull CommandManager getCommandManager();

    @NotNull ConfigManager getConfigManager();

    @NotNull JavaPlugin getJavaPlugin();

    @NotNull String getName();

    @NotNull String getUsername();

    @NotNull String getRepo();

    @NotNull LifecycleEventManager<@NotNull Plugin> getLifecycleManager();

    default Component arguments(@Nullable Locale locale, @NotNull String translationKey, @Nullable Object @NotNull ... args) {
        List<RebarArgument> pargs = new ArrayList<>();
        for (int i = 0; i < args.length / 2; i += 1) {
            if (args[i] == null || args[i + 1] == null) {
                continue;
            }
            String argkey = args[i].toString();
            Object object = args[i + 1];
            switch (object) {
                case ComponentLike componentLike -> pargs.add(RebarArgument.of(argkey, componentLike));
                case String string -> pargs.add(RebarArgument.of(argkey, string));
                case Integer integer -> pargs.add(RebarArgument.of(argkey, integer));
                case Long longValue -> pargs.add(RebarArgument.of(argkey, longValue));
                case Double doubleValue -> pargs.add(RebarArgument.of(argkey, doubleValue));
                case Float floatValue -> pargs.add(RebarArgument.of(argkey, floatValue));
                case Boolean booleanValue -> pargs.add(RebarArgument.of(argkey, booleanValue));
                case Character character -> pargs.add(RebarArgument.of(argkey, character));
                case Number number -> pargs.add(RebarArgument.of(argkey, number.intValue()));
                default -> {
                }
            }
        }
        if (locale != null) {
            return GlobalTranslator.render(Component.translatable(translationKey, pargs), locale);
        } else {
            return Component.translatable(translationKey, pargs);
        }
    }

    String MESSAGE_PREFIX = "rebarworldedit.messages.";

    default void send(CommandSender sender, String translationKey, Object... args) {
        sender.sendMessage(arguments(Locale.of("en"), MESSAGE_PREFIX + translationKey, args));
    }

    default void send(CommandSender sender, Locale locale, String translationKey, Object... args) {
        sender.sendMessage(arguments(locale, MESSAGE_PREFIX + translationKey, args));
    }

    default void send(Player player, String translationKey, Object... args) {
        player.sendMessage(arguments(player.locale(), MESSAGE_PREFIX + translationKey, args));
    }

    default @NotNull Component translate(@NotNull String key) {
        return GlobalTranslator.render(Component.translatable(MESSAGE_PREFIX + key), Locale.of("en"));
    }

    default void info(@NotNull String key) {
        log(0xFFFFFF , "[RebarWorldedit] [INFO] ", key);
    }

    default void warning(@NotNull String key) {
        log(0xFFFF55, "[RebarWorldedit] [WARNING] ", key);
    }

    default void severe(@NotNull String key) {
        log(0xFF5555, "[RebarWorldedit] [SEVERE] ", key);
    }

    default void debug(@NotNull String message) {
        if (getConfigManager().isDebug()) {
            logDebug(0x55FFFF, "[RebarWorldedit] [DEBUG] ", message);
        }
    }

    default void trace(@NotNull Throwable e) {
        e.printStackTrace();
    }

    default void log(int color, @NotNull String prefix, @NotNull String key) {
        Bukkit.getConsoleSender().sendMessage(Component.text().color(TextColor.color(color)).append(Component.text(prefix).append(translate(key))));
    }

    default void logDebug(int color, @NotNull String prefix, @NotNull String message) {
        Bukkit.getConsoleSender().sendMessage(Component.text().color(TextColor.color(color)).append(Component.text(prefix).append(Component.text(message))));
    }

    @Nullable
    default String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", getUsername(), getRepo());
    }

    default @NotNull NamespacedKey key(String key) {
        return new NamespacedKey(this, key);
    }

    @Nullable PluginCommand getCommand(String name);

    default @Nullable Component get(@NotNull String key) {
        return arguments(null, key);
    }
}
