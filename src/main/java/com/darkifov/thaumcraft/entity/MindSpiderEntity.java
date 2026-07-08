package com.darkifov.thaumcraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Stage273-282 direct 1.19.2 port of TC4 EntityMindSpider. */
public class MindSpiderEntity extends Spider {
    private static final EntityDataAccessor<Byte> HARMLESS = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.STRING);
    private int lifeSpan = Integer.MAX_VALUE;

    public MindSpiderEntity(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HARMLESS, (byte)0);
        entityData.define(VIEWER, "");
    }

    public String getViewer() { return entityData.get(VIEWER); }

    public void setViewer(String player) { entityData.set(VIEWER, player == null ? "" : player); }

    public boolean isHarmless() { return entityData.get(HARMLESS) != 0; }

    public void setHarmless(boolean harmless) {
        if (harmless) lifeSpan = 1200;
        entityData.set(HARMLESS, (byte)(harmless ? 1 : 0));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && tickCount > lifeSpan) {
            discard();
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        return !isHarmless() && super.doHurtTarget(target);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isHarmless() && super.canAttack(target);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected int getExperienceReward() {
        return isHarmless() ? 0 : super.getExperienceReward();
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return isHarmless() ? 0.0F : 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // Original EntityMindSpider returns item id 0 and has an empty drop method.
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("harmless", entityData.get(HARMLESS));
        tag.putString("viewer", entityData.get(VIEWER));
        tag.putInt("lifeSpan", lifeSpan);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(HARMLESS, tag.getByte("harmless"));
        entityData.set(VIEWER, tag.getString("viewer"));
        lifeSpan = tag.contains("lifeSpan") ? tag.getInt("lifeSpan") : (isHarmless() ? 1200 : Integer.MAX_VALUE);
    }
}
