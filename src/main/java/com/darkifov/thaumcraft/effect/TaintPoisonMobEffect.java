package com.darkifov.thaumcraft.effect;

import com.darkifov.thaumcraft.entity.TaintedMob;
import com.darkifov.thaumcraft.damage.TC4DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Direct Forge 1.19.2 adaptation of TC4 PotionFluxTaint.
 *
 * <p>Every 40 >> amplifier ticks it heals ITaintedMob implementations by one
 * point and deals one point of taint/magic damage to non-undead victims.</p>
 */
public final class TaintPoisonMobEffect extends MobEffect {
    public TaintPoisonMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x663377);
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level.isClientSide) return;
        if (target instanceof TaintedMob) {
            target.heal(1.0F);
        } else if (!target.isInvertedHealAndHarm()) {
            target.hurt(TC4DamageSources.TAINT, 1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(1, 40 >> Math.max(0, amplifier));
        return duration % interval == 0;
    }
}
