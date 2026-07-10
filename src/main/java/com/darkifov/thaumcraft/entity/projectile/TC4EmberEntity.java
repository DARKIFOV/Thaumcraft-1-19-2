package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/** Exact modern adapter for TC4 4.2.3.5 EntityEmber. */
public class TC4EmberEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(TC4EmberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIREY = SynchedEntityData.defineId(TC4EmberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TC4EmberEntity.class, EntityDataSerializers.FLOAT);

    public TC4EmberEntity(EntityType<? extends TC4EmberEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        this.maxLife = 20;
        setNoGravity(true);
    }

    public TC4EmberEntity(EntityType<? extends TC4EmberEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        this.maxLife = 20;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_DURATION, 20);
        entityData.define(DATA_FIREY, 0);
        entityData.define(DATA_DAMAGE, 1.0F);
    }

    public void setDuration(int duration) {
        int safe = Math.max(1, duration);
        entityData.set(DATA_DURATION, safe);
        this.maxLife = safe;
    }

    public int getDuration() { return entityData.get(DATA_DURATION); }
    public void setFirey(int firey) { entityData.set(DATA_FIREY, Math.max(0, firey)); }
    public int getFirey() { return entityData.get(DATA_FIREY); }

    @Override
    public void setDamage(float damage) {
        super.setDamage(damage);
        entityData.set(DATA_DAMAGE, Math.max(0.0F, damage));
    }

    public float getDamage() { return entityData.get(DATA_DAMAGE); }

    @Override
    public void tick() {
        if (tickCount > getDuration()) {
            discard();
            return;
        }
        maxLife = getDuration();
        damage = getDamage();
        super.tick();
    }

    @Override
    protected void beforeTc4Move() {
        double drag = getDuration() <= 20 ? 0.95D : 0.975D;
        setDeltaMovement(getDeltaMovement().scale(drag));
        if (onGround) setDeltaMovement(getDeltaMovement().scale(0.66D));
    }

    @Override
    protected double tc4Gravity() { return 0.0D; }

    @Override
    protected void onHitLiving(LivingEntity living) {
        if (level.isClientSide || living.fireImmune()) return;
        LivingEntity owner = getOwner() instanceof LivingEntity livingOwner ? livingOwner : null;
        DamageSource source = owner == null ? DamageSource.indirectMagic(this, this) : DamageSource.indirectMagic(this, owner);
        if (living.hurt(source, getDamage())) {
            living.setSecondsOnFire(3 + getFirey());
        }
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        if (!level.isClientSide && random.nextFloat() < 0.025F * getFirey()) {
            BlockPos firePos = hit.getBlockPos().relative(hit.getDirection());
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
        }
        if (!level.isClientSide) discard();
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", getDamage());
        tag.putInt("firey", getFirey());
        tag.putInt("duration", getDuration());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDamage(tag.contains("damage") ? tag.getFloat("damage") : 1.0F);
        setFirey(tag.getInt("firey"));
        setDuration(tag.contains("duration") ? tag.getInt("duration") : 20);
    }
}
