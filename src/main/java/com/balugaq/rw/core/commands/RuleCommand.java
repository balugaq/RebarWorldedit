package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleCommand extends SubCommand {
    private static final Map<String, String> RULES = new HashMap<>();
    private static final String KEY = "rule";

    static {
        RULES.put("limitBlocks", "worldedit.modification-block-limit");
        RULES.put("limitChunkPerSecond", "worldedit.modification-chunk-limit-per-second");
    }

    @NotNull
    private final IRebarWorldEdit plugin;

    public RuleCommand(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (args.length < 1) {
            plugin.send(commandSender, "error.missing-argument", "rule");
            return false;
        }

        if (args.length < 2) {
            plugin.send(commandSender, "error.missing-argument", "value");
            return false;
        }

        String ruleName = args[0];
        if (!RULES.containsKey(ruleName)) {
            plugin.send(commandSender, "error.invalid-rule");
            return false;
        }

        String ruleKey = RULES.get(ruleName);
        String value = args[1];

        int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            plugin.send(commandSender, "error.invalid-argument", value);
            return false;
        }

        plugin.getConfigManager().setConfig(ruleKey, intValue);
        plugin.send(commandSender, "command.rule.success", ruleName, intValue);

        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            return new ArrayList<>();
        }

        if (args.length <= 1) {
            return new ArrayList<>(RULES.keySet());
        }

        return new ArrayList<>();
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }
}
