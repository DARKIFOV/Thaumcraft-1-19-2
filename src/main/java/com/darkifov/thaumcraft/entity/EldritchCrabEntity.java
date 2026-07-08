package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/** Stage220 1.19.2 port of original EntityEldritchCrab. */
public class EldritchCrabEntity extends Monster {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EldritchCrabEntity.class, EntityDataSerializers.BYTE);
    private static final byte HELM_BIT = 0x1;

    public EldritchCrabEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData() { super.defineSynchedData(); entityData.define(FLAGS, (byte)0); }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.63F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CrimsonCultistEntity.class, true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        setHelm(level.getDifficulty() == Difficulty.HARD || random.nextFloat() < 0.33F);
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount < 20) fallDistance = 0.0F;
        LivingEntity target = getTarget();
        if (!level.isClientSide && getVehicle() == null && target != null && target.getVehicle() == null && !onGround && !hasHelm()
                && !target.isRemoved() && getY() - target.getY() >= target.getBbHeight() / 2.0F && distanceToSqr(target) < 4.0D) {
            startRiding(target, true);
        }
        if (!level.isClientSide && getVehicle() instanceof LivingEntity living && tickCount % (10 + random.nextInt(10)) == 0) {
            doHurtTarget(living);
            if (random.nextFloat() < 0.2F) stopRiding();
        }
    }

    @Override
    public double getMyRidingOffset() { return getVehicle() != null ? 0.5D : 0.0D; }

    @Override
    public int getArmorValue() { return hasHelm() ? 5 : 0; }

    public boolean hasHelm() { return (entityData.get(FLAGS) & HELM_BIT) != 0; }

    public void setHelm(boolean helm) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, helm ? (byte)(flags | HELM_BIT) : (byte)(flags & ~HELM_BIT));
        refreshHelmAttributes();
    }

    private void refreshHelmAttributes() {
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(hasHelm() ? 0.275D : 0.30D);
        if (getAttribute(Attributes.ARMOR) != null) getAttribute(Attributes.ARMOR).setBaseValue(hasHelm() ? 5.0D : 0.0D);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (hit) playSound(TC4Sounds.event("crabclaw"), 1.0F, 0.9F + random.nextFloat() * 0.2F);
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!level.isClientSide && result && hasHelm() && getHealth() / getMaxHealth() <= 0.5F) {
            setHelm(false);
            spawnAtLocation(new ItemStack(ThaumcraftMod.CRIMSON_PLATE_CHEST.get(), 1)); // Stage221: original TC4 ConfigItems.itemChestCultistPlate
        }
        return result;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (recentlyHit && (random.nextInt(3) == 0 || random.nextInt(1 + looting) > 0)) spawnAtLocation(new ItemStack(Items.EMERALD, 1));
    }

    @Override protected SoundEvent getAmbientSound() { return TC4Sounds.event("crabtalk"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return TC4Sounds.event("crabdeath"); }
    @Override public int getAmbientSoundInterval() { return 160; }
    @Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return true; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putByte("Flags", entityData.get(FLAGS)); }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); entityData.set(FLAGS, tag.getByte("Flags")); refreshHelmAttributes(); }

    public static boolean checkEldritchCrabSpawnRules(EntityType<EldritchCrabEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
