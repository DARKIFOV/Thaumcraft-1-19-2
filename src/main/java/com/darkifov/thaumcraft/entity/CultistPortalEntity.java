package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

/**
 * Stage283-302 super-mega port of TC4 EntityCultistPortal.
 *
 * <p>Original anchors preserved here: stage/stagecounter NBT, immobile/no-gravity
 * boss display entity, 200 tick opening, banner/loot pulses, staged cultist spawns,
 * stage 12 leader spawn, stage>12 self-damage, contact zap and eldritch-object drop.
 * Old 1.7.10 TileBanner/blockLootCrate calls are converted to existing 1.19.2
 * cultist/loot/block adapters without registering addon-only placeholder items.</p>
 */
public class CultistPortalEntity extends Monster {
    public static final String STAGE_TAG = "stage";
    public static final String STAGE_COUNTER_TAG = "stagecounter";
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.thaumcraft.cultist_portal"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private int stage = 0;
    private int stageCounter = 200;
    private int pulse = 0;

    public CultistPortalEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 30;
        setNoGravity(true);
        setCustomName(Component.translatable("entity.thaumcraft.cultist_portal"));
        setCustomNameVisible(true);
        bossBar.setDarkenScreen(false);
        bossBar.setCreateWorldFog(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // Original func_70626_be is empty: the portal does not path or target.
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (pulse > 0) pulse--;
        if (level.isClientSide) {
            clientParticles();
            return;
        }
        if (!(level instanceof ServerLevel server)) return;
        bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        bossBar.setName(getDisplayName());

        if (stageCounter > 0) {
            stageCounter--;
            openingSequence(server);
        } else if (server.getNearestPlayer(this, 48.0D) != null) {
            pulse(server);
            switch (stage) {
                case 0, 1, 2, 3, 4 -> {
                    stageCounter = 15 + random.nextInt(Math.max(1, 10 - stage)) - stage;
                    spawnMinion(server);
                }
                case 12 -> {
                    stageCounter = 50 + getTiming(server) * 2 + random.nextInt(50);
                    spawnBoss(server);
                }
                default -> {
                    int timing = getTiming(server);
                    stageCounter = timing + random.nextInt(5 + timing / 3);
                    spawnMinion(server);
                }
            }
            stage++;
        } else {
            stageCounter = 30 + random.nextInt(30);
        }

        if (stage < 12) heal(1.0F);
        if (stage > 12) hurt(DamageSource.MAGIC, 5.0F + random.nextInt(5));
    }

    private void openingSequence(ServerLevel server) {
        if (stageCounter == 160 && stage == 0) {
            pulse(server);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos banner = blockPosition().relative(dir.getOpposite(), 6);
                server.setBlock(banner, ThaumcraftMod.ELDRITCH_DECORATIVE.get().defaultBlockState(), 3);
                server.sendParticles(ParticleTypes.ENCHANT, banner.getX() + 0.5D, banner.getY() + 0.7D, banner.getZ() + 0.5D, 18, 0.25D, 0.45D, 0.25D, 0.05D);
            }
            server.playSound(null, blockPosition(), TC4Sounds.event("wandfail"), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        if (stageCounter > 20 && stageCounter < 150 && stage == 0 && stageCounter % 13 == 0) {
            int x = blockPosition().getX() + random.nextInt(5) - random.nextInt(5);
            int z = blockPosition().getZ() + random.nextInt(5) - random.nextInt(5);
            if (x != blockPosition().getX() && z != blockPosition().getZ()) {
                BlockPos loot = new BlockPos(x, blockPosition().getY(), z);
                if (server.isEmptyBlock(loot)) {
                    int md = random.nextFloat() < 0.05F ? 2 : random.nextFloat() < 0.20F ? 1 : 0;
                    server.setBlock(loot, ThaumcraftMod.OUTER_LANDS_LOOT_CRATE.get().defaultBlockState().setValue(com.darkifov.thaumcraft.block.TC4LootBlock.VARIANT, md), 3);
                    server.sendParticles(ParticleTypes.ENCHANT, loot.getX() + 0.5D, loot.getY() + 0.5D, loot.getZ() + 0.5D, 12, 0.2D, 0.2D, 0.2D, 0.05D);
                    server.playSound(null, loot, TC4Sounds.event("wandfail"), SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        }
    }

    private int getTiming(ServerLevel server) {
        return server.getEntitiesOfClass(CrimsonCultistEntity.class, getBoundingBox().inflate(32.0D), Entity::isAlive).size() * 20;
    }

    private void spawnMinion(ServerLevel server) {
        EntityType<CrimsonCultistEntity> type = random.nextFloat() > 0.33F ? ThaumcraftMod.CRIMSON_KNIGHT.get() : ThaumcraftMod.CRIMSON_CLERIC.get();
        CrimsonCultistEntity cultist = type.create(server);
        if (cultist == null) return;
        cultist.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25D, getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
        cultist.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.TRIGGERED, null, null);
        cultist.restrictTo(blockPosition(), 32);
        cultist.getPersistentData().putString("TC4Original", "EntityCultistPortal.spawnMinions");
        server.addFreshEntity(cultist);
        server.playSound(null, cultist.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private void spawnBoss(ServerLevel server) {
        CrimsonCultistEntity leader = ThaumcraftMod.CRIMSON_PRAETOR.get().create(server);
        if (leader == null) return;
        leader.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25D, getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.TRIGGERED, null, null);
        leader.restrictTo(blockPosition(), 32);
        leader.getPersistentData().putString("TC4Original", "EntityCultistPortal.spawnBoss");
        server.addFreshEntity(leader);
        server.playSound(null, leader.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private void pulse(ServerLevel server) {
        pulse = 10;
        server.broadcastEntityEvent(this, (byte)16);
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5D, getZ(), 32, 0.8D, 1.3D, 0.8D, 0.05D);
    }

    private void clientParticles() {
        if (tickCount % 4 == 0 || pulse > 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, getRandomX(0.7D), getY() + random.nextDouble() * 3.0D, getRandomZ(0.7D), 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            pulse = 10;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (!level.isClientSide && distanceToSqr(player) < 3.0D && player.hurt(DamageSource.mobAttack(this), 8.0F)) {
            level.playSound(null, blockPosition(), TC4Sounds.event("zap"), SoundSource.HOSTILE, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.1F + 1.0F);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        // Immobile TC4 portal: func_70091_d is empty.
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected boolean shouldDespawnInPeaceful() { return false; }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    public int getStage() { return stage; }

    public int getStageCounter() { return stageCounter; }

    public int getPulse() { return pulse; }

    public float getTc4RenderScale(float partialTicks) {
        int e = (int)Math.min(50.0F, tickCount + partialTicks);
        if (hurtTime > 0) {
            double d = Math.sin(hurtTime * 72.0D * Math.PI / 180.0D);
            e = (int)(e + 6.0D * d);
        }
        if (pulse > 0) {
            double d = Math.sin(pulse * 36.0D * Math.PI / 180.0D);
            e = (int)(e + 12.0D * d);
        }
        float missing = (1.0F - getHealth() / getMaxHealth()) / 3.0F;
        float bob2 = net.minecraft.util.Mth.sin(tickCount / (6.0F - 15.0F * missing)) * missing + missing;
        return e / 50.0F * 1.3F - bob2 / 3.0F;
    }

    public float getTc4RenderAlpha() {
        float missing = (1.0F - getHealth() / getMaxHealth()) / 3.0F;
        float bob = net.minecraft.util.Mth.sin(tickCount / (5.0F - 12.0F * missing)) * missing + missing;
        return Math.max(0.0F, 1.0F - bob);
    }

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
        spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 1));
        TC4OuterLandsLootAdapter.dropBossDeathLoot(this, source, looting);
    }

    @Override
    protected void tickDeath() {
        if (!level.isClientSide && deathTime == 1) {
            level.explode(this, getX(), getY(), getZ(), 2.0F, false, Explosion.BlockInteraction.NONE);
        }
        super.tickDeath();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(STAGE_TAG, stage);
        tag.putInt(STAGE_COUNTER_TAG, stageCounter);
        tag.putInt("pulse", pulse);
        tag.putString("TC4Original", "EntityCultistPortal");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        stage = tag.getInt(STAGE_TAG);
        stageCounter = tag.contains(STAGE_COUNTER_TAG) ? tag.getInt(STAGE_COUNTER_TAG) : 200;
        pulse = tag.getInt("pulse");
    }
}
