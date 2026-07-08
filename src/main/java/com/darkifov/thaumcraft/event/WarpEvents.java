package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WarpEvents {
    private WarpEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        tickTimers(player);

        if (player.tickCount % 200 != 0) {
            return;
        }

        int warp = PlayerThaumData.getWarpTotal(player);
        int actualWarp = PlayerThaumData.getActualWarp(player);

        if (warp <= 0) {
            return;
        }

        TC4EldritchProgression.syncFromWarp(player);

        if (PlayerThaumData.getWarpEventCooldown(player) > 0) {
            return;
        }

        PlayerThaumData.addWarpCounter(player, Math.max(1, warp / 2));
        int counter = PlayerThaumData.getWarpCounter(player);
        int tc4Trigger = Mth.clamp((int) Math.sqrt(counter), 1, 90);
        int configTrigger = Math.min(
                ThaumcraftConfig.WARP_EVENT_MAX_CHANCE.get(),
                Math.max(ThaumcraftConfig.WARP_EVENT_MIN_CHANCE.get(), warp * ThaumcraftConfig.WARP_EVENT_CHANCE_PER_WARP.get())
        );
        int chance = Math.max(tc4Trigger, configTrigger);

        if (PlayerThaumData.hasWarpWard(player)) {
            chance = Math.max(1, chance / 4);

            if (player.getRandom().nextInt(100) < 18) {
                player.displayClientMessage(Component.literal("Warp Ward absorbs a whisper from beyond.").withStyle(ChatFormatting.AQUA), true);
            }
        }

        if (player.getRandom().nextInt(100) >= chance) {
            return;
        }

        int normalizedWarp = Math.min(100, (warp + warp + counter) / 3);
        PlayerThaumData.setWarpCounter(player, Math.max(0, counter - Math.max(5, (int) Math.sqrt(counter) * 2)));
        triggerWarpEvent(player, Math.max(1, normalizedWarp), actualWarp);

        int randomCooldown = Math.max(1, ThaumcraftConfig.WARP_EVENT_COOLDOWN_RANDOM_TICKS.get());
        PlayerThaumData.setWarpEventCooldown(
                player,
                ThaumcraftConfig.WARP_EVENT_COOLDOWN_MIN_TICKS.get() + player.getRandom().nextInt(randomCooldown)
        );
        PlayerThaumData.decayTemporaryWarp(player, 1);
        ThaumcraftNetwork.syncResearch(player);
    }

    private static void tickTimers(ServerPlayer player) {
        int ward = PlayerThaumData.getWarpWardTicks(player);

        if (ward > 0) {
            PlayerThaumData.setWarpWardTicks(player, ward - 1);
        }

        int cooldown = PlayerThaumData.getWarpEventCooldown(player);

        if (cooldown > 0) {
            PlayerThaumData.setWarpEventCooldown(player, cooldown - 1);
        }
    }

    private static void triggerWarpEvent(ServerPlayer player, int warp, int actualWarp) {
        int eff = player.getRandom().nextInt(Math.max(1, warp));
        if (TC4FortressMaskRuntime.hasGrinningDevil(player)) {
            eff -= 2 + player.getRandom().nextInt(4);
        }
        if (eff <= 0) {
            message(player, "warp.text.mask", "The Grinning Devil mask dampens the Warp event.");
            return;
        }

        if (eff <= 4) {
            grantResearch(player, 1);
            message(player, "warp.text.3", "Forbidden insight floods your mind.");
        } else if (eff <= 8) {
            whispers(player, "warp.text.11", "A voice from nowhere whispers your name.");
        } else if (eff <= 12) {
            exhaustion(player, warp);
        } else if (eff <= 16) {
            thaumarhia(player, warp);
        } else if (eff <= 20) {
            unnaturalHunger(player, warp, 5000);
        } else if (eff <= 24) {
            message(player, "warp.text.12", "You feel watched from behind the world.");
        } else if (eff <= 28) {
            spawnMist(player, warp, 1);
        } else if (eff <= 32) {
            blurred(player, warp);
        } else if (eff <= 36) {
            sunScorned(player, warp);
        } else if (eff <= 40) {
            weakness(player, warp);
        } else if (eff <= 44) {
            infiniteExhaust(player, warp);
        } else if (eff <= 48) {
            taintedGround(player, 8);
        } else if (eff <= 52) {
            deathGaze(player, warp);
        } else if (eff <= 56) {
            suddenlySpiders(player, warp, false);
        } else if (eff <= 60) {
            message(player, "warp.text.13", "Reality bends, then pretends it did not.");
        } else if (eff <= 64) {
            spawnMist(player, warp, Math.max(1, warp / 30));
        } else if (eff <= 68) {
            slowness(player, warp);
        } else if (eff == 76 && PlayerThaumData.getWarpSticky(player) > 0) {
            PlayerThaumData.addWarpSticky(player, -1);
            message(player, "warp.text.14", "A fragment of sticky Warp tears loose.");
        } else if (eff <= 80) {
            unnaturalHunger(player, warp, 6000);
        } else if (eff <= 84) {
            grantResearch(player, Math.max(1, warp / 10));
            message(player, "warp.text.3", "Forbidden insight floods your mind.");
        } else if (eff <= 88) {
            taintedGround(player, 14);
        } else if (eff <= 92) {
            suddenlySpiders(player, warp, true);
        } else {
            spawnMist(player, warp, Math.max(1, warp / 15));
        }

        if (actualWarp > TC4EldritchProgression.BATHSALTS_WARP
                || actualWarp > TC4EldritchProgression.ELDRITCH_MINOR_WARP
                || actualWarp > TC4EldritchProgression.ELDRITCH_MAJOR_WARP) {
            TC4EldritchProgression.syncFromWarp(player);
        }
    }

    private static void message(ServerPlayer player, String originalKey, String fallback) {
        player.displayClientMessage(Component.literal(fallback + " [TC4: " + originalKey + "]").withStyle(ChatFormatting.DARK_PURPLE), false);
        player.level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 0.8F, 0.6F + player.getRandom().nextFloat() * 0.3F);
    }

    private static void whispers(ServerPlayer player, String originalKey, String fallback) {
        message(player, originalKey, fallback);
    }

    private static void exhaustion(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5000, Math.min(3, warp / 15)));
        message(player, "warp.text.1", "Your vis feels exhausted.");
    }

    private static void infiniteExhaust(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 6000, Math.min(4, warp / 15)));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6000, 0));
        message(player, "warp.text.1", "A deeper exhaustion gnaws at your aura.");
    }

    private static void thaumarhia(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(32000, 10 * warp), 0));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
        message(player, "warp.text.15", "Thaumic static leaks from you.");
    }

    private static void unnaturalHunger(ServerPlayer player, int warp, int duration) {
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, Math.min(3, warp / 15)));
        message(player, "warp.text.2", "Your hunger is not your own.");
    }

    private static void blurred(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(32000, 10 * warp), 0));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
        message(player, "warp.text.6", "Your vision blurs with impossible shapes.");
    }

    private static void sunScorned(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5000, Math.min(3, warp / 15)));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
        message(player, "warp.text.5", "The sun feels hostile.");
    }

    private static void weakness(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, Math.min(3, warp / 15)));
        message(player, "warp.text.9", "Your body obeys the wrong laws.");
    }

    private static void deathGaze(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, Math.min(2, warp / 25)));
        message(player, "warp.text.4", "Your gaze hungers for death.");
    }

    private static void slowness(ServerPlayer player, int warp) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Math.min(32000, 5 * warp), 0));
        message(player, "warp.text.10", "The world thickens around your feet.");
    }

    private static void spawnMist(ServerPlayer player, int warp, int guardians) {
        if (player.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 120, 4.0D, 1.5D, 4.0D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 3.0D, 1.2D, 3.0D, 0.05D);
        }

        for (int i = 0; i < Math.min(8, guardians); i++) {
            spawnGuardian(player);
        }

        message(player, "warp.text.6", "Eldritch mist rolls in.");
    }

    private static void spawnGuardian(ServerPlayer player) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        EldritchGuardianEntity guardian = ThaumcraftMod.ELDRITCH_GUARDIAN.get().create(serverLevel);

        if (guardian == null) {
            return;
        }

        BlockPos spawn = findSpawnAround(player, 7, 24);
        guardian.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
        guardian.setTarget(player);
        serverLevel.addFreshEntity(guardian);
    }

    private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        int spawns = Math.min(real ? 12 : 6, Math.max(2, warp / 6));

        for (int i = 0; i < spawns; i++) {
            if (real) {
                CaveSpider spider = EntityType.CAVE_SPIDER.create(serverLevel);

                if (spider != null) {
                    BlockPos spawn = findSpawnAround(player, 7, 16);
                    spider.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, 0.0F, 0.0F);
                    spider.setTarget(player);
                    serverLevel.addFreshEntity(spider);
                }
            } else {
                TaintCrawlerEntity crawler = ThaumcraftMod.TAINT_CRAWLER.get().create(serverLevel);

                if (crawler != null) {
                    BlockPos spawn = findSpawnAround(player, 4, 10);
                    crawler.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, 0.0F, 0.0F);
                    crawler.setTarget(player);
                    serverLevel.addFreshEntity(crawler);
                }
            }
        }

        message(player, "warp.text.7", real ? "Spiders pour out of your thoughts." : "Something like spiders skitters nearby.");
    }

    private static void taintedGround(ServerPlayer player, int tries) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < tries; i++) {
            BlockPos target = player.blockPosition().offset(player.getRandom().nextInt(9) - 4, player.getRandom().nextInt(3) - 1, player.getRandom().nextInt(9) - 4);

            if (TaintSpreadRuntime.convert(serverLevel, target, true)) {
                message(player, "warp.text.6", "Warp leaks into the world as taint.");
                return;
            }
        }
    }

    private static void grantResearch(ServerPlayer player, int times) {
        int amount = Math.max(1, 1 + player.getRandom().nextInt(Math.max(1, times)));
        ItemStack points = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get(), amount);

        if (!player.getInventory().add(points.copy())) {
            player.drop(points.copy(), false);
        }
    }

    private static BlockPos findSpawnAround(ServerPlayer player, int min, int max) {
        ServerLevel level = player.getLevel();
        BlockPos base = player.blockPosition();

        for (int i = 0; i < 40; i++) {
            int dx = Mth.nextInt(player.getRandom(), min, max) * (player.getRandom().nextBoolean() ? 1 : -1);
            int dz = Mth.nextInt(player.getRandom(), min, max) * (player.getRandom().nextBoolean() ? 1 : -1);
            BlockPos pos = base.offset(dx, player.getRandom().nextInt(7) - 3, dz);

            while (pos.getY() > level.getMinBuildHeight() + 2 && level.getBlockState(pos.below()).isAir()) {
                pos = pos.below();
            }

            if (!level.getBlockState(pos.below()).isAir() && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }

        return base.offset(2, 0, 2);
    }
}
