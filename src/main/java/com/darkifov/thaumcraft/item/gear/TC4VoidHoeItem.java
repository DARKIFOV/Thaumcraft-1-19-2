package com.darkifov.thaumcraft.item.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TC4VoidHoeItem extends HoeItem {
    public TC4VoidHoeItem(Properties properties) {
        super(TC4VoidToolTier.INSTANCE, -3, 0.0F, properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        TC4VoidGearRuntime.repairTick(stack, level, entity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        TC4VoidGearRuntime.applyWeakness(target, 80);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TC4VoidGearRuntime.appendTooltip(stack, tooltip, false);
    }
}
