package com.balugaq.rw.core.commands;

import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommand implements TabExecutor {
    @NotNull
    public abstract String getKey();
}
