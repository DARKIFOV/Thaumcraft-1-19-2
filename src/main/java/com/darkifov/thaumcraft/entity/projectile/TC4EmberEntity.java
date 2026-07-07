package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stage180 strict port shell for original EntityEmber used by ItemFocusFire.
 *
 * Original fields are preserved as damage/firey/duration.  The projectile has
 * zero gravity, slows every tick, burns living targets for 3 + firey seconds and
 * can rarely ignite an adjacent air block when alchemist's fire is present.
 */
public class TC4EmberEntity extends TC4FocusProjectileEntity {
    private int duration = 20;
    private int firey = 0;

    public TC4EmberEntity(EntityType<? extends TC4EmberEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        this.maxLife = duration;
        this.damage = 1.0F;
        setNoGravity(true);
    }

    public TC4EmberEntity(EntityType<? extends TC4EmberEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        this.maxLife = duration;
        this.damage = 1.0F;
        setNoGravity(true);
    }

    public void setDuration(int duration) {
        this.duration = duration;
        this.maxLife = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setFirey(int firey) {
        this.firey = firey;
    }

    public int getFirey() {
        return firey;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void tick() {
        if (life > duration) {
            discard();
            return;
        }
        super.tick();
        double drag = duration <= 20 ? 0.95D : 0.975D;
        setDeltaMovement(getDeltaMovement().scale(this.onGround ? 0.66D : drag));
        if (level.isClientSide) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, getX(), getY(), getZ(), random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D);
        }
    }

    @Override
    protected double tc4Gravity() {
        return 0.0D;
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        if (!living.fireImmune() && living.hurt(DamageSource.ON_FIRE, damage)) {
            living.setSecondsOnFire(3 + firey);
        }
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        if (!level.isClientSide && random.nextFloat() < 0.025F * firey) {
            BlockPos firePos = hit.getBlockPos().relative(hit.getDirection());
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
        }
        if (!level.isClientSide) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", damage);
        tag.putInt("firey", firey);
        tag.putInt("duration", duration);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("damage");
        firey = tag.getInt("firey");
        duration = tag.getInt("duration");
        maxLife = duration;
    }
}
