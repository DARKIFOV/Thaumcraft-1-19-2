package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.Comparator;

/**
 * Forge 1.19.2 adaptation of TC4 4.2.3.5 EntityThaumicSlime.
 *
 * Preserved contracts: 1/2/4 random start size, size-as-health/damage,
 * merge cap 100, ranged size-1 spit, sqrt(size) death split, NBT Size,
 * and the 1-in-3 small-slime Tainted Goo drop.
 */
public class TC4ThaumicSlimeEntity extends Slime {
    private static final int MAX_TC4_SIZE = 100;
    private int spitCounter = 100;
    private int launchedTicks = 10;
    private boolean tc4Initialized;

    public TC4ThaumicSlimeEntity(EntityType<? extends TC4ThaumicSlimeEntity> type, Level level) {
        super(type, level);
        xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        tc4Initialized = true;
        setTc4Size(1 << random.nextInt(3), true);
        return result;
    }

    @Override
    public void tick() {
        if (!tc4Initialized && !level.isClientSide) {
            tc4Initialized = true;
            setTc4Size(1 << random.nextInt(3), true);
        }
        super.tick();
        if (launchedTicks > 0) launchedTicks--;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }

        Player player = level.getNearestPlayer(this, 16.0D);
        if (player != null && !player.getAbilities().instabuild) {
            getLookControl().setLookAt(player, 10.0F, 20.0F);
            if (spitCounter > 0) spitCounter--;
            if (distanceTo(player) > 4.0F && spitCounter <= 0 && getTc4Size() > 3) {
                spitCounter = 101;
                spitAt(player);
                playSound(SoundEvents.SLIME_SQUISH, 1.0F,
                        ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
                setTc4Size(getTc4Size() - 1, true);
            }
            return;
        }

        TC4ThaumicSlimeEntity target = getClosestMergeableSlime();
        if (target != null) {
            getLookControl().setLookAt(target, 10.0F, 20.0F);
            if (distanceTo(target) < getBbWidth() + target.getBbWidth()) {
                target.setTc4Size(Math.min(MAX_TC4_SIZE, target.getTc4Size() + getTc4Size()), true);
                discard();
            }
        }
    }

    private void spitAt(Player target) {
        if (!(level instanceof ServerLevel server)) return;
        TC4ThaumicSlimeEntity spit = ThaumcraftMod.THAUMIC_SLIME.get().create(server);
        if (spit == null) return;
        spit.tc4Initialized = true;
        spit.setTc4Size(1, true);
        spit.launchedTicks = 10;

        Vec3 origin = position().add(0.0D, getBbHeight() * 0.5D, 0.0D);
        Vec3 aim = target.position().add(0.0D, target.getBbHeight() / 3.0D, 0.0D).subtract(origin);
        double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
        Vec3 velocity = aim.add(0.0D, horizontal * 0.2D, 0.0D).normalize().scale(1.5D)
                .add(random.nextGaussian() * 0.0075D,
                        random.nextGaussian() * 0.0075D,
                        random.nextGaussian() * 0.0075D);
        spit.moveTo(origin.x, origin.y, origin.z,
                (float) (Math.atan2(aim.z, aim.x) * 180.0D / Math.PI) - 90.0F,
                (float) (-(Math.atan2(aim.y, Math.max(1.0E-7D, horizontal)) * 180.0D / Math.PI)));
        spit.setDeltaMovement(velocity);
        server.addFreshEntity(spit);
    }

    private TC4ThaumicSlimeEntity getClosestMergeableSlime() {
        AABB search = getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
        return level.getEntitiesOfClass(TC4ThaumicSlimeEntity.class, search,
                        slime -> slime != this && slime.tickCount > 100 && slime.getTc4Size() < MAX_TC4_SIZE)
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    public int getTc4Size() {
        return getSize();
    }

    public void setTc4Size(int size, boolean heal) {
        int clamped = Math.max(1, Math.min(MAX_TC4_SIZE, size));
        super.setSize(clamped, false);
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(clamped);
        }
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(clamped);
        }
        // Vanilla Slime#setSize scales movement speed linearly with size; TC4 drove
        // movement through its jump controller instead, so keep the modern attribute
        // bounded while preserving the original size-scaled body and attack.
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.20D);
        }
        xpReward = Math.max(1, (int) Math.sqrt(clamped));
        if (heal) setHealth(getMaxHealth());
        else setHealth(Math.min(getHealth(), getMaxHealth()));
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float root = (float) Math.sqrt(Math.max(1, getTc4Size()));
        float side = 0.25F * root + 0.25F;
        return EntityDimensions.scalable(side, side);
    }

    @Override
    public void playerTouch(Player player) {
        if (!isAlive()) return;
        int reachScale = Math.max(1, (int) Math.sqrt(getTc4Size()));
        if (launchedTicks > 0 && reachScale == 2) reachScale = 3;
        double reach = 0.8D * reachScale * 0.8D * reachScale;
        if (distanceTo(player) < reach
                && player.hurt(DamageSource.mobAttack(this), getTc4Size())) {
            playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        int oldSize = getTc4Size();
        int children = (int) Math.sqrt(oldSize);
        boolean split = reason == Entity.RemovalReason.KILLED
                && !level.isClientSide && children > 1 && isDeadOrDying();
        if (split && level instanceof ServerLevel server) {
            for (int i = 0; i < children; i++) {
                TC4ThaumicSlimeEntity child = ThaumcraftMod.THAUMIC_SLIME.get().create(server);
                if (child == null) continue;
                child.tc4Initialized = true;
                child.setTc4Size(1, true);
                float offsetX = ((float) (i % 2) - 0.5F) * oldSize / 4.0F;
                float offsetZ = ((float) (i / 2) - 0.5F) * oldSize / 4.0F;
                child.moveTo(getX() + offsetX, getY() + 0.5D, getZ() + offsetZ,
                        random.nextFloat() * 360.0F, 0.0F);
                server.addFreshEntity(child);
            }
            // Prevent vanilla Slime.remove from also creating 2–4 half-size children.
            super.setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (getTc4Size() < 3 && random.nextInt(3) == 0) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.TAINTED_SLIME.get()));
        }
        super.dropCustomDeathLoot(source, looting, recentlyHit);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Size", getTc4Size() - 1);
        tag.putInt("TC4SpitCounter", spitCounter);
        tag.putInt("TC4Launched", launchedTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tc4Initialized = true;
        setTc4Size(tag.getInt("Size") + 1, false);
        if (tag.contains("TC4SpitCounter")) spitCounter = tag.getInt("TC4SpitCounter");
        if (tag.contains("TC4Launched")) launchedTicks = tag.getInt("TC4Launched");
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F * (float) Math.sqrt(getTc4Size());
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }
}
