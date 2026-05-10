package com.balugaq.rw.implementation;

import com.balugaq.rw.api.IRebarWorldEdit;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;

@UtilityClass
public class RWKeys {
    public static final IRebarWorldEdit plugin = RebarWorldEdit.getInstance();
    public static final NamespacedKey WORLDEDITOR = plugin.key("worldeditor");
}
