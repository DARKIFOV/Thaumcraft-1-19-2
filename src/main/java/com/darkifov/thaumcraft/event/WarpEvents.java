package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

        int warp = PlayerThaumData.getWarp(player);

        if (warp <= 0 || PlayerThaumData.getWarpEventCooldown(player) > 0) {
            return;
        }

        int chance = Math.min(
                ThaumcraftConfig.WARP_EVENT_MAX_CHANCE.get(),
                Math.max(ThaumcraftConfig.WARP_EVENT_MIN_CHANCE.get(), warp * ThaumcraftConfig.WARP_EVENT_CHANCE_PER_WARP.get())
        );

        if (PlayerThaumData.hasWarpWard(player)) {
            chance = Math.max(1, chance / 4);

            if (player.getRandom().nextInt(100) < 18) {
                player.displayClientMessage(Component.literal("Warp Ward absorbs a whisper from beyond.").withStyle(ChatFormatting.AQUA), true);
            }
        }

        if (player.getRandom().nextInt(100) >= chance) {
            return;
        }

        triggerWarpEvent(player, warp);
        int randomCooldown = Math.max(1, ThaumcraftConfig.WARP_EVENT_COOLDOWN_RANDOM_TICKS.get());
        PlayerThaumData.setWarpEventCooldown(
                player,
                ThaumcraftConfig.WARP_EVENT_COOLDOWN_MIN_TICKS.get() + player.getRandom().nextInt(randomCooldown)
        );
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

    private static void triggerWarpEvent(ServerPlayer player, int warp) {
        int severity = warp >= 24 ? 4 : warp >= 16 ? 3 : warp >= 8 ? 2 : 1;
        int roll = player.getRandom().nextInt(4 + severity);

        if (roll <= 1) {
            whispers(player);
            return;
        }

        if (roll == 2) {
            mentalEffect(player, severity);
            return;
        }

        if (roll == 3 && severity >= 2) {
            spawnCrawler(player);
            return;
        }

        if (roll == 4 && severity >= 3) {
            taintGround(player);
            return;
        }

        if (severity >= 4) {
            eldritchWhisper(player);
            return;
        }

        whispers(player);
    }

    private static void whispers(ServerPlayer player) {
        player.displayClientMessage(Component.literal("You hear impossible whispers behind your thoughts.").withStyle(ChatFormatting.DARK_PURPLE), false);
        player.level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 0.8F, 0.6F + player.getRandom().nextFloat() * 0.3F);
    }

    private static void mentalEffect(ServerPlayer player, int severity) {
        player.displayClientMessage(Component.literal("Warp twists your senses.").withStyle(ChatFormatting.DARK_PURPLE), false);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * (6 + severity * 3), 0));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * Math.max(2, severity), 0));

        if (severity >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 10, 0));
        }
    }

    private static void spawnCrawler(ServerPlayer player) {
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = player.blockPosition().offset(player.getRandom().nextInt(9) - 4, 0, player.getRandom().nextInt(9) - 4);

        for (int i = 0; i < 6; i++) {
            if (serverLevel.getBlockState(pos.below()).isAir()) {
                pos = pos.below();
            }
        }

        TaintCrawlerEntity crawler = ThaumcraftMod.TAINT_CRAWLER.get().create(serverLevel);

        if (crawler == null) {
            return;
        }

        crawler.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
        serverLevel.addFreshEntity(crawler);
        player.displayClientMessage(Component.literal("Warp births a tainted thing nearby.").withStyle(ChatFormatting.DARK_PURPLE), false);
        serverLevel.playSound(null, pos, SoundEvents.AMBIENT_CAVE, SoundSource.HOSTILE, 0.8F, 0.7F);
    }

    private static void taintGround(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level;
        BlockPos center = player.blockPosition();

        for (int i = 0; i < 10; i++) {
            BlockPos target = center.offset(player.getRandom().nextInt(7) - 3, player.getRandom().nextInt(3) - 1, player.getRandom().nextInt(7) - 3);
            BlockState state = level.getBlockState(target);

            if (state.is(Blocks.DIRT)
                    || state.is(Blocks.GRASS_BLOCK)
                    || state.is(Blocks.COARSE_DIRT)
                    || state.is(Blocks.ROOTED_DIRT)
                    || state.is(Blocks.MUD)
                    || state.is(Blocks.STONE)) {
                level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
                player.displayClientMessage(Component.literal("Warp leaks into the world as taint.").withStyle(ChatFormatting.DARK_PURPLE), false);
                return;
            }
        }
    }

    private static void eldritchWhisper(ServerPlayer player) {
        boolean unlocked = PlayerThaumData.unlockResearch(player, "ELDRITCH_WHISPERS");
        PlayerThaumData.addEldritchAttunement(player, 5);

        if (unlocked) {
            player.displayClientMessage(Component.literal("A forbidden thought becomes research: Eldritch Whispers.").withStyle(ChatFormatting.DARK_PURPLE), false);
        } else {
            player.displayClientMessage(Component.literal("The forbidden door opens a little wider.").withStyle(ChatFormatting.DARK_PURPLE), false);
        }

        player.level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 0.8F, 0.7F);
    }
}
