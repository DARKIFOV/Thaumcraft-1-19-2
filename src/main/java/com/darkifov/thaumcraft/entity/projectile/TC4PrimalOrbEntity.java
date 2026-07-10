package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeProfile;
import com.darkifov.thaumcraft.aura.AuraNodeWorldRuntime;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Modern 1.19.2 adapter for TC4 4.2.3.5 EntityPrimalOrb. */
public class TC4PrimalOrbEntity extends TC4FocusProjectileEntity {
    private static final EntityDataAccessor<Boolean> DATA_SEEKER = SynchedEntityData.defineId(TC4PrimalOrbEntity.class, EntityDataSerializers.BOOLEAN);

    public TC4PrimalOrbEntity(EntityType<? extends TC4PrimalOrbEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 5000;
    }

    public TC4PrimalOrbEntity(EntityType<? extends TC4PrimalOrbEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 5000;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_SEEKER, false);
    }

    public void setSeeker(boolean seeker) {
        entityData.set(DATA_SEEKER, seeker);
    }

    public boolean isSeeker() {
        return entityData.get(DATA_SEEKER);
    }

    @Override
    public void tick() {
        if (isInWaterOrBubble()) {
            if (level.isClientSide) {
                spawnImpactParticles();
            } else {
                primalImpact(blockPosition());
            }
            return;
        }

        if (level.isClientSide) {
            for (int i = 0; i < 6; i++) {
                level.addParticle(ParticleTypes.ENCHANT,
                        getX() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                        getY() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                        getZ() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                        0.0D, 0.0D, 0.0D);
            }
            level.addParticle(ParticleTypes.WITCH,
                    getX() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                    getY() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                    getZ() + (random.nextFloat() - random.nextFloat()) * 0.2F,
                    0.0D, 0.0D, 0.0D);
        }

        int nextCount = life + 1;
        if (tickCount > 20) {
            if (!isSeeker()) {
                java.util.Random deterministic = new java.util.Random(getId() + nextCount);
                setDeltaMovement(getDeltaMovement().add(
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F,
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F,
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F));
            } else {
                LivingEntity target = nearestSeekerTarget();
                if (target != null) {
                    double distanceSq = Math.max(1.0D, distanceToSqr(target));
                    double dx = (target.getX() - getX()) / distanceSq;
                    double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.9D - getY()) / distanceSq;
                    double dz = (target.getZ() - getZ()) / distanceSq;
                    Vec3 next = getDeltaMovement().add(dx * 0.2D, dy * 0.2D, dz * 0.2D);
                    setDeltaMovement(
                            Mth.clamp((float) next.x, -0.2F, 0.2F),
                            Mth.clamp((float) next.y, -0.2F, 0.2F),
                            Mth.clamp((float) next.z, -0.2F, 0.2F));
                }
            }
        }

        super.tick();
    }

    private LivingEntity nearestSeekerTarget() {
        Entity owner = getOwner();
        int ownerId = owner == null ? Integer.MIN_VALUE : owner.getId();
        AABB box = getBoundingBox().inflate(16.0D);
        LivingEntity closest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e.getId() != ownerId && distanceToSqr(e) <= 256.0D)) {
            double distance = distanceToSqr(candidate);
            if (distance < best) {
                best = distance;
                closest = candidate;
            }
        }
        return closest;
    }

    @Override
    protected void onHitLiving(LivingEntity living) {
        primalImpact(living.blockPosition());
    }

    @Override
    protected double tc4Gravity() {
        return 0.001D;
    }

    @Override
    protected double tc4Drag() {
        return 1.0D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        primalImpact(hit.getBlockPos());
    }

    private void primalImpact(BlockPos impact) {
        if (level.isClientSide) {
            spawnImpactParticles();
            return;
        }
        if (isRemoved()) {
            return;
        }

        boolean waterImpact = isInWaterOrBubble();
        float specialChance = waterImpact ? 10.0F : 1.0F;
        float strength = waterImpact ? 4.0F : 2.0F;
        level.explode(null, getX(), getY(), getZ(), strength, true, Explosion.BlockInteraction.BREAK);

        if (!isSeeker() && random.nextInt(100) <= specialChance) {
            if (random.nextBoolean()) {
                taintSplosion(impact);
            } else {
                createRandomNodeAtImpact(impact);
            }
        }

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ENCHANT, getX(), getY(), getZ(), 72, 0.5D, 0.5D, 0.5D, 0.12D);
            server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 36, 0.5D, 0.5D, 0.5D, 0.08D);
        }
        discard();
    }

    private void createRandomNodeAtImpact(BlockPos impact) {
        if (!(level instanceof ServerLevel server) || !level.getBlockState(impact).isAir()) {
            return;
        }
        level.setBlock(impact, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(impact);
        if (blockEntity instanceof AuraNodeBlockEntity node) {
            AuraNodeProfile profile = AuraNodeWorldRuntime.createRandomWorldgenProfile(server, impact, random, false, false, false);
            node.initializeAs(profile.type(), profile.modifier(), profile.aspects());
        }
    }

    private void taintSplosion(BlockPos impact) {
        int x = Mth.floor(getX());
        int z = Mth.floor(getZ());
        for (int i = 0; i < 10; i++) {
            int xx = x + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            int zz = z + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            if (!random.nextBoolean()) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(xx, impact.getY(), zz));
            if (level.getBlockState(surface).isAir() && !level.getBlockState(surface.below()).isAir()) {
                level.setBlock(surface, ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState(), 3);
            }
        }
    }

    private void spawnImpactParticles() {
        for (int i = 0; i < 36; i++) {
            float fx = (random.nextFloat() - random.nextFloat()) * 0.5F;
            float fy = (random.nextFloat() - random.nextFloat()) * 0.5F;
            float fz = (random.nextFloat() - random.nextFloat()) * 0.5F;
            level.addParticle(i % 2 == 0 ? ParticleTypes.ENCHANT : ParticleTypes.WITCH,
                    getX() + fx, getY() + fy, getZ() + fz, fx * 0.5D, fy * 0.5D, fz * 0.5D);
        }
    }

    @Override
    public float getPickRadius() {
        return 0.1F;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("seeker", isSeeker());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSeeker(tag.getBoolean("seeker"));
    }
}
