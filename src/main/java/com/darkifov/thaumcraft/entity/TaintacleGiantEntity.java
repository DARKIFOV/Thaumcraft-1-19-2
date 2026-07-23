package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.damage.TC4DamageSources;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Stage283-302 direct 1.19.2 boss adapter for TC4 EntityTaintacleGiant. */
public class TaintacleGiantEntity extends Monster implements TaintedMob {
    public static final String ANGER_TAG = "Anger";
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.thaumcraft.taintacle_giant"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private int anger = 0;
    private int attackCooldown = 0;
    private float flailIntensity = 1.0F;

    public TaintacleGiantEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 20;
        setCustomName(Component.translatable("entity.thaumcraft.taintacle_giant").withStyle(ChatFormatting.DARK_PURPLE));
        setCustomNameVisible(true);
        bossBar.setDarkenScreen(false);
        bossBar.setCreateWorldFog(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 125.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // TC4 taintacle is mostly stationary and selects targets manually.
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        TC4ChampionModifierRuntime.makeChampion(this, true);
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (anger > 0) anger--;
        if (attackCooldown > 0) attackCooldown--;
        if (level.isClientSide) {
            if (anger > 0 && random.nextInt(15) == 0) {
                level.addParticle(ParticleTypes.ANGRY_VILLAGER, getRandomX(0.8D), getY() + getBbHeight() + random.nextFloat() * 0.5D, getRandomZ(0.8D), 0.0D, 0.02D, 0.0D);
            }
            flailIntensity = Mth.clamp(flailIntensity + (anger > 0 ? 0.08F : -0.04F), 1.0F, 3.0F);
            return;
        }
        if (!(level instanceof ServerLevel server)) return;
        bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        bossBar.setName(getDisplayName());
        if (tickCount % 30 == 0) heal(1.0F);
        if (tickCount % 20 == 0) acquireAndStrike(server);
    }

    private void acquireAndStrike(ServerLevel server) {
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(getBbHeight() * 6.0F, getBbHeight() * 3.0F, getBbHeight() * 6.0F), LivingEntity::isAlive)) {
            if (candidate == this || candidate instanceof TaintacleGiantEntity || candidate instanceof TaintCrawlerEntity) continue;
            double d = distanceToSqr(candidate);
            if (d < best) {
                best = d;
                target = candidate;
            }
        }
        if (target == null || !hasLineOfSight(target)) return;
        faceEntityLikeTC4(target, 5.0F);
        double reach = getBbHeight() * getBbHeight();
        if (best <= reach && attackCooldown <= 0) {
            attackCooldown = 20;
            if (target.hurt(TC4DamageSources.tentacle(this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                server.playSound(null, blockPosition(), TC4Sounds.event("tentacle"), SoundSource.HOSTILE, getSoundVolume(), getVoicePitch());
            }
        } else if (best > reach && attackCooldown <= 0) {
            attackCooldown = 40 + random.nextInt(20);
            spawnTaintAnchorNear(server, target.blockPosition());
        }
    }

    private void spawnTaintAnchorNear(ServerLevel server, BlockPos pos) {
        BlockPos place = pos.offset(random.nextInt(3) - random.nextInt(3), 0, random.nextInt(3) - random.nextInt(3));
        TaintacleSmallEntity small = ThaumcraftMod.TAINTACLE_SMALL.get().create(server);
        if (small != null) {
            small.moveTo(place.getX() + 0.5D, place.getY(), place.getZ() + 0.5D, 0.0F, 0.0F);
            small.getPersistentData().putString("TC4Original", "EntityTaintacleGiant.spawnTentacles");
            server.addFreshEntity(small);
        }
        if (server.isEmptyBlock(place)) {
            server.setBlock(place, ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState(), 3);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, place.getX() + 0.5D, place.getY() + 0.1D, place.getZ() + 0.5D, 12, 0.25D, 0.15D, 0.25D, 0.02D);
        }
    }

    private void faceEntityLikeTC4(Entity entity, float maxTurn) {
        double dx = entity.getX() - getX();
        double dz = entity.getZ() - getZ();
        float targetYaw = (float)(Mth.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        setYRot(Mth.approachDegrees(getYRot(), targetYaw, maxTurn));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level.isClientSide && amount > 35.0F) {
            if (anger == 0) {
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, Math.max(0, (int)(amount / 15.0F))));
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, Math.max(0, (int)(amount / 40.0F))));
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, Math.max(0, (int)(amount / 40.0F))));
                anger = 200;
                if (source.getEntity() instanceof Player player) {
                    player.displayClientMessage(Component.translatable("tc.boss.enrage", getDisplayName()), false);
                }
            }
            amount = 35.0F;
        }
        if (!level.isClientSide && source.getEntity() != null && distanceTo(source.getEntity()) > 16.0F && level instanceof ServerLevel server) {
            spawnTaintAnchorNear(server, source.getEntity().blockPosition());
        }
        return super.hurt(source, amount);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0.0D, Math.min(0.0D, movement.y), 0.0D));
    }

    @Override
    public boolean isPushable() { return true; }

    @Override
    protected boolean shouldDespawnInPeaceful() { return false; }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    protected float getSoundVolume() { return getBbHeight() / 8.0F; }

    @Override
    public float getVoicePitch() { return 1.3F - getBbHeight() / 10.0F; }

    public int getAnger() { return anger; }

    public float getFlailIntensity() { return flailIntensity; }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        boolean siblingBossNearby = !level.getEntitiesOfClass(TaintacleGiantEntity.class, new AABB(blockPosition()).inflate(48.0D), Entity::isAlive).isEmpty();
        if (!siblingBossNearby) {
            spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 1));
            TC4OuterLandsLootAdapter.dropBossDeathLoot(this, source, looting);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ANGER_TAG, anger);
        tag.putInt("AttackCooldown", attackCooldown);
        tag.putFloat("flailIntensity", flailIntensity);
        tag.putString("TC4Original", "EntityTaintacleGiant");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        anger = tag.getInt(ANGER_TAG);
        attackCooldown = tag.getInt("AttackCooldown");
        flailIntensity = tag.contains("flailIntensity") ? tag.getFloat("flailIntensity") : 1.0F;
    }
}
