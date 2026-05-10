package com.balugaq.rw.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ItemStackUtil {
    @NotNull
    public static ItemStack toPureItemStack(@NotNull ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        return new ItemStack(itemStack);
    }
}
