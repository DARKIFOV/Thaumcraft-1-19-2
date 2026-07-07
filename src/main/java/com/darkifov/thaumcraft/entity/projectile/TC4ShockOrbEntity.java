package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class TC4ShockOrbEntity extends TC4FocusProjectileEntity {
    private float area = 4.0F;

    public TC4ShockOrbEntity(EntityType<? extends TC4ShockOrbEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        damage = 5.0F;
        maxLife = 500;
    }

    public TC4ShockOrbEntity(EntityType<? extends TC4ShockOrbEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        damage = 5.0F;
        maxLife = 500;
    }

    public void setArea(float area) { this.area = area; }
    public float getArea() { return area; }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
        }
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        burst(living);
    }

    @Override
    protected double tc4Gravity() {
        return 0.05D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        burst(null);
        if (!level.isClientSide) {
            discard();
        }
    }

    private void burst(LivingEntity direct) {
        if (level.isClientSide) {
            return;
        }
        for (Entity entity : level.getEntities(this, getBoundingBox().inflate(area), e -> e != getOwner() && e.isAlive())) {
            if (canSeeFromOrb(entity)) {
                entity.hurt(DamageSource.LIGHTNING_BOLT, damage);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                }
            }
        }
        level.playSound(null, blockPosition(), TC4Sounds.event("shock"), SoundSource.NEUTRAL, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 24, area * 0.25D, area * 0.15D, area * 0.25D, 0.05D);
        }
    }

    private boolean canSeeFromOrb(Entity entity) {
        Vec3 start = position();
        Vec3 end = entity.getBoundingBox().getCenter();
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putFloat("area", area); }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); area = tag.getFloat("area"); }
}
