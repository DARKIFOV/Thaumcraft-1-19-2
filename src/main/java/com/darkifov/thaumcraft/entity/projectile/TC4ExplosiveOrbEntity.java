package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Exact modern adapter for TC4 EntityExplosiveOrb (Focus Fireball). */
public class TC4ExplosiveOrbEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Float> DATA_STRENGTH = SynchedEntityData.defineId(TC4ExplosiveOrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ALCHEMISTS_FIRE = SynchedEntityData.defineId(TC4ExplosiveOrbEntity.class, EntityDataSerializers.BOOLEAN);

    public TC4ExplosiveOrbEntity(EntityType<? extends TC4ExplosiveOrbEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 500;
    }

    public TC4ExplosiveOrbEntity(EntityType<? extends TC4ExplosiveOrbEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 500;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_STRENGTH, 1.0F);
        entityData.define(DATA_ALCHEMISTS_FIRE, false);
    }

    public void setStrength(float strength) { entityData.set(DATA_STRENGTH, Math.max(0.0F, strength)); }
    public float getStrength() { return entityData.get(DATA_STRENGTH); }
    public void setAlchemistsFire(boolean value) { entityData.set(DATA_ALCHEMISTS_FIRE, value); }
    public boolean isAlchemistsFire() { return entityData.get(DATA_ALCHEMISTS_FIRE); }

    @Override
    protected void onHitLiving(LivingEntity living) {
        if (!level.isClientSide) {
            LivingEntity owner = getOwner() instanceof LivingEntity livingOwner ? livingOwner : null;
            DamageSource source = owner == null ? DamageSource.indirectMagic(this, this) : DamageSource.indirectMagic(this, owner);
            living.hurt(source, getStrength() * 1.5F);
        }
        explode();
    }

    @Override
    protected double tc4Gravity() { return 0.01D; }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) { explode(); }

    private void explode() {
        if (!level.isClientSide) {
            level.explode(this, getX(), getY(), getZ(), getStrength(), isAlchemistsFire(), Explosion.BlockInteraction.NONE);
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        markHurt();
        Entity attacker = source.getEntity();
        if (attacker == null) return false;
        Vec3 look = attacker.getLookAngle();
        if (look == null) return false;
        setDeltaMovement(look.scale(0.9D));
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("strength", getStrength());
        tag.putBoolean("alchemistsFire", isAlchemistsFire());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setStrength(tag.contains("strength") ? tag.getFloat("strength") : 1.0F);
        setAlchemistsFire(tag.getBoolean("alchemistsFire"));
    }
}
