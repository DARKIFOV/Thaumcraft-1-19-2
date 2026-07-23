package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.damage.TC4DamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stage303-322 direct 1.19.2 adapter for TC4 EntityTaintacle.
 *
 * <p>Original anchors preserved: size 0.66x3.0, xp 10, health 50, damage 7,
 * stationary movement, target radius based on height, 20 tick melee cooldown,
 * 40+rand(20) distant small-tentacle spawn, agitation/flail intensity, and
 * taint fibre conversion around spawned tentacles. Old ITaintedMob/biome ids are
 * represented by existing 1.19.2 taint blocks and persistent breadcrumbs.</p>
 */
public class TaintacleEntity extends Monster implements TaintedMob {
    public static final String FLAIL_TAG = "flailIntensity";
    protected LivingEntity tendrilTarget;
    protected int attackCooldown;
    protected float flailIntensity = 1.0F;

    public TaintacleEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void registerGoals() {
        // Original EntityTaintacle does its target scan and facing manually.
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (attackCooldown > 0) attackCooldown--;
        if (level.isClientSide) {
            boolean agitated = getAgitationState();
            if (tickCount > getBbHeight() * 10.0F && (hurtTime > 0 || attackCooldown > 0 || agitated)) {
                flailIntensity = Math.min(3.0F, flailIntensity + 0.2F);
            } else if (flailIntensity > 1.0F) {
                flailIntensity = Math.max(1.0F, flailIntensity - 0.2F);
            }
            if (tickCount < getBbHeight() * 10.0F && this.onGround && random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, getRandomX(0.45D), getY() + 0.1D, getRandomZ(0.45D), 0.0D, 0.02D, 0.0D);
            }
            return;
        }
        if (tickCount % 20 == 0 && !isOnTaintLikeBlock()) {
            hurt(DamageSource.MAGIC, 1.0F);
        }
        if (tendrilTarget == null || !tendrilTarget.isAlive() || !getAgitationState()) {
            tendrilTarget = findTaintacleTarget();
        }
        if (tendrilTarget != null && tendrilTarget.isAlive() && hasLineOfSight(tendrilTarget)) {
            faceEntityLikeTC4(tendrilTarget, 5.0F);
            attackEntityLikeTC4(tendrilTarget, distanceTo(tendrilTarget));
        }
    }

    protected LivingEntity findTaintacleTarget() {
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        double xz = getBbHeight() * 6.0F;
        double y = getBbHeight() * 3.0F;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(xz, y, xz), LivingEntity::isAlive)) {
            if (candidate == this || candidate instanceof TaintacleEntity || candidate instanceof TaintacleGiantEntity || candidate instanceof TaintCrawlerEntity) continue;
            double d = candidate.distanceToSqr(this);
            if (d < best) {
                best = d;
                target = candidate;
            }
        }
        return target;
    }

    protected void attackEntityLikeTC4(Entity entity, float distance) {
        if (attackCooldown > 0) return;
        if (distance <= getBbHeight() && entity.getBoundingBox().maxY > getBoundingBox().minY && entity.getBoundingBox().minY < getBoundingBox().maxY) {
            attackCooldown = 20;
            if (entity.hurt(TC4DamageSources.tentacle(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE))
                    && level instanceof ServerLevel server) {
                server.playSound(null, blockPosition(), TC4Sounds.event("tentacle"), SoundSource.HOSTILE, getSoundVolume(), getVoicePitch());
            }
        } else if (distance > getBbHeight() && isEntityOnGround1192Adapter(entity) && !(this instanceof TaintacleSmallEntity)) {
            spawnTentacleSmall(entity);
        }
    }

    /**
     * Forge 1.19.2 adapter for TC4 Entity.onGround checks on arbitrary targets.
     * Mojang mappings keep Entity.onGround protected in this target, so external
     * target checks use the public verticalCollisionBelow flag that backs the
     * same ground-contact state. No gameplay effect is invented here.
     */
    protected boolean isEntityOnGround1192Adapter(Entity entity) {
        return entity.verticalCollisionBelow;
    }

    protected void spawnTentacleSmall(Entity entity) {
        if (!(level instanceof ServerLevel server)) return;
        attackCooldown = 40 + random.nextInt(20);
        TaintacleSmallEntity small = ThaumcraftMod.TAINTACLE_SMALL.get().create(server);
        if (small == null) return;
        small.moveTo(entity.getX() + random.nextFloat() - random.nextFloat(), entity.getY(), entity.getZ() + random.nextFloat() - random.nextFloat(), 0.0F, 0.0F);
        small.getPersistentData().putString("TC4Original", "EntityTaintacle.spawnTentacles");
        server.addFreshEntity(small);
        server.playSound(null, blockPosition(), TC4Sounds.event("tentacle"), SoundSource.HOSTILE, getSoundVolume(), getVoicePitch());
        BlockPos p = small.blockPosition();
        if (server.isEmptyBlock(p) && hasSolidNeighbor(server, p)) {
            server.setBlock(p, random.nextInt(4) == 0 ? ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState() : ThaumcraftMod.TAINT_CRUST.get().defaultBlockState(), 3);
        }
    }

    private static boolean hasSolidNeighbor(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below())
                || level.getBlockState(pos.north()).isSolidRender(level, pos.north())
                || level.getBlockState(pos.south()).isSolidRender(level, pos.south())
                || level.getBlockState(pos.east()).isSolidRender(level, pos.east())
                || level.getBlockState(pos.west()).isSolidRender(level, pos.west());
    }

    protected boolean isOnTaintLikeBlock() {
        BlockPos pos = blockPosition();
        return level.getBlockState(pos).is(ThaumcraftMod.TAINT_FIBRES.get())
                || level.getBlockState(pos).is(ThaumcraftMod.TAINT_CRUST.get())
                || level.getBlockState(pos.below()).is(ThaumcraftMod.TAINT_CRUST.get())
                || level.getBlockState(pos.below()).is(ThaumcraftMod.TAINT_SOIL.get())
                || level.getBlockState(pos.below()).is(ThaumcraftMod.FLESH_BLOCK.get());
    }

    protected void faceEntityLikeTC4(Entity entity, float maxTurn) {
        double dx = entity.getX() - getX();
        double dz = entity.getZ() - getZ();
        float targetYaw = (float)(Mth.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        setYRot(Mth.approachDegrees(getYRot(), targetYaw, maxTurn));
        yBodyRot = getYRot();
    }

    public boolean getAgitationState() {
        return tendrilTarget != null && tendrilTarget.distanceToSqr(this) < getBbHeight() * 7.0F * (getBbHeight() * 7.0F);
    }

    public float getFlailIntensity() { return flailIntensity; }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0.0D, Math.min(0.0D, movement.y), 0.0D));
    }

    @Override
    public boolean isPushable() { return true; }

    @Override
    protected int decreaseAirSupply(int air) { return air; }

    @Override
    protected float getSoundVolume() { return getBbHeight() / 8.0F; }

    @Override
    public float getVoicePitch() { return 1.3F - getBbHeight() / 10.0F; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(this instanceof TaintacleSmallEntity) && source.getEntity() != null && distanceTo(source.getEntity()) > 16.0F) {
            spawnTentacleSmall(source.getEntity());
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (random.nextBoolean()) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.TAINTED_SLIME.get(), 1));
        }
        super.dropCustomDeathLoot(source, looting, recentlyHit);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackCooldown", attackCooldown);
        tag.putFloat(FLAIL_TAG, flailIntensity);
        tag.putString("TC4Original", "EntityTaintacle");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attackCooldown = tag.getInt("AttackCooldown");
        flailIntensity = tag.contains(FLAIL_TAG) ? tag.getFloat(FLAIL_TAG) : 1.0F;
    }
}
