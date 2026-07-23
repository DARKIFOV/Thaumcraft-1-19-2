package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** TC4's large crust-grown spore that periodically releases flying Taint Swarms. */
public final class TaintSporeSwarmerEntity extends TaintSporeEntity {
    private int spawnCounter = 500;
    private boolean releasing;

    public TaintSporeSwarmerEntity(EntityType<? extends TaintSporeSwarmerEntity> type, Level level) {
        super(type, level);
        setNoGravity(false);
        setSporeSize(10);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 75.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public int getSpawnCounter() {
        return spawnCounter;
    }

    private void setSpawnCounter(int value) {
        spawnCounter = Mth.clamp(value, 0, 500);
    }

    @Override
    protected void serverSporeTick() {
        setNoGravity(false);
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);

        int counter = Math.max(0, getSpawnCounter() - 1);
        setSpawnCounter(counter);
        if (counter == 0 && level.getNearestPlayer(this, 16.0D) != null) {
            setSpawnCounter(500);
            releaseSwarms(1);
        }
    }

    @Override
    protected void clientSporeTick() {
        super.clientSporeTick();
        setSpawnCounter(getSpawnCounter() - 1);
        int desired = Math.max(0, (500 - getSpawnCounter()) / 25);
        if (random.nextInt(Math.max(1, 22 - Math.min(20, desired))) == 0) {
            level.addParticle(ParticleTypes.WITCH,
                    getX() + random.nextGaussian() * 0.45D,
                    getY() + 0.5D + random.nextGaussian() * 0.35D,
                    getZ() + random.nextGaussian() * 0.45D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public void playerTouch(Player player) {
        // Unlike the small spore, touching the TC4 Swarmer does not burst it.
    }

    @Override
    protected void onLethalDamage() {
        releaseSwarms(1);
    }

    private void releaseSwarms(int amount) {
        if (level.isClientSide || releasing) return;
        releasing = true;
        playSound(TC4Sounds.event("gore"), 1.0F, 0.9F + random.nextFloat() * 0.1F);
        for (int i = 0; i < amount; i++) {
            TaintSwarmEntity swarm = ThaumcraftMod.TAINT_SWARM.get().create(level);
            if (swarm != null) {
                swarm.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(swarm);
            }
        }
        level.broadcastEntityEvent(this, (byte) 6);
        releasing = false;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 6) {
            setSpawnCounter(500);
            for (int i = 0; i < 25; i++) {
                level.addParticle(ParticleTypes.WITCH,
                        getX() + random.nextGaussian() * 0.55D,
                        getY() + 0.5D + random.nextGaussian() * 0.45D,
                        getZ() + random.nextGaussian() * 0.55D,
                        random.nextGaussian() * 0.03D,
                        random.nextGaussian() * 0.02D,
                        random.nextGaussian() * 0.03D);
            }
        } else if (event == 2) {
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.WITCH,
                        getX() + random.nextGaussian() * 0.45D,
                        getY() + 0.5D + random.nextGaussian() * 0.35D,
                        getZ() + random.nextGaussian() * 0.45D,
                        random.nextGaussian() * 0.02D,
                        random.nextGaussian() * 0.015D,
                        random.nextGaussian() * 0.02D);
            }
            super.handleEntityEvent(event);
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        Item tendril = ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_taint_tendril").get();
        for (int i = 0; i < 2; i++) {
            spawnAtLocation(new ItemStack(random.nextBoolean() ? ThaumcraftMod.TAINTED_SLIME.get() : tendril));
        }
    }

    @Override protected SoundEvent getAmbientSound() { return TC4Sounds.event("roots"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return TC4Sounds.event("gore"); }
    @Override protected SoundEvent getDeathSound() { return TC4Sounds.event("gore"); }
    @Override public int getAmbientSoundInterval() { return 200; }
    @Override public float getLightLevelDependentMagicValue() { return 1.0F; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpawnCounter", getSpawnCounter());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SpawnCounter")) setSpawnCounter(tag.getInt("SpawnCounter"));
        setSporeSize(10);
    }
}
