package com.darkifov.thaumcraft.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Stage174 base adapter for original TC4 focus projectile entities.
 *
 * This is intentionally a small Forge 1.19.2 shell around the original entity
 * semantics.  Specific projectiles keep their TC4 names and configurable fields
 * (damage, strength, bounce, frosty, seeker), while exact renderer/model polish
 * remains a later parity stage.
 */
public abstract class TC4FocusProjectileEntity extends Projectile {
    protected float damage = 3.0F;
    protected int life = 0;
    protected int maxLife = 80;

    protected TC4FocusProjectileEntity(EntityType<? extends TC4FocusProjectileEntity> type, Level level) {
        super(type, level);
    }

    protected TC4FocusProjectileEntity(EntityType<? extends TC4FocusProjectileEntity> type, Level level, Player owner) {
        this(type, level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        HitResult hit = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hit)) {
            onHit(hit);
        }
        move(MoverType.SELF, getDeltaMovement());
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -tc4Gravity(), 0.0D));
        }
        setDeltaMovement(getDeltaMovement().scale(0.99D));
        if (!level.isClientSide && life > maxLife) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            onHitLiving(living);
            if (!level.isClientSide && shouldDiscardAfterEntityHit(living)) discard();
        } else if (hit instanceof BlockHitResult blockHit) {
            onHitBlockTC4(blockHit);
        }
    }

    protected void onHitLiving(LivingEntity living) {
        living.hurt(DamageSource.MAGIC, damage);
    }

    protected boolean shouldDiscardAfterEntityHit(LivingEntity living) {
        return true;
    }

    protected double tc4Gravity() {
        return 0.03D;
    }

    protected void onHitBlockTC4(BlockHitResult hit) {
        if (!level.isClientSide) discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("damage", damage);
        tag.putInt("life", life);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getFloat("damage");
        life = tag.getInt("life");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
