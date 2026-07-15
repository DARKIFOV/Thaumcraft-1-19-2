package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/** Legacy registry id backed by TC4's real EntityTaintSpider behavior. */
public class TaintCrawlerEntity extends Spider {
    public TaintCrawlerEntity(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose,
                                                     net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.1F;
    }

    @Override public float getVoicePitch() { return 0.7F; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.SPIDER_AMBIENT; }

    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (random.nextInt(6) == 0) spawnAtLocation(ThaumcraftMod.TAINTED_SLIME.get());
    }
}
