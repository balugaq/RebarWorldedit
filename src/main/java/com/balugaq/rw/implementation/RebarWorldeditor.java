package com.balugaq.rw.implementation;

import com.balugaq.rw.utils.Constants;
import com.balugaq.rw.utils.WorldUtils;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RebarWorldeditor extends RebarItem implements RebarBlockInteractor {
    public RebarWorldeditor(@NotNull final ItemStack stack) {
        super(stack);
    }

    @Override
    @MultiHandler(priorities = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUsedToClickBlock(@NotNull final PlayerInteractEvent event, @NotNull final EventPriority priority) {
        final Player player = event.getPlayer();
        if (!player.isOp() && !player.hasPermission(Constants.PERMISSION_ADMIN)) {
            RebarWorldEdit.getInstance().send(player, "error.no-permission");
            return;
        }

        Block block = event.getClickedBlock();
        if (event.getAction().isLeftClick()) {
            RebarWorldEdit.getInstance().getCommandManager().setPos1(player.getUniqueId(), block.getLocation());
            final Location pos1 = RebarWorldEdit.getInstance().getCommandManager().getPos1(player.getUniqueId());
            final Location pos2 = RebarWorldEdit.getInstance().getCommandManager().getPos2(player.getUniqueId());
            if (pos2 != null) {
                RebarWorldEdit.getInstance().send(player, "command.setpos1.success-with-range", WorldUtils.locationToString(pos1), WorldUtils.locationRange(pos1, pos2));
            } else {
                RebarWorldEdit.getInstance().send(player, "command.setpos1.success", WorldUtils.locationToString(pos1));
            }
        }

        if (event.getAction().isRightClick()) {
            final Location pos1 = RebarWorldEdit.getInstance().getCommandManager().getPos1(player.getUniqueId());
            final Location pos2 = RebarWorldEdit.getInstance().getCommandManager().getPos2(player.getUniqueId());
            if (pos1 != null) {
                RebarWorldEdit.getInstance().send(player, "command.setpos2.success-with-range", WorldUtils.locationToString(pos1), WorldUtils.locationRange(pos1, pos2));
            } else {
                RebarWorldEdit.getInstance().send(player, "command.setpos2.success", WorldUtils.locationToString(pos2));
            }
        }
    }
}
