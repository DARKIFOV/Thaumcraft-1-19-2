package com.darkifov.thaumcraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/** Forge 1.19.2 adaptation of TC4 EntityFireBat used by Focus: Nine Hells. */
public class TC4FireBatEntity extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(TC4FireBatEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POTENCY = SynchedEntityData.defineId(TC4FireBatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(TC4FireBatEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET = SynchedEntityData.defineId(TC4FireBatEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final int SUMMONED = 1;
    private static final int EXPLOSIVE = 2;
    private static final int DEVIL = 4;
    private static final int VAMPIRE = 8;

    private int attackCooldown;
    private int targetlessTicks;

    public TC4FireBatEntity(EntityType<? extends TC4FireBatEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FLYING_SPEED, 0.7D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_FLAGS, (byte) 0);
        entityData.define(DATA_POTENCY, 0);
        entityData.define(DATA_OWNER, Optional.empty());
        entityData.define(DATA_TARGET, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        // Original summoned Firebat uses its own direct flight steering rather than path navigation.
    }

    public void configure(Player owner, LivingEntity target, int potency, boolean explosive, boolean devil, boolean vampire) {
        setFlag(SUMMONED, true);
        setFlag(EXPLOSIVE, explosive);
        setFlag(DEVIL, devil);
        setFlag(VAMPIRE, vampire);
        entityData.set(DATA_POTENCY, Math.max(0, potency));
        entityData.set(DATA_OWNER, Optional.of(owner.getUUID()));
        entityData.set(DATA_TARGET, Optional.of(target.getUUID()));
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(devil ? 15.0D : 5.0D);
        setHealth(getMaxHealth());
    }

    public boolean isSummoned() { return hasFlag(SUMMONED); }
    public boolean isExplosive() { return hasFlag(EXPLOSIVE); }
    public boolean isDevil() { return hasFlag(DEVIL); }
    public boolean isVampire() { return hasFlag(VAMPIRE); }
    public int potency() { return entityData.get(DATA_POTENCY); }

    private boolean hasFlag(int flag) {
        return (entityData.get(DATA_FLAGS) & flag) != 0;
    }

    private void setFlag(int flag, boolean enabled) {
        byte value = entityData.get(DATA_FLAGS);
        entityData.set(DATA_FLAGS, enabled ? (byte) (value | flag) : (byte) (value & ~flag));
    }

    private LivingEntity resolveTarget() {
        if (!(level instanceof ServerLevel server)) return null;
        Optional<UUID> id = entityData.get(DATA_TARGET);
        if (id.isEmpty()) return null;
        Entity entity = server.getEntity(id.get());
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private Player resolveOwner() {
        if (!(level instanceof ServerLevel server)) return null;
        Optional<UUID> id = entityData.get(DATA_OWNER);
        if (id.isEmpty()) return null;
        Entity entity = server.getEntity(id.get());
        return entity instanceof Player player ? player : null;
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (attackCooldown > 0) attackCooldown--;

        if (level.isClientSide) {
            clientParticles();
            return;
        }

        if (isInWaterOrBubble()) hurt(DamageSource.DROWN, 1.0F);
        LivingEntity target = resolveTarget();
        if (target == null || target == this || target instanceof TC4FireBatEntity
                || target instanceof Player targetPlayer && targetPlayer.getAbilities().instabuild) {
            targetlessTicks++;
            setDeltaMovement(getDeltaMovement().scale(0.8D).add(0.0D, 0.02D, 0.0D));
            // EntityFireBat damages a summoned bat every AI tick once its assigned target is gone.
            if (isSummoned()) hurt(DamageSource.MAGIC, 2.0F);
            return;
        }
        targetlessTicks = 0;

        Vec3 targetPoint = target.position().add(0.0D, target.getBbHeight() * 0.66D, 0.0D);
        Vec3 delta = targetPoint.subtract(position());
        if (delta.lengthSqr() > 1.0E-6D) {
            Vec3 desired = new Vec3(Math.signum(delta.x) * 0.5D, Math.signum(delta.y) * 0.7D, Math.signum(delta.z) * 0.5D);
            setDeltaMovement(getDeltaMovement().add(desired.subtract(getDeltaMovement()).scale(0.1D)));
            setYRot((float) (Math.atan2(getDeltaMovement().z, getDeltaMovement().x) * 180.0D / Math.PI) - 90.0F);
        }

        double reach = Math.max(2.5D, target.getBbWidth() * 1.1D);
        if (attackCooldown <= 0 && distanceToSqr(target) < reach * reach && getBoundingBox().maxY > target.getBoundingBox().minY && getBoundingBox().minY < target.getBoundingBox().maxY) {
            attackTarget(target);
            attackCooldown = 20;
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (isEffectiveAi() || isControlledByLocalInstance()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91D));
        } else {
            super.travel(input);
        }
    }

    private void attackTarget(LivingEntity target) {
        Player owner = resolveOwner();
        if (isSummoned() && owner != null) {
            // TC4's EntityUtils.setRecentlyHit(..., 100) makes summoned-bat kills count for the caster.
            target.setLastHurtByPlayer(owner);
        }
        if (isVampire()) {
            if (owner != null && !owner.hasEffect(MobEffects.REGENERATION)) {
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 26, 1));
            }
            heal(1.0F);
        }

        if ((isExplosive() || random.nextInt(10) == 0) && !isDevil()) {
            target.invulnerableTime = 0;
            float power = 1.5F + (isExplosive() ? potency() * 0.33F : 0.0F);
            level.explode(this, getX(), getY(), getZ(), power, false, Explosion.BlockInteraction.NONE);
            discard();
        } else if (isVampire() || random.nextBoolean()) {
            Vec3 oldMotion = target.getDeltaMovement();
            float damage = (isDevil() ? 3.0F : 2.0F) + potency();
            target.hurt(DamageSource.mobAttack(this), damage);
            target.setDeltaMovement(oldMotion);
        } else {
            target.setSecondsOnFire(isSummoned() ? 4 : 2);
        }
        playSound(SoundEvents.BAT_HURT, 0.5F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void clientParticles() {
        if (isExplosive()) {
            level.addParticle(ParticleTypes.END_ROD, getRandomX(0.2D), getRandomY(), getRandomZ(0.2D), 0.0D, 0.0D, 0.0D);
        }
        if (!isVampire()) {
            level.addParticle(ParticleTypes.SMOKE, getRandomX(0.4D), getRandomY(), getRandomZ(0.4D), 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, getRandomX(0.4D), getRandomY(), getRandomZ(0.4D), 0.0D, 0.0D, 0.0D);
        } else if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.DAMAGE_INDICATOR, getRandomX(0.3D), getRandomY(), getRandomZ(0.3D), 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isFire() || source.isExplosion() || super.isInvulnerableTo(source);
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.BAT_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.BAT_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.BAT_DEATH; }
    @Override
    protected float getSoundVolume() { return 0.1F; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("tc4Flags", entityData.get(DATA_FLAGS));
        tag.putInt("tc4Potency", potency());
        entityData.get(DATA_OWNER).ifPresent(uuid -> tag.putUUID("tc4Owner", uuid));
        entityData.get(DATA_TARGET).ifPresent(uuid -> tag.putUUID("tc4Target", uuid));
        tag.putInt("tc4AttackCooldown", attackCooldown);
        tag.putInt("tc4Targetless", targetlessTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_FLAGS, tag.getByte("tc4Flags"));
        entityData.set(DATA_POTENCY, tag.getInt("tc4Potency"));
        entityData.set(DATA_OWNER, tag.hasUUID("tc4Owner") ? Optional.of(tag.getUUID("tc4Owner")) : Optional.empty());
        entityData.set(DATA_TARGET, tag.hasUUID("tc4Target") ? Optional.of(tag.getUUID("tc4Target")) : Optional.empty());
        attackCooldown = tag.getInt("tc4AttackCooldown");
        targetlessTicks = tag.getInt("tc4Targetless");
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(isDevil() ? 15.0D : 5.0D);
    }
}
