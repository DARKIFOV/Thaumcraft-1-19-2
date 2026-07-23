package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.MindSpiderEntity;
import com.darkifov.thaumcraft.effect.TC4WarpMobEffect;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import com.darkifov.thaumcraft.warp.TC4WarpRuntimeParity;
import com.darkifov.thaumcraft.warp.TC4UnnaturalHungerParity;
import com.darkifov.thaumcraft.warp.TC4WarpResearchGrant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

/**
 * Direct 1.19.2 runtime port of TC4 4.2.3.5 WarpEvents.
 *
 * <p>The original check runs once per 2000 player ticks, uses sqrt(warpCounter)
 * as the event roll, includes warping gear in the severity calculation and
 * decays one temporary warp after every unwarded check. The previous rebuild
 * checked ten times more often, injected a second percentage/cooldown system
 * and shifted every effect range by four points; this class restores the
 * original scheduling and event table.</p>
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WarpEvents {

    private WarpEvents() {
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MobEffectInstance hunger = player.getEffect(ThaumcraftMod.UNNATURAL_HUNGER.get());
        if (hunger == null) {
            return;
        }

        // Forge documents Finish#getItem() as the pre-use copy, matching the
        // original PlayerUseItemEvent.Finish item comparison after consumption.
        ItemStack consumed = event.getItem();
        boolean rottenFlesh = consumed.is(Items.ROTTEN_FLESH);
        boolean zombieBrain = ThaumcraftMod.TC4_RESEARCH_ITEMS.containsKey("tc4_brain")
                && consumed.is(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_brain").get());

        if (rottenFlesh || zombieBrain) {
            TC4UnnaturalHungerParity.Reduction reduced =
                    TC4UnnaturalHungerParity.afterCurative(
                            hunger.getDuration(), hunger.getAmplifier());

            player.removeEffect(ThaumcraftMod.UNNATURAL_HUNGER.get());
            if (reduced.remainsActive()) {
                MobEffectInstance replacement = new MobEffectInstance(
                        ThaumcraftMod.UNNATURAL_HUNGER.get(),
                        reduced.duration(),
                        reduced.amplifier(),
                        true, true, true);
                // The original re-added instance cleared all cures and restored
                // only rotten flesh. Brain handling still comes from this event.
                replacement.setCurativeItems(List.of(new ItemStack(Items.ROTTEN_FLESH)));
                player.addEffect(replacement);
            }
            hungerMessage(player, "warp.text.hunger.2", ChatFormatting.DARK_GREEN);
        } else if (consumed.isEdible()) {
            // TC4 warned about ordinary food but did not cancel its nutrition.
            hungerMessage(player, "warp.text.hunger.1", ChatFormatting.DARK_RED);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        tickLegacyTimers(player);

        if (player.tickCount % 10 == 0 && player.hasEffect(ThaumcraftMod.DEATH_GAZE.get())) {
            checkDeathGaze(player);
        }

        if (player.tickCount <= 0 || player.tickCount % TC4WarpRuntimeParity.WARP_CHECK_INTERVAL_TICKS != 0
                || !ThaumcraftConfig.WARP_EVENTS_ENABLED.get()) {
            return;
        }

        // TC4 skipped checkWarpEvent entirely while Warp Ward was active. That
        // also pauses temporary-warp decay until the ward expires.
        if (hasWarpWard(player)) {
            return;
        }

        checkWarpEvent(player);
    }

    private static void tickLegacyTimers(ServerPlayer player) {
        // Worlds from older rebuild versions stored a second authoritative ward
        // timer in persistent NBT. Migrate it once into the real MobEffect and
        // clear the old field so milk/commands can remove Warp Ward as in TC4.
        int legacyWard = PlayerThaumData.takeLegacyWarpWardTicks(player);
        if (legacyWard > 0 && !player.hasEffect(ThaumcraftMod.WARP_WARD.get())) {
            player.addEffect(new MobEffectInstance(
                    ThaumcraftMod.WARP_WARD.get(),
                    legacyWard,
                    0,
                    true,
                    true,
                    true
            ));
        }

        // Retain save compatibility for worlds created by 11.62.58-11.62.66.
        // The non-TC4 cooldown is no longer consulted by the event scheduler.
        int cooldown = PlayerThaumData.getWarpEventCooldown(player);
        if (cooldown > 0) {
            PlayerThaumData.setWarpEventCooldown(player, cooldown - 1);
        }
    }

    private static boolean hasWarpWard(ServerPlayer player) {
        return player.hasEffect(ThaumcraftMod.WARP_WARD.get());
    }

    private static void checkWarpEvent(ServerPlayer player) {
        int storedWarp = PlayerThaumData.getWarpTotal(player);
        int actualWarp = PlayerThaumData.getActualWarp(player);
        int warp = storedWarp + TC4WarpingGearAdapter.getEquippedWarp(player);
        int warpCounter = PlayerThaumData.getWarpCounter(player);
        int roll = player.getRandom().nextInt(100);

        boolean triggered = warpCounter > 0
                && warp > 0
                && roll <= Math.sqrt(warpCounter);

        if (triggered) {
            int normalizedWarp = Math.min(100, (warp + warp + warpCounter) / 3);
            int reducedCounter = (int) (warpCounter - Math.max(5.0D, Math.sqrt(warpCounter) * 2.0D));
            PlayerThaumData.setWarpCounter(player, reducedCounter);

            int effectRoll = player.getRandom().nextInt(Math.max(1, normalizedWarp));
            if (TC4FortressMaskRuntime.hasGrinningDevil(player)) {
                effectRoll -= 2 + player.getRandom().nextInt(4);
            }

            sendWarpPulse(player);
            if (effectRoll > 0) {
                applyOriginalEvent(player, effectRoll, normalizedWarp);
            }

            // The three Eldritch research thresholds were evaluated only after
            // a successful warp event in TC4.
            if (actualWarp > TC4EldritchProgression.BATHSALTS_WARP
                    || actualWarp > TC4EldritchProgression.ELDRITCH_MINOR_WARP
                    || actualWarp > TC4EldritchProgression.ELDRITCH_MAJOR_WARP) {
                TC4EldritchProgression.syncFromWarp(player);
            }
        }

        // Original WarpEvents.checkWarpEvent always decayed one temporary warp
        // after an unwarded check, regardless of whether the random event fired.
        PlayerThaumData.decayTemporaryWarp(player, 1);
        ThaumcraftNetwork.syncResearch(player);
    }

    private static void applyOriginalEvent(ServerPlayer player, int effectRoll, int warp) {
        if (effectRoll <= 4) {
            grantResearch(player, 1);
            message(player, "warp.text.3");
        } else if (effectRoll <= 8) {
            // Intentional TC4 gap: visual pulse only.
        } else if (effectRoll <= 12) {
            message(player, "warp.text.11");
        } else if (effectRoll <= 16) {
            addWarpEffect(player, ThaumcraftMod.VIS_EXHAUST.get(), 5000, Math.min(3, warp / 15), true);
            message(player, "warp.text.1");
        } else if (effectRoll <= 20) {
            addWarpEffect(player, ThaumcraftMod.THAUMARHIA.get(), Math.min(32000, 10 * warp), 0, true);
            message(player, "warp.text.15");
        } else if (effectRoll <= 24) {
            addWarpEffect(player, ThaumcraftMod.UNNATURAL_HUNGER.get(),
                    TC4UnnaturalHungerParity.FIRST_WARP_DURATION_TICKS,
                    TC4UnnaturalHungerParity.warpAmplifier(warp), true);
            message(player, "warp.text.2");
        } else if (effectRoll <= 28) {
            message(player, "warp.text.12");
        } else if (effectRoll <= 32) {
            spawnMist(player, 1);
        } else if (effectRoll <= 36) {
            // TC4 WarpEvents applied Config.potionBlurredID here with NO chat
            // line. warp.text.8 is the BATHSALTS milestone message (actual
            // warp > 10), not this event; see TC4EldritchProgression.
            addWarpEffect(player, ThaumcraftMod.BLURRED_VISION.get(), Math.min(32000, 10 * warp), 0, true);
        } else if (effectRoll <= 40) {
            addWarpEffect(player, ThaumcraftMod.SUN_SCORNED.get(), 5000, Math.min(3, warp / 15), true);
            message(player, "warp.text.5");
        } else if (effectRoll <= 44) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, Math.min(3, warp / 15), true, true, true));
            message(player, "warp.text.9");
        } else if (effectRoll <= 48) {
            addWarpEffect(player, ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get(), 6000, Math.min(3, warp / 15), false);
            message(player, "warp.text.1");
        } else if (effectRoll <= 52) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true, true, true));
            message(player, "warp.text.10");
        } else if (effectRoll <= 56) {
            addWarpEffect(player, ThaumcraftMod.DEATH_GAZE.get(), 6000, Math.min(3, warp / 15), true);
            message(player, "warp.text.4");
        } else if (effectRoll <= 60) {
            suddenlySpiders(player, warp, false);
        } else if (effectRoll <= 64) {
            message(player, "warp.text.13");
        } else if (effectRoll <= 68) {
            spawnMist(player, warp / 30);
        } else if (effectRoll <= 72) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, true, true, true));
        } else if (effectRoll <= 75) {
            // Intentional TC4 gap: visual pulse only.
        } else if (effectRoll == 76) {
            if (PlayerThaumData.getWarpSticky(player) > 0) {
                PlayerThaumData.decayStickyWarpFromEvent(player, 1);
            }
            message(player, "warp.text.14");
        } else if (effectRoll <= 80) {
            addWarpEffect(player, ThaumcraftMod.UNNATURAL_HUNGER.get(),
                    TC4UnnaturalHungerParity.SECOND_WARP_DURATION_TICKS,
                    TC4UnnaturalHungerParity.warpAmplifier(warp), true);
            message(player, "warp.text.2");
        } else if (effectRoll <= 84) {
            grantResearch(player, warp / 10);
            message(player, "warp.text.3");
        } else if (effectRoll <= 88) {
            // Intentional TC4 gap: visual pulse only.
        } else if (effectRoll <= 92) {
            suddenlySpiders(player, warp, true);
        } else {
            spawnMist(player, warp / 15);
        }
    }

    /**
     * Message keys that {@link #applyOriginalEvent} can display, enumerated from
     * the production event table. warp.text.8 is intentionally absent: in TC4 it
     * is shown by the BATHSALTS warp milestone (see TC4EldritchProgression),
     * never by the per-event table.
     */
    private static final java.util.Set<String> DISPLAYED_WARP_MESSAGES = java.util.Set.of(
            "warp.text.1",
            "warp.text.2",
            "warp.text.3",
            "warp.text.4",
            "warp.text.5",
            "warp.text.6",
            "warp.text.7",
            "warp.text.9",
            "warp.text.10",
            "warp.text.11",
            "warp.text.12",
            "warp.text.13",
            "warp.text.14",
            "warp.text.15");

    public static boolean usesWarpMessage(String key) {
        return DISPLAYED_WARP_MESSAGES.contains(key);
    }

    private static void addWarpEffect(ServerPlayer player, MobEffect effect, int duration, int amplifier, boolean ambient) {
        player.addEffect(TC4WarpMobEffect.configureCuratives(
                new MobEffectInstance(effect, duration, amplifier, ambient, true, true)
        ));
    }

    private static void sendWarpPulse(ServerPlayer player) {
        if (player.level instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 28, 0.8D, 0.8D, 0.8D, 0.04D);
        }
        player.level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 0.75F, 0.65F + player.getRandom().nextFloat() * 0.25F);
    }

    private static void message(ServerPlayer player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
    }

    private static void hungerMessage(ServerPlayer player, String translationKey, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(translationKey).withStyle(color, ChatFormatting.ITALIC), false);
    }

    private static void spawnMist(ServerPlayer player, int guardians) {
        if (player.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 120, 4.0D, 1.5D, 4.0D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 3.0D, 1.2D, 3.0D, 0.05D);
        }

        for (int i = 0; i < Math.min(8, Math.max(0, guardians)); i++) {
            spawnGuardian(player);
        }

        message(player, "warp.text.6");
    }

    private static void spawnGuardian(ServerPlayer player) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        EldritchGuardianEntity guardian = ThaumcraftMod.ELDRITCH_GUARDIAN.get().create(serverLevel);
        if (guardian == null) {
            return;
        }

        Optional<BlockPos> spawn = findSpawnAround(player, guardian, 7, 24);
        if (spawn.isEmpty()) {
            return;
        }

        BlockPos pos = spawn.get();
        guardian.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                player.getRandom().nextFloat() * 360.0F, 0.0F);
        guardian.setTarget(player);
        serverLevel.addFreshEntity(guardian);
    }

    private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        int spawns = Math.min(50, Math.max(0, warp));
        for (int i = 0; i < spawns; i++) {
            MindSpiderEntity spider = ThaumcraftMod.MIND_SPIDER.get().create(serverLevel);
            if (spider == null) {
                continue;
            }

            Optional<BlockPos> spawn = findSpawnAround(player, spider, 7, 24);
            if (spawn.isEmpty()) {
                continue;
            }

            BlockPos pos = spawn.get();
            spider.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                    player.getRandom().nextFloat() * 360.0F, 0.0F);
            spider.setTarget(player);
            if (!real) {
                spider.setViewer(player.getGameProfile().getName());
                spider.setHarmless(true);
            }
            serverLevel.addFreshEntity(spider);
        }

        message(player, "warp.text.7");
    }

    /** Original WarpEvents.checkDeathGaze, evaluated every ten player ticks. */
    private static void checkDeathGaze(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(ThaumcraftMod.DEATH_GAZE.get());
        if (effect == null) {
            return;
        }

        int range = Math.min(8 + effect.getAmplifier() * 3, 24);
        List<LivingEntity> targets = player.level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(range),
                target -> target != player && target.isAlive()
        );

        for (LivingEntity target : targets) {
            // TC4 required Entity.canBeCollidedWith() before the cone and
            // line-of-sight checks. Modern isPickable() is the corresponding
            // entity-level admission gate.
            if (!target.isPickable()
                    || !isInsideDeathGazeCone(player, target, range)
                    || !player.hasLineOfSight(target)
                    || target.hasEffect(MobEffects.WITHER)) {
                continue;
            }
            if (target instanceof ServerPlayer && player.getServer() != null && !player.getServer().isPvpAllowed()) {
                continue;
            }

            target.setLastHurtByPlayer(player);
            if (target instanceof Mob mob) {
                mob.setTarget(player);
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
        }
    }

    private static void grantResearch(ServerPlayer player, int times) {
        TC4WarpResearchGrant.grantAndSync(player, times);
    }

    private static boolean isInsideDeathGazeCone(ServerPlayer player, LivingEntity target, int range) {
        Vec3 origin = player.getEyePosition();
        // Original EntityUtils.isVisibleTo targeted the entity body midpoint,
        // not its eye position. This matters for tall/short entities.
        Vec3 targetCenter = new Vec3(
                target.getX(),
                target.getBoundingBox().minY + target.getBbHeight() / 2.0D,
                target.getZ());
        Vec3 toTarget = targetCenter.subtract(origin);
        Vec3 look = player.getLookAngle();
        return TC4WarpRuntimeParity.deathGazeConeContains(
                toTarget.x, toTarget.y, toTarget.z,
                look.x, look.y, look.z,
                range);
    }

    private static Optional<BlockPos> findSpawnAround(ServerPlayer player, LivingEntity entity, int min, int max) {
        ServerLevel level = player.getLevel();
        BlockPos base = player.blockPosition();

        for (int i = 0; i < 50; i++) {
            int dx = randomTc4AxisOffset(player, min, max);
            int dy = randomTc4AxisOffset(player, min, max);
            int dz = randomTc4AxisOffset(player, min, max);
            BlockPos pos = base.offset(dx, dy, dz);

            if (pos.getY() <= level.getMinBuildHeight() + 1 || pos.getY() >= level.getMaxBuildHeight() - 2) {
                continue;
            }
            boolean solidTopSurface = level.getBlockState(pos.below())
                    .isFaceSturdy(level, pos.below(), Direction.UP);

            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                    player.getRandom().nextFloat() * 360.0F, 0.0F);
            boolean collisionFree = level.noCollision(entity);
            boolean containsLiquid = containsLiquid(level, entity.getBoundingBox());
            if (TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(
                    solidTopSurface, collisionFree, containsLiquid)) {
                return Optional.of(pos);
            }
        }

        return Optional.empty();
    }

    private static boolean containsLiquid(ServerLevel level, AABB bounds) {
        int minX = Mth.floor(bounds.minX);
        int minY = Mth.floor(bounds.minY);
        int minZ = Mth.floor(bounds.minZ);
        int maxX = Mth.floor(bounds.maxX - 1.0E-7D);
        int maxY = Mth.floor(bounds.maxY - 1.0E-7D);
        int maxZ = Mth.floor(bounds.maxZ - 1.0E-7D);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!level.getFluidState(new BlockPos(x, y, z)).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int randomTc4AxisOffset(ServerPlayer player, int min, int max) {
        int magnitude = Mth.nextInt(player.getRandom(), min, max);
        int signRoll = Mth.nextInt(player.getRandom(), -1, 1);
        return TC4WarpRuntimeParity.signedSpawnOffset(magnitude, signRoll);
    }
}
