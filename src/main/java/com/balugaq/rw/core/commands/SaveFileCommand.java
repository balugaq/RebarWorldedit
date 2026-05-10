package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.BukkitContent;
import com.balugaq.rw.api.Content;
import com.balugaq.rw.api.IRebarWorldEdit;
import com.balugaq.rw.api.RebarContent;
import com.balugaq.rw.utils.PermissionUtil;
import com.balugaq.rw.utils.ReflectionUtil;
import com.balugaq.rw.utils.WorldUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SaveFileCommand extends SubCommand {
    private static final String KEY = "saveFile";
    @NotNull
    private final IRebarWorldEdit plugin;

    public SaveFileCommand(@NotNull IRebarWorldEdit plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public static Map<Integer, String> serializeContent(@NotNull Content content, @NotNull ConfigurationSection c) throws IOException {
        Map<Integer, String> hashBackup = new HashMap<>();
        if (content instanceof BukkitContent bc) {
            BlockState state = bc.getState();
            if (state.getType() == Material.AIR) {
                return hashBackup;
            }

            c.set("type", BukkitContent.getIdentifier());
            c.set("blockData", state.getBlockData().getAsString());

            if (state instanceof Container container) {
                ItemStack[] contents = container.getInventory().getContents();
                Map<Integer, List<Integer>> format = new HashMap<>();
                Map<Integer, Integer> amountMap = new HashMap<>();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack itemStack = contents[i];
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        ItemStack clone = itemStack.clone();
                        int hashCode = clone.hashCode();
                        format.computeIfAbsent(hashCode, k -> new ArrayList<>()).add(i);
                        hashBackup.put(hashCode, getBase64(clone));
                        amountMap.put(i, itemStack.getAmount());
                    }
                }
                c.set("format", format);
                c.set("amountMap", amountMap);
            }
        }

        if (content instanceof RebarContent sc) {
            c.set("type", RebarContent.getIdentifier());
            c.set("id", sc.getId());
        }

        return hashBackup;
    }

    @NotNull
    public static String getBase64(@NotNull Object object) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bs = new BukkitObjectOutputStream(stream);
        bs.writeObject(object);

        bs.close();
        return Base64Coder.encodeLines(stream.toByteArray());
    }

    public static <T> T getObject(@NotNull String base64Str) throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        BukkitObjectInputStream bs = new BukkitObjectInputStream(stream);
        @SuppressWarnings("unchecked") T re = (T) bs.readObject();
        bs.close();
        return re;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PermissionUtil.hasPermission(commandSender, this)) {
            plugin.send(commandSender, "error.no-permission");
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            plugin.send(commandSender, "error.player-only");
            return false;
        }

        final Location pos1 = plugin.getCommandManager().getPos1(player.getUniqueId());
        final Location pos2 = plugin.getCommandManager().getPos2(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.send(commandSender, "error.no-selection");
            return false;
        }

        if (!Objects.equals(pos1.getWorld().getUID(), pos2.getWorld().getUID())) {
            plugin.send(commandSender, "error.world-mismatch");
            return false;
        }

        final long range = WorldUtils.getRange(pos1, pos2);
        final long max = plugin.getConfigManager().getModificationBlockLimit();
        if (range > max) {
            plugin.send(commandSender, "error.too-many-blocks", range, max);
            return false;
        }

        plugin.send(player, "command.savefile.start", WorldUtils.locationToString(pos1), WorldUtils.locationToString(pos2));
        Map<Integer, String> hashBackup = new HashMap<>();
        File file = new File(plugin.getDataFolder() + "/clones/clone" + System.currentTimeMillis() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        try {
            ((DumperOptions) ReflectionUtil.getValue(config, "yamlDumperOptions")).setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        } catch (Throwable ignored) {
        }
        try {
            ((YamlRepresenter) ReflectionUtil.getValue(config, "representer")).setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        } catch (Throwable ignored) {
        }
        ConfigurationSection coroot = config.createSection("content");
        config.set("world", pos1.getWorld().getName());
        WorldUtils.doSimpleWorldEdit(pos1, pos2, (location -> {
            Content content;
            if (WorldUtils.isRebarBlock(location)) {
                content = WorldUtils.getRebarContent(location);
            } else {
                content = WorldUtils.getBukkitContent(location);
            }

            try {
                String key = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
                ConfigurationSection data = new YamlConfiguration();
                Map<Integer, String> hashes = serializeContent(content, data);
                if (data.getKeys(false).isEmpty()) {
                    return;
                }
                coroot.set(key, data);
                hashBackup.putAll(hashes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }), () -> {
            config.set("hashBackup", hashBackup);
            config.set("pos1.world", pos1.getWorld().getName());
            config.set("pos1.x", pos1.getBlockX());
            config.set("pos1.y", pos1.getBlockY());
            config.set("pos1.z", pos1.getBlockZ());
            config.set("pos2.world", pos2.getWorld().getName());
            config.set("pos2.x", pos2.getBlockX());
            config.set("pos2.y", pos2.getBlockY());
            config.set("pos2.z", pos2.getBlockZ());

            try {
                config.save(file);
                plugin.send(player, "command.savefile.success");
            } catch (IOException e) {
                e.printStackTrace();
                plugin.send(player, "error.savefile-failed");
            }
        });

        return true;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    @NotNull
    public String getKey() {
        return KEY;
    }
}
