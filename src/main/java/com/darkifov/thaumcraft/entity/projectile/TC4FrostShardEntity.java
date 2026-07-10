package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Modern 1.19.2 adapter for TC4 4.2.3.5 EntityFrostShard. */
public class TC4FrostShardEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TC4FrostShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FROSTY = SynchedEntityData.defineId(TC4FrostShardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FRAGILE = SynchedEntityData.defineId(TC4FrostShardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_BOUNCE = SynchedEntityData.defineId(TC4FrostShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_BOUNCE_LIMIT = SynchedEntityData.defineId(TC4FrostShardEntity.class, EntityDataSerializers.INT);

    public TC4FrostShardEntity(EntityType<? extends TC4FrostShardEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        // EntityThrowable had no short artificial lifetime in the original.
        maxLife = Integer.MAX_VALUE;
        refreshDimensions();
    }

    public TC4FrostShardEntity(EntityType<? extends TC4FrostShardEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = Integer.MAX_VALUE;
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_DAMAGE, 3.0F);
        entityData.define(DATA_FROSTY, 0);
        entityData.define(DATA_FRAGILE, false);
        entityData.define(DATA_BOUNCE, 0.5F);
        entityData.define(DATA_BOUNCE_LIMIT, 3);
    }

    public void setFragile(boolean fragile) {
        entityData.set(DATA_FRAGILE, fragile);
    }

    public boolean isFragile() {
        return entityData.get(DATA_FRAGILE);
    }

    public void setBounce(double bounce, int limit) {
        entityData.set(DATA_BOUNCE, (float) bounce);
        entityData.set(DATA_BOUNCE_LIMIT, limit);
    }

    public double getBounce() {
        return entityData.get(DATA_BOUNCE);
    }

    public int getBounceLimit() {
        return entityData.get(DATA_BOUNCE_LIMIT);
    }

    private void setBounceLimit(int limit) {
        entityData.set(DATA_BOUNCE_LIMIT, limit);
    }

    public void setFrosty(int frosty) {
        entityData.set(DATA_FROSTY, Math.max(0, frosty));
    }

    public int getFrosty() {
        return entityData.get(DATA_FROSTY);
    }

    @Override
    public void setDamage(float damage) {
        float safeDamage = Math.max(0.0F, damage);
        super.setDamage(safeDamage);
        entityData.set(DATA_DAMAGE, safeDamage);
        refreshDimensions();
    }

    public float getDamage() {
        return entityData.get(DATA_DAMAGE);
    }

    public float getVisualDamage() {
        return getDamage();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float size = 0.15F + getDamage() * 0.15F;
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_DAMAGE.equals(accessor)) {
            damage = getDamage();
            refreshDimensions();
        }
    }

    @Override
    public void tick() {
        damage = getDamage();
        super.tick();

        if (level.isClientSide && getFrosty() > 0) {
            float spread = getDamage() / 10.0F;
            for (int i = 0; i < getFrosty(); i++) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        getX() - spread + random.nextFloat() * spread * 2.0F,
                        getY() - spread + random.nextFloat() * spread * 2.0F,
                        getZ() - spread + random.nextFloat() * spread * 2.0F,
                        0.0D, 0.005D, 0.0D);
            }
        }

        Vec3 motion = getDeltaMovement();
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (motion.lengthSqr() > 1.0E-8D) {
            float targetYaw = (float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG);
            float targetPitch = (float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG);
            while (targetPitch - xRotO < -180.0F) xRotO -= 360.0F;
            while (targetPitch - xRotO >= 180.0F) xRotO += 360.0F;
            while (targetYaw - yRotO < -180.0F) yRotO -= 360.0F;
            while (targetYaw - yRotO >= 180.0F) yRotO += 360.0F;
            setXRot(xRotO + (targetPitch - xRotO) * 0.2F);
            setYRot(yRotO + (targetYaw - yRotO) * 0.2F);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        Vec3 impact = hit.getLocation();
        setPos(impact.x, impact.y, impact.z);

        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            reflectFromEntity(target);
            spawnShardParticles(impactParticleCount(), Blocks.PACKED_ICE.defaultBlockState());
            if (!level.isClientSide) {
                damageEntity(target);
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            reflectFromBlock(blockHit.getDirection());
            spawnShardParticles(impactParticleCount(), state);
            if (!level.isClientSide) {
                level.playSound(null, blockHit.getBlockPos(), state.getSoundType().getBreakSound(), SoundSource.NEUTRAL,
                        0.3F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            }
        } else {
            return;
        }

        pullBackFromImpact();
        hasImpulse = true;

        int previousLimit = getBounceLimit();
        setBounceLimit(previousLimit - 1);
        if (previousLimit <= 0) {
            shatterAndDiscard();
        }
    }

    private void reflectFromEntity(Entity target) {
        Vec3 motion = getDeltaMovement();
        int ox = Mth.floor(getX()) - Mth.floor(target.getX());
        int oy = Mth.floor(getY()) - Mth.floor(target.getY());
        int oz = Mth.floor(getZ()) - Mth.floor(target.getZ());
        if (oz != 0) motion = new Vec3(motion.x, motion.y, -motion.z);
        if (ox != 0) motion = new Vec3(-motion.x, motion.y, motion.z);
        if (oy != 0) motion = new Vec3(motion.x, -motion.y * 0.9D, motion.z);
        // TC4 damped entity impacts by 0.66 before applying the configured bounce.
        setDeltaMovement(motion.scale(0.66D * getBounce()));
    }

    private void reflectFromBlock(Direction direction) {
        Vec3 motion = getDeltaMovement();
        if (direction.getStepZ() != 0) motion = new Vec3(motion.x, motion.y, -motion.z);
        if (direction.getStepX() != 0) motion = new Vec3(-motion.x, motion.y, motion.z);
        if (direction.getStepY() != 0) motion = new Vec3(motion.x, -motion.y * 0.9D, motion.z);
        // Unlike entity impacts, original block ricochets did not receive 0.66 damping.
        setDeltaMovement(motion.scale(getBounce()));
    }

    private void pullBackFromImpact() {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-8D) {
            Vec3 correction = motion.normalize().scale(0.05D);
            setPos(getX() - correction.x, getY() - correction.y, getZ() - correction.z);
        }
    }

    private void damageEntity(Entity target) {
        Vec3 oldMotion = target.getDeltaMovement();
        Entity owner = getOwner();
        target.hurt(DamageSource.thrown(this, owner), getDamage());

        if (target instanceof LivingEntity living && getFrosty() > 0) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, getFrosty() - 1));
        }

        if (isFragile()) {
            if (target instanceof LivingEntity living) {
                living.invulnerableTime = 0;
            }
            Vec3 changed = target.getDeltaMovement();
            target.setDeltaMovement(oldMotion.add(changed.subtract(oldMotion).scale(0.1D)));
            shatterAndDiscard();
        }
    }

    private int impactParticleCount() {
        return Math.max(1, Mth.ceil(getDamage()));
    }

    private void shatterAndDiscard() {
        if (isRemoved()) return;
        if (!level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(),
                    Blocks.PACKED_ICE.defaultBlockState().getSoundType().getBreakSound(),
                    SoundSource.NEUTRAL, 0.3F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            spawnShardParticles(Math.max(8, Mth.ceil(8.0F * getDamage())), Blocks.PACKED_ICE.defaultBlockState());
        }
        discard();
    }

    private void spawnShardParticles(int count, BlockState state) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    getX(), getY(), getZ(), count,
                    0.5D, 0.15D, 0.5D, 0.35D);
        }
    }

    @Override
    protected double tc4Gravity() {
        return isFragile() ? 0.015D : 0.05D;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", getDamage());
        tag.putBoolean("fragile", isFragile());
        tag.putInt("frost", getFrosty());
        // Original TC4 sent these as spawn data. Persisting them also preserves an
        // in-flight boulder across modern chunk save/reload boundaries.
        tag.putDouble("bounce", getBounce());
        tag.putInt("bounceLimit", getBounceLimit());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDamage(tag.contains("damage") ? tag.getFloat("damage") : 3.0F);
        setFragile(tag.getBoolean("fragile"));
        setFrosty(tag.getInt("frost"));
        setBounce(tag.contains("bounce") ? tag.getDouble("bounce") : 0.5D,
                tag.contains("bounceLimit") ? tag.getInt("bounceLimit") : 3);
    }
}
