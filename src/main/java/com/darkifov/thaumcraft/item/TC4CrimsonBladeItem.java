package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Full TC4 Crimson Blade behavior: self repair, tainting debuffs, primal-charm repair and warp 2. */
public final class TC4CrimsonBladeItem extends SwordItem {
    public TC4CrimsonBladeItem(Properties properties) {
        super(TC4CrimsonToolTier.INSTANCE, 3, -2.4F, properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && entity instanceof LivingEntity
                && entity.tickCount % 20 == 0) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isValidRepairItem(ItemStack damaged, ItemStack repair) {
        return TC4CrimsonToolTier.INSTANCE.getRepairIngredient().test(repair)
                || super.isValidRepairItem(damaged, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
