package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.damage.TC4DamageSources;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

/**
 * TC4's invisible flying Taint Swarm. The entity body drives combat and flight;
 * the client-side cloud is rendered entirely with particles, matching the old
 * no-op RenderTaintSwarm plus swarmParticleFX pipeline.
 */
public final class TaintSwarmEntity extends FlyingMob implements Enemy, TaintedMob {
    private static final EntityDataAccessor<Boolean> SUMMONED =
            SynchedEntityData.defineId(TaintSwarmEntity.class, EntityDataSerializers.BOOLEAN);

    private BlockPos flightTarget;
    private int attackCooldown;
    private int targetSearchCooldown;
    private int damageBonus;

    public TaintSwarmEntity(EntityType<? extends TaintSwarmEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FLYING_SPEED, 0.70D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SUMMONED, false);
    }

    @Override
    protected void registerGoals() {
        // TC4 uses direct steering and a nearest-player query instead of goals.
    }

    public boolean isSummoned() {
        return entityData.get(SUMMONED);
    }

    public void setSummoned(boolean summoned) {
        entityData.set(SUMMONED, summoned);
    }

    public void setDamageBonus(int bonus) {
        damageBonus = Math.max(0, bonus);
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(2.0D + damageBonus);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (level.isClientSide) {
            spawnSwarmParticles();
        }
    }

    private void spawnSwarmParticles() {
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            double x = getX() + random.nextGaussian() * 0.55D;
            double y = getY() + getBbHeight() * 0.5D + random.nextGaussian() * 0.45D;
            double z = getZ() + random.nextGaussian() * 0.55D;
            level.addParticle(ParticleTypes.WITCH, x, y, z,
                    random.nextGaussian() * 0.015D, random.nextGaussian() * 0.01D,
                    random.nextGaussian() * 0.015D);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }
        if (attackCooldown > 0) attackCooldown--;
        updateTarget();
        steerFlight();
        tryAttackTarget();
    }

    private void updateTarget() {
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || target.distanceToSqr(this) > 144.0D
                || target instanceof Player player && player.getAbilities().instabuild)) {
            setTarget(null);
            target = null;
        }

        if (target == null && isSummoned()) {
            hurt(DamageSource.STARVE, 5.0F);
            if (!isAlive()) return;
        }

        if (target == null && !isSummoned() && targetSearchCooldown-- <= 0) {
            targetSearchCooldown = 20;
            Player player = level.getNearestPlayer(this, 12.0D);
            if (player != null && !player.getAbilities().instabuild) setTarget(player);
        }
    }

    private void steerFlight() {
        LivingEntity target = getTarget();
        double tx;
        double ty;
        double tz;
        double response;

        if (target == null) {
            if (!isValidFlightTarget(flightTarget) || random.nextInt(30) == 0
                    || flightTarget.distSqr(blockPosition()) < 4.0D) {
                flightTarget = blockPosition().offset(
                        random.nextInt(7) - random.nextInt(7),
                        random.nextInt(6) - 2,
                        random.nextInt(7) - random.nextInt(7));
            }
            tx = flightTarget.getX() + 0.5D;
            ty = flightTarget.getY() + 0.1D;
            tz = flightTarget.getZ() + 0.5D;
            response = 0.015D;
        } else {
            tx = target.getX();
            ty = target.getY() + target.getEyeHeight();
            tz = target.getZ();
            response = 0.025D;
        }

        Vec3 motion = getDeltaMovement();
        double dx = tx - getX();
        double dy = ty - getY();
        double dz = tz - getZ();
        setDeltaMovement(
                motion.x + (Math.signum(dx) * 0.5D - motion.x) * response,
                motion.y + (Math.signum(dy) * 0.7D - motion.y) * 0.10D,
                motion.z + (Math.signum(dz) * 0.5D - motion.z) * response);
        Vec3 updated = getDeltaMovement();
        setYRot((float) (Mth.atan2(updated.z, updated.x) * Mth.RAD_TO_DEG) - 90.0F);
        yBodyRot = getYRot();
    }

    private boolean isValidFlightTarget(BlockPos target) {
        if (target == null || target.getY() < 1 || !(level instanceof ServerLevel server)) return false;
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, target.getX(), target.getZ()) + 8;
        return target.getY() <= top && level.isEmptyBlock(target)
                && TaintSpreadRuntime.isColumnTainted(server, target);
    }

    private void tryAttackTarget() {
        LivingEntity target = getTarget();
        if (target == null || attackCooldown > 0 || distanceToSqr(target) >= 9.0D
                || target.getBoundingBox().maxY <= getBoundingBox().minY
                || target.getBoundingBox().minY >= getBoundingBox().maxY) {
            return;
        }

        Vec3 targetMotion = target.getDeltaMovement();
        float attackDamage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (target.hurt(TC4DamageSources.swarm(this), attackDamage)) {
            target.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), 100, 0), this);
            target.setDeltaMovement(targetMotion);
            playSound(TC4Sounds.event("swarmattack"), 0.3F, 0.9F + random.nextFloat() * 0.2F);
        }
        attackCooldown = 10 + random.nextInt(5);
    }

    @Override
    public void travel(Vec3 input) {
        if (isEffectiveAi() || isControlledByLocalInstance()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().multiply(0.91D, 0.60D, 0.91D));
        } else {
            super.travel(input);
        }
    }

    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) { }
    @Override public float getLightLevelDependentMagicValue() { return 1.0F; }
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return TC4Sounds.event("swarmattack"); }
    @Override protected SoundEvent getDeathSound() { return TC4Sounds.event("swarmattack"); }
    @Override protected float getSoundVolume() { return 0.1F; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Summoned", isSummoned());
        tag.putInt("damBonus", damageBonus);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSummoned(tag.getBoolean("Summoned"));
        setDamageBonus(tag.getInt("damBonus"));
    }
}
