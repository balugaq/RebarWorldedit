package com.balugaq.rw.implementation;

import com.balugaq.rw.api.IRebarWorldEdit;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RWSetup {
    public RWSetup(IRebarWorldEdit plugin) {
        SimpleStaticGuidePage mainPage = new SimpleStaticGuidePage(plugin.key("main"));
        RebarGuide.getRootPage().addPage(Material.DIAMOND_AXE, mainPage);
        ItemStack stack = ItemStackBuilder.rebar(Material.DIAMOND_AXE, RWKeys.WORLDEDITOR).build();
        RebarItem.register(RebarWorldeditor.class, stack);
        mainPage.addItem(stack);
    }
}
