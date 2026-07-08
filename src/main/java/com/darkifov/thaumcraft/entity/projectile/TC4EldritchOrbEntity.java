package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Stage216 Forge 1.19.2 adapter for TC4 EntityEldritchOrb.
 *
 * Original 1.7.10 semantics preserved here:
 * - zero gravity
 * - expires at 100 ticks
 * - on impact, damages non-undead living entities in radius 2 around the orb
 * - damage is thrower attackDamage * 0.666F
 * - applies Wither for 160 ticks
 * - emits the TC4 byte-16 wisp burst via a dedicated client FX packet
 */
public class TC4EldritchOrbEntity extends Projectile {
    public static final String LIFE_TAG = "life";
    private int life = 0;

    public TC4EldritchOrbEntity(EntityType<? extends TC4EldritchOrbEntity> type, Level level) {
        super(type, level);
        noPhysics = false;
    }

    public TC4EldritchOrbEntity(EntityType<? extends TC4EldritchOrbEntity> type, Level level, LivingEntity owner) {
        this(type, level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
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
        setDeltaMovement(getDeltaMovement().scale(0.99D));
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, getX(), getY(), getZ(), random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D, random.nextGaussian() * 0.01D);
        }
        if (!level.isClientSide && life > 100) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level.isClientSide && getOwner() instanceof LivingEntity owner) {
            burst(owner);
            discard();
        }
    }

    private void burst(LivingEntity owner) {
        float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.666F;
        for (Entity entity : level.getEntities(owner, getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity && e.isAlive())) {
            if (entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
                living.hurt(DamageSource.indirectMagic(this, owner), damage);
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 0));
            }
        }
        level.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            ThaumcraftNetwork.sendEldritchOrbBurst(serverLevel, this, 32.0D);
        }
    }

    public void tc4ShootAt(LivingEntity target, float velocity, float inaccuracy) {
        double dx = target.getX() + target.getDeltaMovement().x - getX();
        double dy = target.getY() - getY() - target.getBbHeight() / 2.0F;
        double dz = target.getZ() + target.getDeltaMovement().z - getZ();
        shoot(dx, dy, dz, velocity, inaccuracy);
        setYRot((float)(Mth.atan2(getDeltaMovement().x, getDeltaMovement().z) * 180.0D / Math.PI));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(LIFE_TAG, life);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        life = tag.getInt(LIFE_TAG);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
