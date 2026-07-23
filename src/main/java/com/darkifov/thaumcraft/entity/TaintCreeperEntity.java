package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * TC4 EntityTaintCreeper. Its 30-tick fuse makes a small non-block-breaking
 * blast, poisons nearby non-tainted living entities, and marks ten nearby
 * columns as tainted instead of using vanilla Creeper destruction.
 */
public final class TaintCreeperEntity extends Monster implements TaintedMob {
    private static final int FUSE_TIME = 30;
    private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(
            TaintCreeperEntity.class, EntityDataSerializers.INT);
    private int oldFuse;

    public TaintCreeperEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FUSE, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override public void tick() {
        super.tick();
        TaintedMobSupport.tickBirthParticles(this);
        oldFuse = getFuse();
        if (level.isClientSide) return;

        LivingEntity target = getTarget();
        boolean swell = target != null && target.isAlive() && distanceToSqr(target) < 9.0D
                && getSensing().hasLineOfSight(target);
        int nextFuse = swell ? getFuse() + 1 : Math.max(0, getFuse() - 1);
        if (swell && getFuse() == 0) playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
        setFuse(nextFuse);
        if (nextFuse >= FUSE_TIME) explodeTaint();
    }

    private int getFuse() { return entityData.get(FUSE); }
    private void setFuse(int value) { entityData.set(FUSE, value); }
    private void explodeTaint() {
        level.explode(this, getX(), getY() + getBbHeight() * 0.5D, getZ(),
                1.5F, false, Explosion.BlockInteraction.NONE);
        AABB area = getBoundingBox().inflate(6.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area,
                living -> living.isAlive() && !(living instanceof TaintedMob)
                        && living.getMobType() != MobType.UNDEAD)) {
            living.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), 100, 0, false, true));
        }

        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            int baseX = (int) getX();
            int baseY = (int) getY();
            int baseZ = (int) getZ();
            for (int i = 0; i < 10; i++) {
                int x = baseX + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
                int z = baseZ + (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
                BlockPos pos = new BlockPos(x, baseY, z);
                if (!random.nextBoolean() || TaintSpreadRuntime.isColumnTainted(server, pos)) continue;
                TaintSpreadRuntime.markTaintedColumn(server, pos);
                BlockState replaced = server.getBlockState(pos);
                if ((replaced.isAir() || replaced.getMaterial().isReplaceable())
                        && server.getBlockState(pos.below()).isFaceSturdy(server, pos.below(), net.minecraft.core.Direction.UP)) {
                    BlockState fibres = TaintFibresBlock.withAttachments(server, pos,
                            ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState().setValue(TaintFibresBlock.AGE, 0));
                    if (TaintFibresBlock.hasAttachment(fibres)) server.setBlock(pos, fibres, 3);
                }
            }
        }
        discard();
    }

    public float getTc4FlashIntensity(float partialTick) {
        return (oldFuse + (getFuse() - oldFuse) * partialTick) / 28.0F;
    }

    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(TaintedMobSupport.randomTaintResource(random));
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short) FUSE_TIME);
        tag.putInt("TC4Fuse", getFuse());
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TC4Fuse")) setFuse(tag.getInt("TC4Fuse"));
    }
}
