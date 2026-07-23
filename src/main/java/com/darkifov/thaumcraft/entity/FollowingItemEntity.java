package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

import java.util.UUID;

/**
 * TC4 {@code EntityFollowingItem}: a temporary no-clip item that curves toward
 * an entity or coordinate, then becomes a normal SpecialItem when it arrives.
 */
public final class FollowingItemEntity extends SpecialItemEntity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(FollowingItemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PARTICLE_TYPE =
            SynchedEntityData.defineId(FollowingItemEntity.class, EntityDataSerializers.INT);

    private UUID targetUuid;
    private Vec3 targetPosition = Vec3.ZERO;
    private int homingTicks = 20;
    private double followGravity = 0.04D;

    public FollowingItemEntity(EntityType<? extends FollowingItemEntity> type, Level level) {
        super(type, level);
    }

    public FollowingItemEntity(Level level, double x, double y, double z, ItemStack stack,
                               Entity target, int particleType) {
        super(ThaumcraftMod.FOLLOWING_ITEM.get(), level, x, y, z, stack);
        setTarget(target);
        setParticleType(particleType);
    }

    public FollowingItemEntity(Level level, double x, double y, double z, ItemStack stack,
                               Vec3 targetPosition, int particleType) {
        super(ThaumcraftMod.FOLLOWING_ITEM.get(), level, x, y, z, stack);
        this.targetPosition = targetPosition;
        setParticleType(particleType);
        beginFollowing();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_TARGET_ID, -1);
        entityData.define(DATA_PARTICLE_TYPE, 3);
    }

    public void setTarget(Entity target) {
        if (target == null) {
            clearTarget();
            return;
        }
        targetUuid = target.getUUID();
        entityData.set(DATA_TARGET_ID, target.getId());
        targetPosition = targetCenter(target);
        beginFollowing();
    }

    public Entity getTarget() {
        int id = entityData.get(DATA_TARGET_ID);
        Entity target = id < 0 ? null : level.getEntity(id);
        if (target != null) {
            return target;
        }
        if (!level.isClientSide && targetUuid != null && level instanceof ServerLevel serverLevel) {
            target = serverLevel.getEntity(targetUuid);
            if (target != null) {
                entityData.set(DATA_TARGET_ID, target.getId());
            }
        }
        return target;
    }

    public void setParticleType(int type) {
        entityData.set(DATA_PARTICLE_TYPE, type);
    }

    public int getParticleType() {
        return entityData.get(DATA_PARTICLE_TYPE);
    }

    private void beginFollowing() {
        noPhysics = true;
        setNoGravity(true);
    }

    private void clearTarget() {
        targetUuid = null;
        entityData.set(DATA_TARGET_ID, -1);
        targetPosition = Vec3.ZERO;
        noPhysics = false;
        setNoGravity(false);
    }

    @Override
    public void tick() {
        Entity target = getTarget();
        if (target != null) {
            targetPosition = targetCenter(target);
        }

        if (!targetPosition.equals(Vec3.ZERO)) {
            Vec3 delta = targetPosition.subtract(position());
            if (homingTicks > 1) {
                homingTicks--;
            }
            double distance = delta.length();
            if (distance > 0.5D) {
                setDeltaMovement(delta.scale(1.0D / (distance * homingTicks)));
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.1D));
                clearTarget();
            }
            spawnTrailParticle();
        } else {
            noPhysics = false;
            setNoGravity(false);
            setDeltaMovement(getDeltaMovement().add(0.0D, -followGravity, 0.0D));
        }

        // SpecialItemEntity adds its +0.04 anti-gravity impulse. While the
        // item is homing, noGravity suppresses the vanilla half of that pair.
        // Compensate so the trajectory remains exactly controlled by delta.
        if (isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        super.tick();
    }

    private static Vec3 targetCenter(Entity target) {
        return new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0D, target.getZ());
    }

    private void spawnTrailParticle() {
        if (!level.isClientSide) {
            return;
        }
        double x = xo + (random.nextFloat() - random.nextFloat()) * 0.125F;
        double y = yo + getBbHeight() * 0.5D + (random.nextFloat() - random.nextFloat()) * 0.125F;
        double z = zo + (random.nextFloat() - random.nextFloat()) * 0.125F;
        int type = getParticleType();
        if (type == 10) {
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.02D, 0.0D);
            return;
        }
        float[] rgb = particleColor(type);
        level.addParticle(new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 0.65F),
                x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static float[] particleColor(int type) {
        return switch (type) {
            case 1 -> new float[]{0.85F, 0.25F, 0.20F};
            case 2 -> new float[]{0.35F, 0.75F, 1.00F};
            case 3 -> new float[]{0.35F, 0.85F, 0.35F};
            case 4 -> new float[]{0.95F, 0.85F, 0.25F};
            case 5 -> new float[]{0.65F, 0.35F, 0.90F};
            default -> new float[]{0.75F, 0.55F, 1.00F};
        };
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(targetUuid != null);
        if (targetUuid != null) {
            buffer.writeUUID(targetUuid);
        }
        buffer.writeDouble(targetPosition.x);
        buffer.writeDouble(targetPosition.y);
        buffer.writeDouble(targetPosition.z);
        buffer.writeVarInt(Math.max(1, homingTicks));
        buffer.writeDouble(followGravity);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        targetUuid = buffer.readBoolean() ? buffer.readUUID() : null;
        targetPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        homingTicks = Math.max(1, buffer.readVarInt());
        followGravity = buffer.readDouble();
        if (!targetPosition.equals(Vec3.ZERO) || targetUuid != null) {
            beginFollowing();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetUuid != null) {
            tag.putUUID("Target", targetUuid);
        }
        tag.putDouble("TargetX", targetPosition.x);
        tag.putDouble("TargetY", targetPosition.y);
        tag.putDouble("TargetZ", targetPosition.z);
        tag.putInt("type", getParticleType());
        tag.putInt("HomingTicks", homingTicks);
        tag.putDouble("Gravity", followGravity);
        tag.putString("TC4Original", "EntityFollowingItem");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            targetUuid = tag.getUUID("Target");
        }
        targetPosition = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        setParticleType(tag.contains("type") ? tag.getInt("type") : 3);
        homingTicks = Math.max(1, tag.contains("HomingTicks") ? tag.getInt("HomingTicks") : 20);
        followGravity = tag.contains("Gravity") ? tag.getDouble("Gravity") : 0.04D;
        if (!targetPosition.equals(Vec3.ZERO) || targetUuid != null) {
            beginFollowing();
        }
    }
}
