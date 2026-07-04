package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class IchorPickaxeItem extends PickaxeItem {
    public IchorPickaxeItem(Properties properties) {
        super(IchorToolTier.INSTANCE, 2, -2.6F, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("KAMI-tier pickaxe with high speed and durability.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Tool tier: Ichorium").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
