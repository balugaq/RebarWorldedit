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
            RebarWorldedit.getInstance().send(player, "error.no-permission");
            return;
        }

        Block block = event.getClickedBlock();
        if (event.getAction().isLeftClick()) {
            RebarWorldedit.getInstance().getCommandManager().setPos1(player.getUniqueId(), block.getLocation());
            final Location pos1 = RebarWorldedit.getInstance().getCommandManager().getPos1(player.getUniqueId());
            final Location pos2 = RebarWorldedit.getInstance().getCommandManager().getPos2(player.getUniqueId());
            if (pos2 != null) {
                RebarWorldedit.getInstance().send(player, "command.setpos1.success-with-range", "pos", WorldUtils.locationToString(pos1), "range", WorldUtils.locationRange(pos1, pos2));
            } else {
                RebarWorldedit.getInstance().send(player, "command.setpos1.success", "pos", WorldUtils.locationToString(pos1));
            }
        }

        if (event.getAction().isRightClick()) {
            RebarWorldedit.getInstance().getCommandManager().setPos2(player.getUniqueId(), block.getLocation());
            final Location pos1 = RebarWorldedit.getInstance().getCommandManager().getPos1(player.getUniqueId());
            final Location pos2 = RebarWorldedit.getInstance().getCommandManager().getPos2(player.getUniqueId());
            if (pos1 != null) {
                RebarWorldedit.getInstance().send(player, "command.setpos2.success-with-range", "pos", WorldUtils.locationToString(pos1), "range", WorldUtils.locationRange(pos1, pos2));
            } else {
                RebarWorldedit.getInstance().send(player, "command.setpos2.success", "pos", WorldUtils.locationToString(pos2));
            }
        }

        event.setCancelled(true);
    }
}
