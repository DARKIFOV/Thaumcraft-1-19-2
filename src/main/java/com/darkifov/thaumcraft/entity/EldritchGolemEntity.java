package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import com.darkifov.thaumcraft.entity.projectile.TC4GolemOrbEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Stage215 1.19.2 port of TC4 EntityEldritchGolem boss semantics. */
public class EldritchGolemEntity extends TC4ThaumcraftBossEntity {
    public static final String HEADLESS_TAG = "headless";
    public static final String BEAM_CHARGE_TAG = "BeamCharge";
    public static final String CHARGING_BEAM_TAG = "ChargingBeam";

    private boolean headless = false;
    private int beamCharge = 0;
    private boolean chargingBeam = false;
    private int attackTimer = 0;
    private int arcing = 0;
    private BlockPos arcTarget = BlockPos.ZERO;

    public EldritchGolemEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 50;
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        setTc4Home(blockPosition(), 24);
        spawnTimer = 150;
        TC4ChampionModifierRuntime.makeChampion(this, true);
        generateName();
        return result;
    }

    @Override
    public void tick() {
        if (attackTimer > 0) {
            attackTimer--;
        }
        super.tick();
        if (level.isClientSide) {
            if (headless) {
                level.addParticle(ParticleTypes.SMOKE, getX() + (random.nextDouble() - 0.5D) * 0.6D, getEyeY() - 0.3D, getZ() + (random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.02D, 0.0D);
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, getX() + (random.nextDouble() - 0.5D) * 0.8D, getEyeY() - 0.75D, getZ() + (random.nextDouble() - 0.5D) * 0.8D, 0.0D, 0.01D, 0.0D);
            }
            if (arcing > 0) {
                arcing--;
            }
            return;
        }
        if (headless && beamCharge <= 0) {
            chargingBeam = true;
        }
        if (headless && chargingBeam) {
            beamCharge++;
            if (level instanceof ServerLevel serverLevel) {
                if (tickCount % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getEyeY(), getZ(), 6, 0.4D, 0.25D, 0.4D, 0.03D);
                }
                if (tickCount % 1 == 0) {
                    emitHeadlessArcLikeTC4(serverLevel);
                }
            }
            if (beamCharge >= 150) {
                chargingBeam = false;
            }
        }
        LivingEntity target = getTarget();
        if (headless && target != null && beamCharge > 0 && tickCount % 20 == 0 && hasLineOfSight(target)) {
            fireGolemBeam(target);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (attackTimer > 0) {
            return false;
        }
        attackTimer = 10;
        boolean result = target.hurt(DamageSource.mobAttack(this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
        if (result) {
            target.push(-Math.sin(getYRot() * Math.PI / 180.0D) * 1.5D, 0.2D, Math.cos(getYRot() * Math.PI / 180.0D) * 1.5D);
            playBossSound("golemattack", 0.8F, 1.0F);
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level.isClientSide && amount > getHealth() && !headless) {
            setHeadless(true);
            spawnTimer = 100;
            level.explode(this, getX(), getEyeY(), getZ(), 2.0F, false, Explosion.BlockInteraction.NONE);
            setHealth(Math.max(1.0F, getMaxHealth() * 0.20F));
            playBossSound("egattack", 1.0F, 0.75F);
            return false;
        }
        return super.hurt(source, amount);
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
        if (headless) {
            chargingBeam = true;
            if (level instanceof ServerLevel serverLevel) {
                ThaumcraftNetwork.sendEldritchBossFx(serverLevel, this, 18, 32.0D);
            }
        }
    }

    public int getBeamCharge() {
        return beamCharge;
    }

    public boolean isChargingBeam() {
        return chargingBeam;
    }

    public int getArcing() {
        return arcing;
    }

    public BlockPos getArcTarget() {
        return arcTarget;
    }

    public void clientStartArc(BlockPos target) {
        this.arcTarget = target == null ? BlockPos.ZERO : target;
        this.arcing = 10;
    }

    @Override
    public void generateName() {
        setCustomName(Component.translatable("entity.thaumcraft.eldritch_golem.champion", championModifierName()).withStyle(ChatFormatting.DARK_PURPLE));
        setCustomNameVisible(true);
    }

    private void fireGolemBeam(LivingEntity target) {
        beamCharge = Math.max(0, beamCharge - 15 - random.nextInt(5));
        if (level instanceof ServerLevel serverLevel) {
            TC4GolemOrbEntity blast = new TC4GolemOrbEntity(ThaumcraftMod.GOLEM_ORB.get(), level, this, target, false);
            double yaw = Math.toRadians(getYRot());
            blast.setPos(getX() - Math.sin(yaw), getEyeY() - 0.35D, getZ() + Math.cos(yaw));
            blast.tc4ShootAt(target, 0.66F, 5.0F);
            serverLevel.addFreshEntity(blast);
        }
        playBossSound("egattack", 1.0F, 1.0F + random.nextFloat() * 0.1F);
    }

    private void emitHeadlessArcLikeTC4(ServerLevel serverLevel) {
        if (arcing > 0) {
            arcing--;
            return;
        }
        float radius = 2.0F + random.nextFloat() * 2.0F;
        double radians = Math.toRadians(random.nextInt(360));
        int bx = Mth.floor(getX() + radius * Math.cos(radians));
        int by = Mth.floor(getY());
        int bz = Mth.floor(getZ() + radius * Math.sin(radians));
        BlockPos pos = new BlockPos(bx, by, bz);
        int c = 0;
        while (c < 5 && serverLevel.isEmptyBlock(pos)) {
            c++;
            pos = pos.below();
        }
        if (serverLevel.isEmptyBlock(pos.above()) && !serverLevel.isEmptyBlock(pos)) {
            arcTarget = pos;
            arcing = 8 + random.nextInt(5);
            ThaumcraftNetwork.sendEldritchBossBlockFx(serverLevel, this, 19, pos, 32.0D);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        TC4OuterLandsLootAdapter.dropBossDeathLoot(this, source, looting);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!level.isClientSide && horizontalCollision && random.nextInt(5) == 0) {
            BlockState state = level.getBlockState(blockPosition().relative(getDirection()));
            if (state.getDestroySpeed(level, blockPosition().relative(getDirection())) >= 0.0F && state.getDestroySpeed(level, blockPosition().relative(getDirection())) <= 0.15F) {
                level.destroyBlock(blockPosition().relative(getDirection()), true, this);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(HEADLESS_TAG, headless);
        tag.putInt(BEAM_CHARGE_TAG, beamCharge);
        tag.putBoolean(CHARGING_BEAM_TAG, chargingBeam);
        tag.putInt("Arcing", arcing);
        tag.putInt("ArcX", arcTarget.getX());
        tag.putInt("ArcY", arcTarget.getY());
        tag.putInt("ArcZ", arcTarget.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        headless = tag.getBoolean(HEADLESS_TAG);
        beamCharge = tag.getInt(BEAM_CHARGE_TAG);
        chargingBeam = tag.getBoolean(CHARGING_BEAM_TAG);
        arcing = tag.getInt("Arcing");
        arcTarget = new BlockPos(tag.getInt("ArcX"), tag.getInt("ArcY"), tag.getInt("ArcZ"));
        generateName();
    }
}
