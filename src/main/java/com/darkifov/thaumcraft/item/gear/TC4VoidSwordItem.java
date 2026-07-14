package com.darkifov.thaumcraft.item.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TC4VoidSwordItem extends SwordItem {
    public TC4VoidSwordItem(Properties properties) {
        super(TC4VoidToolTier.INSTANCE, 3, -2.4F, properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        TC4VoidGearRuntime.repairTick(stack, level, entity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        TC4VoidGearRuntime.applyWeakness(target, 60);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TC4VoidGearRuntime.appendTooltip(stack, tooltip, true);
    }
}
