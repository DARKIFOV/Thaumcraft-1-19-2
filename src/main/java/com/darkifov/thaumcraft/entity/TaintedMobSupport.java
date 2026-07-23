package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Shared source-level contracts for TC4's converted taint animals. */
public final class TaintedMobSupport {
    private TaintedMobSupport() {}

    public static ItemStack randomTaintResource(RandomSource random) {
        return random.nextBoolean()
                ? new ItemStack(ThaumcraftMod.TAINTED_SLIME.get())
                : new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_taint_tendril").get());
    }

    /** TC4 emitted a short sploosh burst during the first five entity ticks. */
    public static void tickBirthParticles(LivingEntity entity) {
        if (!entity.level.isClientSide || entity.tickCount >= 5) return;
        for (int i = 0; i < 10; i++) {
            entity.level.addParticle(ParticleTypes.WITCH,
                    entity.getRandomX(0.8D),
                    entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight(),
                    entity.getRandomZ(0.8D),
                    (entity.getRandom().nextDouble() - 0.5D) * 0.04D,
                    entity.getRandom().nextDouble() * 0.04D,
                    (entity.getRandom().nextDouble() - 0.5D) * 0.04D);
        }
    }
}
