package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stage215 1.19.2 base adapter for TC4 EntityThaumcraftBoss.
 *
 * TC4 bosses preserved a home radius, a spawn timer, anger particles, player-count
 * health/damage buffs and champion display-name generation.  This class mirrors
 * those semantics with Forge 1.19.2 APIs and persistent tags instead of the old
 * old watcher/attribute surface.
 */
public abstract class TC4ThaumcraftBossEntity extends Monster {
    public static final String HOME_X_TAG = "HomeX";
    public static final String HOME_Y_TAG = "HomeY";
    public static final String HOME_Z_TAG = "HomeZ";
    public static final String HOME_D_TAG = "HomeD";
    public static final String SPAWN_TIMER_TAG = "SpawnTimer";
    public static final String ANGER_TAG = "Anger";

    private static final UUID[] PLAYER_HEALTH_BUFF_UUIDS = new UUID[] {
            UUID.fromString("54d621c1-dd4d-4b43-8bd2-5531c8875797"),
            UUID.fromString("f51257dc-b7fa-4f7a-92d7-75d68e8592c4"),
            UUID.fromString("3d6b2e42-4141-4364-b76d-0e8664bbd0bb"),
            UUID.fromString("02c97a08-801c-4131-afa2-1427a6151934"),
            UUID.fromString("0f354f6a-33c5-40be-93be-81b1338567f1")
    };
    private static final UUID[] PLAYER_DAMAGE_BUFF_UUIDS = new UUID[] {
            UUID.fromString("534f8c57-929a-48cf-bbd6-0fd851030748"),
            UUID.fromString("d317a76e-0e7c-4c61-acfd-9fa286053b32"),
            UUID.fromString("ff462d63-26a2-4363-830e-143ed97e2a4f"),
            UUID.fromString("cf1eb39e-0c67-495f-887c-0d3080828d2f"),
            UUID.fromString("3cfab9da-2701-43d8-ac07-885f16fa4117")
    };

    private final Map<UUID, Integer> tc4Aggro = new HashMap<>();
    protected int spawnTimer = 0;
    protected int anger = 0;
    private int lastAppliedPlayerBuffs = 0;
    private final ServerBossEvent tc4BossBar = new ServerBossEvent(Component.literal("Thaumcraft Boss"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    protected TC4ThaumcraftBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 50;
        tc4BossBar.setDarkenScreen(false);
        tc4BossBar.setCreateWorldFog(false);
    }

    protected void setTc4Home(BlockPos center, int distance) {
        restrictTo(center, distance);
        CompoundTag tag = getPersistentData();
        tag.putInt(HOME_X_TAG, center.getX());
        tag.putInt(HOME_Y_TAG, center.getY());
        tag.putInt(HOME_Z_TAG, center.getZ());
        tag.putInt(HOME_D_TAG, distance);
    }

    protected void restoreTc4Home(CompoundTag tag) {
        if (tag.contains(HOME_D_TAG)) {
            restrictTo(new BlockPos(tag.getInt(HOME_X_TAG), tag.getInt(HOME_Y_TAG), tag.getInt(HOME_Z_TAG)), tag.getInt(HOME_D_TAG));
        }
    }

    public int getSpawnTimer() {
        return spawnTimer;
    }

    public void setSpawnTimer(int spawnTimer) {
        this.spawnTimer = Math.max(0, spawnTimer);
    }

    public int getAnger() {
        return anger;
    }

    public void setAnger(int anger) {
        this.anger = Math.max(0, anger);
    }

    public boolean isSpawning() {
        return spawnTimer > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (spawnTimer > 0) {
            spawnTimer--;
        }
        if (anger > 0) {
            anger--;
        }
        if (!level.isClientSide) {
            updateTc4BossBar();
            if (tickCount % 30 == 0) {
                heal(1.0F);
            }
            if (tickCount % 20 == 0) {
                updatePlayerCountBuffs();
            }
        } else if (anger > 0 && random.nextInt(15) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER, getRandomX(0.6D), getY() + getBbHeight() + random.nextFloat() * 0.5D, getRandomZ(0.6D), 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (spawnTimer == 0) {
            super.customServerAiStep();
        }
        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isSpawning();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || entity instanceof TC4ThaumcraftBossEntity || entity instanceof EldritchGuardianEntity;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isSpawning()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            tc4Aggro.merge(living.getUUID(), Math.max(1, (int)amount), Integer::sum);
            setAnger(80);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }


    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        tc4BossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        tc4BossBar.removePlayer(player);
    }

    private void updateTc4BossBar() {
        float progress = getMaxHealth() <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth()));
        tc4BossBar.setProgress(progress);
        tc4BossBar.setName(getDisplayName());
        tc4BossBar.setVisible(isAlive());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(SPAWN_TIMER_TAG, spawnTimer);
        tag.putInt(ANGER_TAG, anger);
        CompoundTag persistent = getPersistentData();
        if (persistent.contains(HOME_D_TAG)) {
            tag.putInt(HOME_D_TAG, persistent.getInt(HOME_D_TAG));
            tag.putInt(HOME_X_TAG, persistent.getInt(HOME_X_TAG));
            tag.putInt(HOME_Y_TAG, persistent.getInt(HOME_Y_TAG));
            tag.putInt(HOME_Z_TAG, persistent.getInt(HOME_Z_TAG));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        spawnTimer = tag.getInt(SPAWN_TIMER_TAG);
        anger = tag.getInt(ANGER_TAG);
        if (tag.contains(HOME_D_TAG)) {
            getPersistentData().putInt(HOME_D_TAG, tag.getInt(HOME_D_TAG));
            getPersistentData().putInt(HOME_X_TAG, tag.getInt(HOME_X_TAG));
            getPersistentData().putInt(HOME_Y_TAG, tag.getInt(HOME_Y_TAG));
            getPersistentData().putInt(HOME_Z_TAG, tag.getInt(HOME_Z_TAG));
            restoreTc4Home(getPersistentData());
        }
        generateName();
    }

    public abstract void generateName();

    protected Component championModifierName() {
        int mod = TC4ChampionModifierRuntime.championMod(this);
        if (mod < 0) {
            return Component.translatable("champion.mod.normal");
        }
        return Component.translatable("champion.mod." + TC4ChampionModifierRuntime.MODS[mod].name());
    }

    protected void playBossSound(String sound, float volume, float pitch) {
        level.playSound(null, blockPosition(), com.darkifov.thaumcraft.porting.TC4Sounds.event(sound), SoundSource.HOSTILE, volume, pitch);
    }

    private void updatePlayerCountBuffs() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB box = getBoundingBox().inflate(32.0D);
        int players = serverLevel.getEntitiesOfClass(Player.class, box, Player::isAlive).size();
        int wanted = Math.max(0, Math.min(5, players - 1));
        if (wanted == lastAppliedPlayerBuffs) {
            return;
        }
        double oldMax = getMaxHealth();
        for (int i = 0; i < PLAYER_HEALTH_BUFF_UUIDS.length; i++) {
            if (getAttribute(Attributes.MAX_HEALTH) != null) {
                getAttribute(Attributes.MAX_HEALTH).removeModifier(PLAYER_HEALTH_BUFF_UUIDS[i]);
            }
            if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(PLAYER_DAMAGE_BUFF_UUIDS[i]);
            }
        }
        for (int i = 0; i < wanted; i++) {
            if (getAttribute(Attributes.MAX_HEALTH) != null) {
                getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(PLAYER_HEALTH_BUFF_UUIDS[i], "TC4 boss player health buff " + (i + 1), 50.0D, AttributeModifier.Operation.ADDITION));
            }
            if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(PLAYER_DAMAGE_BUFF_UUIDS[i], "TC4 boss player damage buff " + (i + 1), 0.5D, AttributeModifier.Operation.ADDITION));
            }
        }
        lastAppliedPlayerBuffs = wanted;
        double newMax = getMaxHealth();
        if (oldMax > 0.0D && newMax > 0.0D) {
            setHealth((float)Math.max(1.0D, Math.min(newMax, getHealth() * (newMax / oldMax))));
        }
    }

    protected void knockTargetAway(Entity target, double strength) {
        Vec3 delta = target.position().subtract(position()).normalize();
        target.push(delta.x * strength, 0.12D, delta.z * strength);
    }
}
