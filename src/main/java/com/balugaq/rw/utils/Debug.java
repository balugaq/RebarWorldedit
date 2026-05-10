package com.balugaq.rw.utils;

import com.balugaq.rw.implementation.RebarWorldEdit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Debug {
    public static void info(String message) {
        RebarWorldEdit.getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        RebarWorldEdit.getInstance().getLogger().warning(message);
    }

    public static void warn(String message) {
        RebarWorldEdit.getInstance().getLogger().warning(message);
    }

    public static void severe(String message) {
        RebarWorldEdit.getInstance().getLogger().severe(message);
    }

    public static void error(String message) {
        RebarWorldEdit.getInstance().getLogger().severe(message);
    }

    public static void debug(@NotNull String message) {
        RebarWorldEdit.getInstance().debug(message);
    }

    public static void debug(@Nullable Object object) {
        debug(object == null ? "null" : object.toString());
    }

    public static void trace(@NotNull Throwable e) {
        e.printStackTrace();
    }
}
