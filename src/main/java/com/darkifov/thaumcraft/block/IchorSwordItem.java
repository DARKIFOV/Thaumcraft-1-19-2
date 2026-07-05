package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class IchorSwordItem extends SwordItem {
    public IchorSwordItem(Properties properties) {
        super(IchorToolTier.INSTANCE, 6, -2.1F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(4);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("KAMI-tier sword. Ignites enemies on hit.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Tool tier: Ichorium").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
