package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Forge 1.19.2 port of TC4's EntityInhabitedZombie.
 *
 * The host is a 30-health cultist-armoured zombie. Its death immediately
 * releases a helmeted Eldritch Crab, which is the original Outer Lands
 * lifecycle rather than a normal zombie loot path.
 */
public class InhabitedZombieEntity extends Zombie {
    private boolean releasedCrab;

    public InhabitedZombieEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CrimsonCultistEntity.class, true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data,
                                        @Nullable CompoundTag tag) {
        // TC4 deliberately does not call EntityZombie's spawn finalizer here:
        // the husk is always adult and receives only the cultist plate below.
        float armourChance = level.getDifficulty() == Difficulty.HARD ? 0.9F : 0.6F;
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ThaumcraftMod.CRIMSON_PLATE_HELM.get()));
        setDropChance(EquipmentSlot.HEAD, 0.0F);
        if (random.nextFloat() <= armourChance) {
            setItemSlot(EquipmentSlot.CHEST, new ItemStack(ThaumcraftMod.CRIMSON_PLATE_CHEST.get()));
            setDropChance(EquipmentSlot.CHEST, 0.0F);
        }
        if (random.nextFloat() <= armourChance) {
            setItemSlot(EquipmentSlot.LEGS, new ItemStack(ThaumcraftMod.CRIMSON_PLATE_LEGS.get()));
            setDropChance(EquipmentSlot.LEGS, 0.0F);
        }
        return data;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // EntityInhabitedZombie intentionally has no direct item drops.
    }

    @Override
    protected void tickDeath() {
        if (!releasedCrab && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            releasedCrab = true;
            EldritchCrabEntity crab = ThaumcraftMod.ELDRITCH_CRAB.get().create(serverLevel);
            if (crab != null) {
                crab.moveTo(getX(), getY() + getEyeHeight(), getZ(), getYRot(), getXRot());
                crab.setHelm(true);
                serverLevel.addFreshEntity(crab);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    getX(), getY() + getBbHeight() * 0.5D, getZ(),
                    20, getBbWidth() * 0.5D, getBbHeight() * 0.5D,
                    getBbWidth() * 0.5D, 0.02D);
            // TC4 discards the host in the same death update. Jump to the
            // vanilla removal tick so XP is still awarded once.
            deathTime = 19;
        }
        super.tickDeath();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TC4Sounds.event("crabtalk");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true;
    }

    public static boolean checkInhabitedZombieSpawnRules(EntityType<InhabitedZombieEntity> type,
                                                          ServerLevelAccessor level,
                                                          MobSpawnType reason,
                                                          BlockPos pos,
                                                          net.minecraft.util.RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        AABB area = new AABB(pos).inflate(32.0D, 16.0D, 32.0D);
        return serverLevel.getEntitiesOfClass(InhabitedZombieEntity.class, area).isEmpty();
    }
}
