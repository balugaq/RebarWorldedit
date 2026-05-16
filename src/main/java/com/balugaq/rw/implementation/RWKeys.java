package com.balugaq.rw.implementation;

import com.balugaq.rw.api.IRebarWorldedit;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;

@UtilityClass
public class RWKeys {
    public static final IRebarWorldedit plugin = RebarWorldedit.getInstance();
    public static final NamespacedKey WORLDEDITOR = plugin.key("worldeditor");
}
