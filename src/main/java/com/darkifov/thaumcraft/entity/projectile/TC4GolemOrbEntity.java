package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Stage216 Forge 1.19.2 adapter for TC4 EntityGolemOrb.
 *
 * Original parity notes:
 * - target id and red flag mirror the original spawn-data payload through entity-data/NBT
 * - zero gravity
 * - red lifetime 240, normal lifetime 160
 * - target homing uses TC4's distance-squared divisor and clamps motion to +/-0.25
 * - entity hit damage is thrower attackDamage * (red ? 1.0F : 0.6F)
 * - projectile can be deflected by attacker look vector
 */
public class TC4GolemOrbEntity extends Projectile {
    public static final String LIFE_TAG = "life";
    public static final String TARGET_TAG = "target";
    public static final String RED_TAG = "red";

    private int life = 0;
    private int targetId = -1;
    private LivingEntity target;
    private boolean red = false;

    public TC4GolemOrbEntity(EntityType<? extends TC4GolemOrbEntity> type, Level level) {
        super(type, level);
    }

    public TC4GolemOrbEntity(EntityType<? extends TC4GolemOrbEntity> type, Level level, LivingEntity owner, LivingEntity target, boolean red) {
        this(type, level);
        setOwner(owner);
        this.target = target;
        this.targetId = target == null ? -1 : target.getId();
        this.red = red;
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (target == null && targetId >= 0) {
            Entity entity = level.getEntity(targetId);
            if (entity instanceof LivingEntity living) {
                target = living;
            }
        }
        if (target != null && target.isAlive()) {
            double distSqr = Math.max(0.001D, distanceToSqr(target));
            double dx = (target.getX() - getX()) / distSqr;
            double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.6D - getY()) / distSqr;
            double dz = (target.getZ() - getZ()) / distSqr;
            Vec3 motion = getDeltaMovement().add(dx * 0.2D, dy * 0.2D, dz * 0.2D);
            setDeltaMovement(
                    Mth.clamp(motion.x, -0.25D, 0.25D),
                    Mth.clamp(motion.y, -0.25D, 0.25D),
                    Mth.clamp(motion.z, -0.25D, 0.25D)
            );
        }
        HitResult hit = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hit)) {
            onHit(hit);
        }
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.99D));
        if (level.isClientSide) {
            level.addParticle(red ? ParticleTypes.FLAME : ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
        if (!level.isClientSide && life > (red ? 240 : 160)) {
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
            if (hit instanceof EntityHitResult entityHit) {
                entityHit.getEntity().hurt(DamageSource.indirectMagic(this, owner), (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * (red ? 1.0F : 0.6F));
            }
            burst();
            discard();
        }
    }

    private void burst() {
        level.playSound(null, blockPosition(), TC4Sounds.event("shock"), SoundSource.HOSTILE, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 20, 0.35D, 0.25D, 0.35D, 0.08D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        Entity direct = source.getEntity();
        if (direct != null) {
            Vec3 look = direct.getLookAngle();
            if (look != null) {
                setDeltaMovement(look.x * 0.9D, look.y * 0.9D, look.z * 0.9D);
                level.playSound(null, blockPosition(), TC4Sounds.event("zap"), SoundSource.HOSTILE, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
                return true;
            }
        }
        return false;
    }

    public void tc4ShootAt(LivingEntity target, float velocity, float inaccuracy) {
        double dx = target.getX() + target.getDeltaMovement().x - getX();
        double dy = target.getY() - getY() - target.getBbHeight() / 2.0F;
        double dz = target.getZ() + target.getDeltaMovement().z - getZ();
        shoot(dx, dy, dz, velocity, inaccuracy);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(LIFE_TAG, life);
        tag.putInt(TARGET_TAG, targetId);
        tag.putBoolean(RED_TAG, red);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        life = tag.getInt(LIFE_TAG);
        targetId = tag.getInt(TARGET_TAG);
        red = tag.getBoolean(RED_TAG);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
