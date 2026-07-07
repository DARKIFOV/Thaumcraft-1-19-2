package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class TC4FrostShardEntity extends TC4FocusProjectileEntity {
    private boolean fragile;
    private double bounce;
    private int bounceLimit;
    private int frosty;

    public TC4FrostShardEntity(EntityType<? extends TC4FrostShardEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 500;
    }

    public TC4FrostShardEntity(EntityType<? extends TC4FrostShardEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 500;
    }

    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public void setBounce(double bounce, int limit) { this.bounce = bounce; this.bounceLimit = limit; }
    public void setFrosty(int frosty) { this.frosty = frosty; }
    public float getVisualDamage() { return damage; }

    @Override
    public void setDamage(float damage) {
        super.setDamage(damage);
        refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && frosty > 0) {
            float spread = damage / 10.0F;
            for (int i = 0; i < frosty; i++) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        getX() - spread + random.nextFloat() * (spread * 2.0F),
                        getY() - spread + random.nextFloat() * (spread * 2.0F),
                        getZ() - spread + random.nextFloat() * (spread * 2.0F),
                        0.0D, 0.005D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        living.hurt(DamageSource.FREEZE, damage);
        if (frosty > 0) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, frosty - 1));
        }
        if (!level.isClientSide) {
            level.playSound(null, living.blockPosition(), TC4Sounds.event("ice"), SoundSource.NEUTRAL, 0.4F, 1.0F);
        }
    }

    @Override
    protected boolean shouldDiscardAfterEntityHit(LivingEntity living) {
        return fragile || bounceLimit-- <= 0;
    }

    @Override
    protected double tc4Gravity() {
        return fragile ? 0.015D : 0.05D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        if (!level.isClientSide && bounce > 0.0D && bounceLimit-- > 0) {
            net.minecraft.world.phys.Vec3 motion = getDeltaMovement();
            if (hit.getDirection().getAxis() == net.minecraft.core.Direction.Axis.X) motion = new net.minecraft.world.phys.Vec3(-motion.x, motion.y, motion.z);
            if (hit.getDirection().getAxis() == net.minecraft.core.Direction.Axis.Y) motion = new net.minecraft.world.phys.Vec3(motion.x, -motion.y * 0.9D, motion.z);
            if (hit.getDirection().getAxis() == net.minecraft.core.Direction.Axis.Z) motion = new net.minecraft.world.phys.Vec3(motion.x, motion.y, -motion.z);
            setDeltaMovement(motion.scale(0.66D * bounce));
            level.playSound(null, hit.getBlockPos(), TC4Sounds.event("ice"), SoundSource.NEUTRAL, 0.3F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            return;
        }
        if (!level.isClientSide) {
            level.playSound(null, hit.getBlockPos(), TC4Sounds.event("ice"), SoundSource.NEUTRAL, 0.35F, 0.9F + random.nextFloat() * 0.2F);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("fragile", fragile);
        tag.putDouble("bounce", bounce);
        tag.putInt("bounceLimit", bounceLimit);
        tag.putInt("frosty", frosty);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fragile = tag.getBoolean("fragile");
        bounce = tag.getDouble("bounce");
        bounceLimit = tag.getInt("bounceLimit");
        frosty = tag.getInt("frosty");
    }
}
