package com.balugaq.rw.api;

import com.balugaq.rw.core.managers.CommandManager;
import com.balugaq.rw.core.managers.ConfigManager;
import com.balugaq.rw.core.managers.DisplayManager;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface IRebarWorldEdit extends RebarAddon, Plugin {
    @NotNull CommandManager getCommandManager();

    @NotNull ConfigManager getConfigManager();

    @Nullable DisplayManager getDisplayManager();

    @NotNull JavaPlugin getJavaPlugin();

    @NotNull String getName();

    @NotNull String getUsername();

    @NotNull String getRepo();

    default Component arguments(@Nullable Locale locale, String translationKey, Object... args) {
        List<RebarArgument> pargs = new ArrayList<>();
        for (int i = 0; i <= args.length / 2; i += 2) {
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

    String MESSAGE_PREFIX = "rebarworldedit.message.";

    default void send(CommandSender sender, String translationKey, Object... args) {
        sender.sendMessage(arguments(Locale.ROOT, MESSAGE_PREFIX + translationKey, args));
    }

    default void send(CommandSender sender, Locale locale, String translationKey, Object... args) {
        sender.sendMessage(arguments(locale, MESSAGE_PREFIX + translationKey, args));
    }

    default void send(Player player, String translationKey, Object... args) {
        player.sendMessage(arguments(player.locale(), MESSAGE_PREFIX + translationKey, args));
    }

    default String translate(String key) {
        Component component = GlobalTranslator.render(Component.translatable(MESSAGE_PREFIX + key), Locale.ROOT);
        if (component instanceof TextComponent tc) {
            return tc.content();
        }

        return component.toString();
    }

    default void info(String key) {
        getLogger().info(translate(key));
    }

    default void warning(String key) {
        getLogger().warning(translate(key));
    }

    default void severe(String key) {
        getLogger().severe(translate(key));
    }

    default void debug(String message) {
        getLogger().info(message);
    }

    @Nullable
    default String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", getUsername(), getRepo());
    }

    default NamespacedKey key(String key) {
        return new NamespacedKey(this, key);
    }
}
