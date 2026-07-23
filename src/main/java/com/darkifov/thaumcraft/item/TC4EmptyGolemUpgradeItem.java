package com.darkifov.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Passive TC4 crafting blank used as the base for all six elemental golem upgrades. */
public class TC4EmptyGolemUpgradeItem extends Item {
    public TC4EmptyGolemUpgradeItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.tc4_golem_upgrade_empty.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
