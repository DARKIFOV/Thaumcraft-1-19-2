package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class TC4ExplosiveOrbEntity extends TC4FocusProjectileEntity {
    private float strength = 1.0F;
    private boolean alchemistsFire;

    public TC4ExplosiveOrbEntity(EntityType<? extends TC4ExplosiveOrbEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 500;
    }

    public TC4ExplosiveOrbEntity(EntityType<? extends TC4ExplosiveOrbEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 500;
    }

    public void setStrength(float strength) { this.strength = strength; }
    public float getStrength() { return strength; }
    public void setAlchemistsFire(boolean alchemistsFire) { this.alchemistsFire = alchemistsFire; }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D);
        }
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        if (alchemistsFire) living.setSecondsOnFire(6);
        living.hurt(alchemistsFire ? DamageSource.ON_FIRE : DamageSource.MAGIC, strength * 1.5F);
        explode();
    }

    @Override
    protected double tc4Gravity() {
        return 0.01D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) { explode(); }

    private void explode() {
        if (!level.isClientSide) {
            level.explode(this, getX(), getY(), getZ(), strength, alchemistsFire, Explosion.BlockInteraction.NONE);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putFloat("strength", strength); tag.putBoolean("alchemistsFire", alchemistsFire); }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); strength = tag.getFloat("strength"); alchemistsFire = tag.getBoolean("alchemistsFire"); }
}
