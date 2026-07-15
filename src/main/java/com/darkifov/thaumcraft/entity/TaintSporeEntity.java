package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Stationary TC4 taint spore grown by a mature age-4 fibre stalk. */
public class TaintSporeEntity extends Monster {
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(TaintSporeEntity.class, EntityDataSerializers.INT);
    private int growth;
    public float displaySize;
    private boolean bursting;

    public TaintSporeEntity(EntityType<? extends TaintSporeEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SIZE, 2);
    }

    public int getSporeSize() { return entityData.get(SIZE); }

    public void setSporeSize(int size) {
        entityData.set(SIZE, Math.max(2, Math.min(10, size)));
        xpReward = getSporeSize();
    }

    @Override public void tick() {
        super.tick();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (level.isClientSide) {
            if (displaySize < getSporeSize()) displaySize = Math.min(getSporeSize(), displaySize + 0.02F);
            return;
        }
        if (tickCount % 20 == 0 && level instanceof ServerLevel server && !TaintSpreadRuntime.isTaintedColumn(server, blockPosition())) {
            hurt(DamageSource.STARVE, 1.0F);
        }
        if (getSporeSize() < 10 && ++growth >= 1200) {
            setSporeSize(getSporeSize() + 1);
            growth = 0;
        }
        BlockPos stem = BlockPos.containing(getX(), getBoundingBox().minY - 0.05D, getZ()).below();
        BlockState state = level.getBlockState(stem);
        if (!state.is(ThaumcraftMod.TAINT_FIBRES.get()) || state.getValue(TaintFibresBlock.AGE) != 4 || isDeadOrDying()) {
            spiderBurst(stem);
        }
    }

    @Override public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!level.isClientSide) spiderBurst(blockPosition().below());
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        boolean lethal = amount >= getHealth();
        boolean result = super.hurt(source, amount);
        if (result && lethal && !level.isClientSide) spiderBurst(blockPosition().below());
        return result;
    }

    private void spiderBurst(BlockPos stem) {
        if (bursting || level.isClientSide) return;
        bursting = true;
        level.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F,
                0.9F + random.nextFloat() * 0.1F);
        int count = getSporeSize() / 3 + random.nextInt(getSporeSize() / 2 + 1);
        for (int i = 0; i < count; i++) {
            TaintCrawlerEntity spider = ThaumcraftMod.TAINT_CRAWLER.get().create(level);
            if (spider != null) {
                spider.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat(),
                        getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(spider);
            }
        }
        BlockState state = level.getBlockState(stem);
        if (state.is(ThaumcraftMod.TAINT_FIBRES.get()) && state.getValue(TaintFibresBlock.AGE) == 4) {
            level.setBlock(stem, state.setValue(TaintFibresBlock.AGE, 3), 3);
        }
        discard();
    }

    @Override protected void registerGoals() { }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) { }
    @Override public boolean removeWhenFarAway(double distance) { return false; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Size", getSporeSize() - 1);
        tag.putInt("Growth", growth);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSporeSize(tag.getInt("Size") + 1);
        growth = tag.getInt("Growth");
    }
}
