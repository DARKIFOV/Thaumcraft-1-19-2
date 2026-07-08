package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.projectile.TC4EldritchOrbEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/** Stage215 1.19.2 port of TC4 EntityEldritchWarden boss semantics. */
public class EldritchWardenEntity extends TC4ThaumcraftBossEntity {
    public static final String TITLE_TAG = "title";
    public static final String FRENZY_TAG = "Frenzy";
    public static final String FRENZY_COUNTER_TAG = "FrenzyCounter";
    public static final String[] TITLES = new String[] {
            "Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"
    };

    private int titleIndex = 0;
    private boolean frenzy = false;
    private int frenzyCounter = 0;
    private int rangedCooldown = 0;
    private boolean lastBlast = false;
    private float armLiftL = 0.0F;
    private float armLiftR = 0.0F;

    public EldritchWardenEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 50;
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CrimsonCultistEntity.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        setTc4Home(blockPosition(), 24);
        spawnTimer = 150;
        titleIndex = random.nextInt(TITLES.length);
        setAbsorptionAmount((float)(getMaxHealth() * 0.66D));
        TC4ChampionModifierRuntime.makeChampion(this, true);
        generateName();
        return result;
    }

    @Override
    public void tick() {
        if (rangedCooldown > 0) {
            rangedCooldown--;
        }
        if (level.isClientSide) {
            if (armLiftL > 0.0F) {
                armLiftL = Math.max(0.0F, armLiftL - 0.05F);
            }
            if (armLiftR > 0.0F) {
                armLiftR = Math.max(0.0F, armLiftR - 0.05F);
            }
        }
        super.tick();

        if (level.isClientSide) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, getRandomX(0.25D), getY() + 0.25D + random.nextDouble() * getBbHeight() * 0.65D, getRandomZ(0.25D), 0.0D, 0.01D, 0.0D);
            return;
        }

        if (tickCount % 120 == 0) {
            playBossSound("egidle", 0.45F, 0.85F + random.nextFloat() * 0.2F);
        }

        if (frenzyCounter > 0) {
            if (frenzyCounter == 150) {
                teleportHomeLikeTC4();
            }
            performFieldFrenzyLikeTC4();
            frenzyCounter--;
        }

        if (invulnerableTime <= 0 && tickCount % 25 == 0) {
            float cap = (float)(getMaxHealth() * 0.66D);
            if (getAbsorptionAmount() < cap) {
                setAbsorptionAmount(Math.min(cap, getAbsorptionAmount() + 1.0F));
            }
        }

        LivingEntity target = getTarget();
        if (target != null && rangedCooldown <= 0 && distanceToSqr(target) > 12.0D && hasLineOfSight(target)) {
            doTc4RangedAttack(target);
            rangedCooldown = frenzy ? 18 : 40;
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean result = super.doHurtTarget(entity);
        if (result && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
            if (living instanceof Player player && random.nextFloat() < 0.25F) {
                PlayerThaumData.addWarpTemporary(player, 1);
            }
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL || source == DamageSource.DROWN) {
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (!level.isClientSide && result && !frenzy && getAbsorptionAmount() <= 0.0F && getHealth() < getMaxHealth() * 0.33F) {
            frenzy = true;
            frenzyCounter = 150;
            level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.75F, 0.65F);
        }
        return result;
    }

    @Override
    public void generateName() {
        Component modifier = championModifierName();
        setCustomName(Component.translatable("entity.thaumcraft.eldritch_warden.champion", Component.literal(getTitle()).withStyle(ChatFormatting.DARK_PURPLE), modifier).withStyle(ChatFormatting.DARK_PURPLE));
        setCustomNameVisible(true);
    }

    public String getTitle() {
        int safe = Math.max(0, Math.min(TITLES.length - 1, titleIndex));
        return TITLES[safe];
    }

    private void doTc4RangedAttack(LivingEntity target) {
        if (random.nextFloat() <= 0.2F) {
            doTc4SonicAttack(target);
            return;
        }
        lastBlast = !lastBlast;
        int status = lastBlast ? 16 : 15;
        if (lastBlast) {
            armLiftR = 1.0F;
        } else {
            armLiftL = 1.0F;
        }
        if (level instanceof ServerLevel serverLevel) {
            ThaumcraftNetwork.sendEldritchBossFx(serverLevel, this, status, 32.0D);
            TC4EldritchOrbEntity blast = new TC4EldritchOrbEntity(ThaumcraftMod.ELDRITCH_ORB.get(), level, this);
            int rr = lastBlast ? 90 : 180;
            double xx = Math.cos(Math.toRadians((getYRot() + rr) % 360.0F)) * 0.5D;
            double zz = Math.sin(Math.toRadians((getYRot() + rr) % 360.0F)) * 0.5D;
            blast.setPos(blast.getX() - xx, blast.getY() - 0.13D, blast.getZ() - zz);
            blast.tc4ShootAt(target, 1.0F, 2.0F);
            serverLevel.addFreshEntity(blast);
        }
        playBossSound("egattack", 2.0F, 1.0F + random.nextFloat() * 0.1F);
    }

    private void doTc4SonicAttack(LivingEntity target) {
        if (!hasLineOfSight(target)) {
            return;
        }
        target.push(-Math.sin(getYRot() * Math.PI / 180.0D) * 1.5D, 0.1D, Math.cos(getYRot() * Math.PI / 180.0D) * 1.5D);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
        if (target instanceof Player player) {
            PlayerThaumData.addWarpTemporary(player, 3 + random.nextInt(3));
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getEyeY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        playBossSound("zap", 1.0F, 0.9F + random.nextFloat() * 0.1F);
    }

    private void performFieldFrenzyLikeTC4() {
        if (!(level instanceof ServerLevel serverLevel) || frenzyCounter >= 121 || frenzyCounter % 10 != 0) {
            return;
        }
        double radius = (150 - frenzyCounter) / 8.0D;
        int stepDivisor = 1 + frenzyCounter / 8;
        BlockPos center = blockPosition();
        for (int q = 0; q < 180 / stepDivisor; q++) {
            double radians = Math.toRadians(q * 2 * stepDivisor);
            int dx = (int)(radius * Math.cos(radians));
            int dz = (int)(radius * Math.sin(radians));
            BlockPos pos = center.offset(dx, 0, dz);
            if (serverLevel.isEmptyBlock(pos) && serverLevel.getBlockState(pos.below()).isSolidRender(serverLevel, pos.below())) {
                ThaumcraftNetwork.sendEldritchBossBlockFx(serverLevel, this, 17, pos, 32.0D);
                if (random.nextFloat() < 0.10F) {
                    serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D, 4, 0.3D, 0.2D, 0.3D, 0.02D);
                }
            }
        }
        playBossSound("zap", 1.0F, 0.9F + random.nextFloat() * 0.1F);
    }

    private void teleportHomeLikeTC4() {
        CompoundTag tag = getPersistentData();
        if (!tag.contains(TC4ThaumcraftBossEntity.HOME_D_TAG)) {
            return;
        }
        double ox = getX();
        double oy = getY();
        double oz = getZ();
        BlockPos home = new BlockPos(tag.getInt(TC4ThaumcraftBossEntity.HOME_X_TAG), tag.getInt(TC4ThaumcraftBossEntity.HOME_Y_TAG), tag.getInt(TC4ThaumcraftBossEntity.HOME_Z_TAG));
        for (int tries = 0; tries < 20; tries++) {
            BlockPos pos = home.offset(random.nextInt(8) - random.nextInt(8), 0, random.nextInt(8) - random.nextInt(8));
            if (level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && level.isEmptyBlock(pos)) {
                teleportTo(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, ox, oy + getBbHeight() * 0.5D, oz, 64, getBbWidth(), getBbHeight() * 0.5D, getBbWidth(), 0.15D);
                    serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY() + getBbHeight() * 0.5D, getZ(), 64, getBbWidth(), getBbHeight() * 0.5D, getBbWidth(), 0.15D);
                }
                return;
            }
        }
    }

    public float getArmLiftL() {
        return armLiftL;
    }

    public float getArmLiftR() {
        return armLiftR;
    }

    public void clientArmLift(boolean left) {
        if (left) {
            this.armLiftL = 1.0F;
        } else {
            this.armLiftR = 1.0F;
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        TC4OuterLandsLootAdapter.dropBossDeathLoot(this, source, looting);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte(TITLE_TAG, (byte)titleIndex);
        tag.putBoolean(FRENZY_TAG, frenzy);
        tag.putInt(FRENZY_COUNTER_TAG, frenzyCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        titleIndex = Math.floorMod(tag.getByte(TITLE_TAG), TITLES.length);
        frenzy = tag.getBoolean(FRENZY_TAG);
        frenzyCounter = tag.getInt(FRENZY_COUNTER_TAG);
        generateName();
    }
}
