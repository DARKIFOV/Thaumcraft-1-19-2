package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Modern adapter for TC4 4.2.3.5 EntityShockOrb (Earth Shock upgrade). */
public class TC4ShockOrbEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Float> DATA_AREA = SynchedEntityData.defineId(TC4ShockOrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TC4ShockOrbEntity.class, EntityDataSerializers.FLOAT);

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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_AREA, 4.0F);
        entityData.define(DATA_DAMAGE, 5.0F);
    }

    public void setArea(float area) {
        entityData.set(DATA_AREA, Math.max(1.0F, area));
    }

    public float getArea() {
        return entityData.get(DATA_AREA);
    }

    @Override
    public void setDamage(float damage) {
        super.setDamage(damage);
        entityData.set(DATA_DAMAGE, Math.max(0.0F, damage));
    }

    public float getDamage() {
        return entityData.get(DATA_DAMAGE);
    }

    @Override
    public void tick() {
        damage = getDamage();
        super.tick();
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(),
                    random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
        }
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        burst();
    }

    @Override
    protected double tc4Gravity() {
        return 0.05D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        burst();
    }

    private void burst() {
        if (level.isClientSide || isRemoved()) {
            return;
        }

        float radius = getArea();
        double radiusSq = radius * radius;
        for (Entity entity : level.getEntities(this, getBoundingBox().inflate(radius), Entity::isAlive)) {
            if (distanceToSqr(entity) <= radiusSq && canSeeFromOrb(entity)) {
                entity.hurt(DamageSource.thrown(this, getOwner()), getDamage());
            }
        }

        placeElectricGround(radius);
        level.playSound(null, blockPosition(), TC4Sounds.event("shock"), SoundSource.NEUTRAL,
                1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 40,
                    1.5D, 1.5D, 1.5D, 0.08D);
        }
        discard();
    }

    private void placeElectricGround(float radius) {
        int area = Math.max(1, Mth.floor(radius));
        int centerY = Mth.floor(getY());
        for (int i = 0; i < 20; i++) {
            int x = Mth.floor(getX()) + random.nextInt(area) - random.nextInt(area);
            int y = centerY + area;
            int z = Mth.floor(getZ()) + random.nextInt(area) - random.nextInt(area);
            while (level.getBlockState(new BlockPos(x, y, z)).isAir() && y > centerY - area) {
                y--;
            }
            BlockPos ground = new BlockPos(x, y, z);
            BlockPos field = ground.above();
            if (level.getBlockState(field).isAir()
                    && !level.getBlockState(ground).isAir()
                    && !level.getBlockState(field).is(ThaumcraftMod.ELECTRIC_SHOCK.get())
                    && canSeePoint(Vec3.atCenterOf(field))) {
                level.setBlock(field, ThaumcraftMod.ELECTRIC_SHOCK.get().defaultBlockState(), 3);
            }
        }
    }

    private boolean canSeeFromOrb(Entity entity) {
        return canSeePoint(entity.getBoundingBox().getCenter());
    }

    private boolean canSeePoint(Vec3 end) {
        return level.clip(new ClipContext(position(), end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getPickRadius() {
        return 0.1F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        markHurt();
        Vec3 look = attacker.getLookAngle();
        if (look.lengthSqr() > 1.0E-8D) {
            setDeltaMovement(look.scale(0.9D));
            hasImpulse = true;
            level.playSound(null, blockPosition(), TC4Sounds.event("zap"), SoundSource.NEUTRAL,
                    1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("area", getArea());
        tag.putFloat("shockDamage", getDamage());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setArea(tag.contains("area") ? tag.getFloat("area") : 4.0F);
        setDamage(tag.contains("shockDamage") ? tag.getFloat("shockDamage") : damage);
    }
}
