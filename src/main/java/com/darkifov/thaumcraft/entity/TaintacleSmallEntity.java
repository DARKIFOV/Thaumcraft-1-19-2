package com.darkifov.thaumcraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Stage303-322 direct adapter for TC4 EntityTaintacleSmall. */
public class TaintacleSmallEntity extends TaintacleEntity {
    public static final String LIFETIME_TAG = "lifetime";
    private int lifetime = 200;

    public TaintacleSmallEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && lifetime-- <= 0) {
            hurt(DamageSource.STARVE, 10.0F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // Original small taintacle has no drops.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(LIFETIME_TAG, lifetime);
        tag.putString("TC4Original", "EntityTaintacleSmall");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifetime = tag.contains(LIFETIME_TAG) ? tag.getInt(LIFETIME_TAG) : 200;
    }
}
