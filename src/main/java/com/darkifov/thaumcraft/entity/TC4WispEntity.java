package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.WispEssenceItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** TC4 4.2.3.5 Wisp behavior adapted to Forge 1.19.2. */
public class TC4WispEntity extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<String> DATA_ASPECT =
            SynchedEntityData.defineId(TC4WispEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_ATTACK =
            SynchedEntityData.defineId(TC4WispEntity.class, EntityDataSerializers.INT);

    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private int courseChangeCooldown;
    private int aggroCooldown;
    private int attackCounter;

    public TC4WispEntity(EntityType<? extends TC4WispEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FLYING_SPEED, 0.60D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_ASPECT, "");
        entityData.define(DATA_ATTACK, 0);
    }

    @Override
    protected void registerGoals() {
        // TC4 Wisp uses direct flight steering rather than pathfinding goals.
    }

    public Aspect getAspect() {
        return Aspect.byId(entityData.get(DATA_ASPECT));
    }

    public void setAspect(Aspect aspect) {
        entityData.set(DATA_ASPECT, aspect == null ? "" : aspect.id());
    }

    public int getAttackCounter() {
        return entityData.get(DATA_ATTACK);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        setNoGravity(true);
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }
        if (getAspect() == null) chooseAspect();
        steerFlight();
        updateTargetAndAttack();
        entityData.set(DATA_ATTACK, attackCounter);
    }

    private void chooseAspect() {
        List<Aspect> pool = new ArrayList<>();
        String biome = level.getBiome(blockPosition()).unwrapKey()
                .map(key -> key.location().getPath()).orElse("");
        if (biome.contains("eerie")) {
            pool.addAll(List.of(Aspect.TENEBRAE, Aspect.EXANIMIS, Aspect.PERDITIO,
                    Aspect.ALIENIS, Aspect.VENENUM, Aspect.MORTUUS));
        } else {
            // Original weighting: 90% primal, 10% compound.
            boolean primal = random.nextInt(10) != 0;
            for (Aspect aspect : Aspect.values()) {
                if (aspect.isPrimal() == primal) pool.add(aspect);
            }
        }
        if (!pool.isEmpty()) setAspect(pool.get(random.nextInt(pool.size())));
    }

    private void steerFlight() {
        double dx = waypointX - getX();
        double dy = waypointY - getY();
        double dz = waypointZ - getZ();
        double distanceSq = dx * dx + dy * dy + dz * dz;
        if (distanceSq < 1.0D || distanceSq > 3600.0D) {
            waypointX = getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            waypointY = getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            waypointZ = getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            dx = waypointX - getX();
            dy = waypointY - getY();
            dz = waypointZ - getZ();
            distanceSq = dx * dx + dy * dy + dz * dz;
        }
        if (courseChangeCooldown-- <= 0) {
            courseChangeCooldown += random.nextInt(5) + 2;
            double distance = Math.sqrt(distanceSq);
            if (distance > 0.0D && isCourseTraversable(dx, dy, dz, distance)) {
                setDeltaMovement(getDeltaMovement().add(dx / distance * 0.1D,
                        dy / distance * 0.1D, dz / distance * 0.1D));
            } else {
                waypointX = getX();
                waypointY = getY();
                waypointZ = getZ();
            }
        }
    }

    private boolean isCourseTraversable(double dx, double dy, double dz, double distance) {
        Vec3 step = new Vec3(dx, dy, dz).scale(1.0D / distance);
        AABB box = getBoundingBox();
        for (int i = 1; i < distance; i++) {
            box = box.move(step);
            if (!level.noCollision(this, box)) return false;
        }
        return true;
    }

    private void updateTargetAndAttack() {
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || distanceToSqr(target) > 256.0D)) {
            setTarget(null);
            target = null;
        }
        if (target == null && aggroCooldown-- <= 0) {
            aggroCooldown = 20;
            Player player = level.getNearestPlayer(this, 16.0D);
            if (player != null && !player.getAbilities().instabuild) {
                setTarget(player);
                target = player;
            }
        }
        if (target == null) {
            attackCounter = Math.max(0, attackCounter - 1);
            return;
        }

        double distanceSq = distanceToSqr(target);
        if (distanceSq < 4.0D) {
            if (attackCounter == 20) zap(target);
            attackCounter = attackCounter >= 30 ? -20 : attackCounter + 1;
        } else if (distanceSq < 256.0D) {
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
            if (attackCounter < 0) attackCounter++;
            else attackCounter = Math.max(0, attackCounter - 1);
        }
    }

    private void zap(LivingEntity target) {
        float damage = random.nextInt(4) == 0 ? 4.0F : 3.0F;
        target.hurt(DamageSource.mobAttack(this), damage);
        playSound(TC4Sounds.event("zap"), 1.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public void travel(Vec3 input) {
        if (isEffectiveAi() || isControlledByLocalInstance()) {
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91D));
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        Aspect aspect = getAspect();
        if (aspect != null) {
            ItemStack essence = WispEssenceItem.withAspect(
                    new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_wispessence").get()), aspect, 2);
            spawnAtLocation(essence);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return TC4Sounds.event("wisplive"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return TC4Sounds.event("wisplive"); }
    @Override protected SoundEvent getDeathSound() { return TC4Sounds.event("wispdead"); }
    @Override protected float getSoundVolume() { return 0.25F; }
    @Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return true; }
    @Override public int getMaxSpawnClusterSize() { return 1; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Aspect aspect = getAspect();
        if (aspect != null) tag.putString("Type", aspect.id());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Type")) setAspect(Aspect.byId(tag.getString("Type")));
    }

    public static boolean checkWispSpawnRules(EntityType<TC4WispEntity> type,
                                               ServerLevelAccessor level,
                                               MobSpawnType reason,
                                               BlockPos pos,
                                               RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random)
                || !Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        int nearby = level.getEntitiesOfClass(TC4WispEntity.class,
                new AABB(pos).inflate(16.0D)).size();
        return nearby < 8;
    }
}
