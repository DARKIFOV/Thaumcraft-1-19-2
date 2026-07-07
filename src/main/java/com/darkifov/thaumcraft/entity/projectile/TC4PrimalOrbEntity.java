package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TC4PrimalOrbEntity extends TC4FocusProjectileEntity {
    private boolean seeker;
    private Aspect aspect = Aspect.IGNIS;

    public TC4PrimalOrbEntity(EntityType<? extends TC4PrimalOrbEntity> type, Level level) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level);
        maxLife = 5000;
        rollAspect();
    }

    public TC4PrimalOrbEntity(EntityType<? extends TC4PrimalOrbEntity> type, Level level, Player owner) {
        super((EntityType<? extends TC4FocusProjectileEntity>) type, level, owner);
        maxLife = 5000;
        rollAspect();
    }

    public void setSeeker(boolean seeker) { this.seeker = seeker; }
    public Aspect getAspect() { return aspect; }

    @Override
    public void tick() {
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.ENCHANT, getX(), getY(), getZ(), random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            }
        } else if (life > 20) {
            if (!seeker) {
                java.util.Random deterministic = new java.util.Random(getId() + life);
                setDeltaMovement(getDeltaMovement().add(
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F,
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F,
                        (deterministic.nextFloat() - deterministic.nextFloat()) * 0.01F));
            } else {
                LivingEntity target = nearestSeekerTarget();
                if (target != null) {
                    double d = Math.max(1.0D, distanceToSqr(target));
                    double dx = (target.getX() - getX()) / d;
                    double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.9D - getY()) / d;
                    double dz = (target.getZ() - getZ()) / d;
                    Vec3 next = getDeltaMovement().add(dx * 0.2D, dy * 0.2D, dz * 0.2D);
                    setDeltaMovement(Mth.clamp((float) next.x, -0.2F, 0.2F), Mth.clamp((float) next.y, -0.2F, 0.2F), Mth.clamp((float) next.z, -0.2F, 0.2F));
                }
            }
        }
        super.tick();
    }

    private LivingEntity nearestSeekerTarget() {
        Entity owner = getOwner();
        AABB box = getBoundingBox().inflate(16.0D);
        LivingEntity closest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
            double distance = distanceToSqr(candidate);
            if (distance < best) {
                best = distance;
                closest = candidate;
            }
        }
        return closest;
    }

    @Override
    protected void onHitLiving(LivingEntity living) { primalImpact(living.blockPosition()); }

    @Override
    protected double tc4Gravity() {
        return 0.001D;
    }

    @Override
    protected void onHitBlockTC4(BlockHitResult hit) {
        if (!level.isClientSide) {
            primalImpact(hit.getBlockPos());
        }
    }

    private void primalImpact(BlockPos impact) {
        if (level.isClientSide) return;
        float specialChance = isInWaterOrBubble() ? 10.0F : 1.0F;
        float strength = isInWaterOrBubble() ? 4.0F : 2.0F;
        level.explode(this, getX(), getY(), getZ(), strength, true, Explosion.BlockInteraction.BREAK);
        if (!seeker && random.nextInt(100) <= specialChance) {
            if (random.nextBoolean()) {
                taintSplosion(impact);
            } else if (level.getBlockState(impact.above()).isAir()) {
                level.setBlock(impact.above(), ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
            }
        }
        discard();
    }

    private void taintSplosion(BlockPos impact) {
        for (int i = 0; i < 10; i++) {
            BlockPos p = impact.offset((int) ((random.nextFloat() - random.nextFloat()) * 6.0F), 0, (int) ((random.nextFloat() - random.nextFloat()) * 6.0F));
            BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, p);
            if (level.getBlockState(surface).isAir() && !level.getBlockState(surface.below()).isAir()) {
                level.setBlock(surface, ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState(), 3);
            }
        }
    }

    private void rollAspect() {
        Aspect[] primals = {Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
        aspect = primals[random.nextInt(primals.length)];
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("seeker", seeker); tag.putString("aspect", aspect.name()); }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); seeker = tag.getBoolean("seeker"); try { aspect = Aspect.valueOf(tag.getString("aspect")); } catch (Exception ignored) { aspect = Aspect.IGNIS; } }
}
