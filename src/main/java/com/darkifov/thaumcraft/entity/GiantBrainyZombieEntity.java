package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * TC4 giant brainy zombie. Damage makes it angrier: its visual/collision scale
 * rises from 1.0 to 2.0 while attack damage climbs from 7 to 12, then decays by
 * 0.002 per tick back toward one.
 */
public class GiantBrainyZombieEntity extends BrainyZombieEntity {
    private static final EntityDataAccessor<Float> ANGER =
            SynchedEntityData.defineId(GiantBrainyZombieEntity.class, EntityDataSerializers.FLOAT);
    /* Avoid reading SynchedEntityData before defineSynchedData during the superclass constructor. */
    private float collisionAnger = 1.0F;

    public GiantBrainyZombieEntity(EntityType<? extends GiantBrainyZombieEntity> type, Level level) {
        super(type, level);
        xpReward = 15;
        collisionAnger = 1.0F;
        refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BrainyZombieEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANGER, 1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
    }

    public float getAnger() {
        return entityData.get(ANGER);
    }

    public void setAnger(float anger) {
        float clamped = Math.max(1.0F, Math.min(2.0F, anger));
        if (Math.abs(clamped - getAnger()) > 0.0001F) {
            collisionAnger = clamped;
            entityData.set(ANGER, clamped);
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // EntityGiantBrainyZombie#setSize used 1.2 + anger for collision.
        float safeAnger = collisionAnger <= 0.0F ? 1.0F : collisionAnger;
        return super.getDimensions(pose).scale(1.2F + safeAnger);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ANGER.equals(key)) {
            collisionAnger = entityData.get(ANGER);
            refreshDimensions();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level.isClientSide) {
            if (getAnger() > 1.0F) {
                setAnger(getAnger() - 0.002F);
            }
            if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0D + (getAnger() - 1.0F) * 5.0D);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level.isClientSide) {
            setAnger(getAnger() + 0.1F);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        for (int roll = 0; roll < 12; roll++) {
            if (random.nextBoolean()) {
                spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 2));
            }
        }
        if (random.nextInt(10) - looting <= 4) {
            var brain = ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_brain");
            if (brain != null) {
                spawnAtLocation(new ItemStack(brain.get()));
            }
        }

        // EntityLivingBase's old rare-drop gate was 5/200 plus Looting.
        if (random.nextInt(200) - looting < 5) {
            Item rare = switch (random.nextInt(4)) {
                case 0 -> ThaumcraftMod.THAUMIUM_INGOT.get();
                case 1 -> Items.CARROT;
                case 2 -> Items.POTATO;
                default -> ThaumcraftMod.AMBER.get();
            };
            spawnAtLocation(new ItemStack(rare));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("TC4Anger", getAnger());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAnger(tag.contains("TC4Anger") ? tag.getFloat("TC4Anger") : 1.0F);
    }
}
