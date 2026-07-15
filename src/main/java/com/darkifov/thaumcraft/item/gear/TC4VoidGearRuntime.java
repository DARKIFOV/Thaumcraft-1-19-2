package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/** Shared TC4 Void-metal behavior: passive repair, weakness on hit and one point of gear warp. */
final class TC4VoidGearRuntime {
    private static final String LAST_REPAIR_TICK = "TC4VoidLastRepairTick";

    private TC4VoidGearRuntime() {
    }

    static void repairTick(ItemStack stack, Level level, Entity holder) {
        if (level.isClientSide || stack.isEmpty() || !stack.isDamaged() || !(holder instanceof LivingEntity)) {
            return;
        }
        if (holder.tickCount % 20 != 0) {
            return;
        }

        long now = level.getGameTime();
        if (stack.getOrCreateTag().getLong(LAST_REPAIR_TICK) == now) {
            return;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        stack.getOrCreateTag().putLong(LAST_REPAIR_TICK, now);
    }

    static void applyWeakness(LivingEntity target, int durationTicks) {
        if (target != null && !target.level.isClientSide) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, durationTicks, 0));
        }
    }

    static void appendTooltip(ItemStack stack, List<Component> tooltip, boolean sapless) {
        tooltip.add(Component.translatable("tc4.void_gear.self_repair").withStyle(ChatFormatting.DARK_AQUA));
        if (sapless) {
            tooltip.add(Component.translatable("enchantment.special.sapless").withStyle(ChatFormatting.GOLD));
        }
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
    }
}
