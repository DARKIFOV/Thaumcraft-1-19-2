package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.entity.PechEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Forge 1.19.2 adaptation of TC4 EntityPechBlast. */
public class TC4PechBlastEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Integer> DATA_STRENGTH = SynchedEntityData.defineId(TC4PechBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EXTEND = SynchedEntityData.defineId(TC4PechBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_NIGHTSHADE = SynchedEntityData.defineId(TC4PechBlastEntity.class, EntityDataSerializers.BOOLEAN);

    public TC4PechBlastEntity(EntityType<? extends TC4PechBlastEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 500;
    }

    public TC4PechBlastEntity(EntityType<? extends TC4PechBlastEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 500;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_STRENGTH, 0);
        entityData.define(DATA_EXTEND, 0);
        entityData.define(DATA_NIGHTSHADE, false);
    }

    public void configure(int strength, int extend, boolean nightshade) {
        entityData.set(DATA_STRENGTH, Math.max(0, strength));
        entityData.set(DATA_EXTEND, Math.max(0, extend));
        entityData.set(DATA_NIGHTSHADE, nightshade);
    }

    public int strength() { return entityData.get(DATA_STRENGTH); }
    public int extend() { return entityData.get(DATA_EXTEND); }
    public boolean nightshade() { return entityData.get(DATA_NIGHTSHADE); }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && !isRemoved()) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(i % 2 == 0 ? ParticleTypes.WITCH : ParticleTypes.ENCHANT,
                        getRandomX(0.4D), getRandomY(), getRandomZ(0.4D),
                        0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    protected double tc4Gravity() { return 0.025D; }

    @Override
    protected void onHit(HitResult hit) {
        if (!level.isClientSide) burst();
        else impactParticles();
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        // handled atomically by onHit
    }

    private void burst() {
        if (isRemoved()) return;
        Entity owner = getOwner();
        AABB area = getBoundingBox().inflate(2.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                living -> living.isAlive() && living != owner && !(living instanceof PechEntity))) {
            target.hurt(DamageSource.thrown(this, owner), strength() + 2.0F);
            applyCurse(target);
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 36, 0.7D, 0.7D, 0.7D, 0.08D);
            server.sendParticles(ParticleTypes.ENCHANT, getX(), getY(), getZ(), 36, 0.7D, 0.7D, 0.7D, 0.08D);
        }
        discard();
    }

    private void applyCurse(LivingEntity target) {
        int duration = 100 + extend() * 40;
        int potency = strength();
        if (nightshade()) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, potency));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, potency + 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, potency));
            return;
        }
        switch (random.nextInt(3)) {
            case 0 -> target.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, potency));
            case 1 -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, potency + 1));
            default -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, potency));
        }
    }

    private void impactParticles() {
        for (int i = 0; i < 27; i++) {
            level.addParticle(i % 3 == 0 ? ParticleTypes.DRAGON_BREATH : ParticleTypes.WITCH,
                    getRandomX(0.6D), getRandomY(), getRandomZ(0.6D),
                    random.nextGaussian() * 0.08D, random.nextGaussian() * 0.08D, random.nextGaussian() * 0.08D);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("strength", strength());
        tag.putInt("duration", extend());
        tag.putBoolean("nightshade", nightshade());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        configure(tag.getInt("strength"), tag.getInt("duration"), tag.getBoolean("nightshade"));
    }
}
