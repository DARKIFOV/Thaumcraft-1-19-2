package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/**
 * TC4 EntityGolemBobber port. The bobber is owned by a fishing-core golem,
 * follows the original 0.1 launch vector, survives for at most 4000 ticks and
 * uses the old water buoyancy/drag constants instead of a player FishingHook.
 */
public final class GolemBobberEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FISHER_ID =
            SynchedEntityData.defineId(GolemBobberEntity.class, EntityDataSerializers.INT);

    private UUID fisherUuid;
    private int lifetime;
    private boolean touchedBlock;

    public GolemBobberEntity(EntityType<? extends GolemBobberEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public GolemBobberEntity(Level level, ThaumGolemEntity fisher, BlockPos target) {
        this(ThaumcraftMod.GOLEM_BOBBER.get(), level);
        setFisher(fisher);
        setPos(fisher.getX(), fisher.getY(), fisher.getZ());
        double dx = target.getX() + 0.5D - fisher.getX();
        double dy = target.getY() + 1.0D - fisher.getY();
        double dz = target.getZ() + 0.5D - fisher.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        setDeltaMovement(dx * 0.1D, dy * 0.1D + Math.sqrt(distance) * 0.08D, dz * 0.1D);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_FISHER_ID, -1);
    }

    public void setFisher(ThaumGolemEntity fisher) {
        fisherUuid = fisher.getUUID();
        entityData.set(DATA_FISHER_ID, fisher.getId());
    }

    public ThaumGolemEntity getFisher() {
        int id = entityData.get(DATA_FISHER_ID);
        Entity byId = id < 0 ? null : level.getEntity(id);
        if (byId instanceof ThaumGolemEntity golem) {
            return golem;
        }
        if (!level.isClientSide && fisherUuid != null && level instanceof ServerLevel serverLevel) {
            Entity byUuid = serverLevel.getEntity(fisherUuid);
            if (byUuid instanceof ThaumGolemEntity golem) {
                entityData.set(DATA_FISHER_ID, golem.getId());
                return golem;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;

        ThaumGolemEntity fisher = getFisher();
        if (!level.isClientSide && (fisher == null || !fisher.isAlive() || lifetime > 4000)) {
            discard();
            return;
        }

        // TC4 keeps a one-tick in-block state. A water collision damps the
        // throw on the following tick; every other block collision removes it.
        if (touchedBlock) {
            touchedBlock = false;
            setDeltaMovement(getDeltaMovement().multiply(
                    random.nextFloat() * 0.2D,
                    random.nextFloat() * 0.2D,
                    random.nextFloat() * 0.2D));
        }

        Vec3 velocity = getDeltaMovement();
        Vec3 start = position();
        Vec3 end = start.add(velocity);
        BlockHitResult hit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            if (level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
                touchedBlock = true;
            } else if (!level.isClientSide) {
                discard();
            }
            return;
        }

        move(MoverType.SELF, velocity);
        double waterFraction = waterFractionLikeTC4();
        double drag = (onGround || horizontalCollision) ? 0.50D : 0.92D;

        // EntityGolemBobber samples five vertical slices of its bounding box.
        // Partial immersion therefore produces a proportional upward force.
        Vec3 next = getDeltaMovement();
        double vertical = next.y + 0.04D * (waterFraction * 2.0D - 1.0D);
        if (waterFraction > 0.0D) {
            drag *= 0.90D;
            vertical *= 0.80D;
        }
        setDeltaMovement(next.x * drag, vertical * drag, next.z * drag);

        if (!level.isClientSide && waterFraction > 0.0D
                && level instanceof ServerLevel serverLevel && random.nextFloat() < 0.02F) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    getX() + random.nextFloat() - random.nextFloat(),
                    getY() + random.nextFloat(),
                    getZ() + random.nextFloat() - random.nextFloat(),
                    2 + random.nextInt(2), 0.1D, 0.0D, 0.1D, 0.0D);
        }
    }

    private double waterFractionLikeTC4() {
        double fraction = 0.0D;
        double minY = getBoundingBox().minY;
        double height = getBoundingBox().maxY - minY;
        for (int slice = 0; slice < 5; slice++) {
            double sampleY = minY + height * (slice + 0.5D) / 5.0D;
            BlockPos sample = new BlockPos(Mth.floor(getX()), Mth.floor(sampleY), Mth.floor(getZ()));
            var fluid = level.getFluidState(sample);
            if (fluid.is(FluidTags.WATER)
                    && sampleY < sample.getY() + fluid.getHeight(level, sample)) {
                fraction += 0.20D;
            }
        }
        return fraction;
    }

    public void splashAndDiscard() {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.15F,
                    1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            serverLevel.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5D, getZ(),
                    20 + random.nextInt(20), 0.1D, 0.0D, 0.1D, 0.0D);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifetime = Math.max(0, tag.getInt("Life"));
        if (tag.hasUUID("Fisher")) {
            fisherUuid = tag.getUUID("Fisher");
        }
        touchedBlock = tag.getBoolean("TouchedBlock");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", lifetime);
        if (fisherUuid != null) {
            tag.putUUID("Fisher", fisherUuid);
        }
        tag.putBoolean("TouchedBlock", touchedBlock);
        tag.putString("TC4Original", "EntityGolemBobber");
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
