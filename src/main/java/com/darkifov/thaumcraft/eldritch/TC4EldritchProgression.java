package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Stage144 strict TC4 bridge for the Eldritch research gate.
 *
 * The original 1.7.10 code unlocks hidden Eldritch progression from actual warp:
 *  - actual warp > 10 -> BATHSALTS hint/research
 *  - actual warp > 25 -> ELDRITCHMINOR
 *  - actual warp > 50 -> ELDRITCHMAJOR
 * Crimson Rites separately unlocks CRIMSON.  Modern helper research keys are
 * mirrored only so the already-existing 1.19.2 GUI/blocks stay compatible.
 */
public final class TC4EldritchProgression {
    public static final int BATHSALTS_WARP = 10;
    public static final int ELDRITCH_MINOR_WARP = 25;
    public static final int ELDRITCH_MAJOR_WARP = 50;

    private TC4EldritchProgression() {
    }

    public static void syncFromWarp(ServerPlayer player) {
        int actualWarp = PlayerThaumData.getActualWarp(player);
        boolean changed = false;

        if (actualWarp > BATHSALTS_WARP) {
            changed |= unlock(player, "BATHSALTS", "Your mind searches for a way to cleanse itself.");
            changed |= unlock(player, "@BATHSALTS", null);
        }

        if (actualWarp > ELDRITCH_MINOR_WARP) {
            changed |= unlock(player, "ELDRITCHMINOR", "Something alien becomes visible at the edge of thought.");
            changed |= unlock(player, "ELDRITCH_WHISPERS", null);
        }

        if (actualWarp > ELDRITCH_MAJOR_WARP) {
            changed |= unlock(player, "ELDRITCHMAJOR", "The Eldritch tab opens fully.");
            changed |= unlock(player, "ELDRITCH_START", null);
            changed |= unlock(player, "ELDRITCH_ALTAR", null);
        }

        if (PlayerThaumData.hasResearch(player, "CRIMSON") && PlayerThaumData.hasResearch(player, "ELDRITCHMAJOR")) {
            changed |= unlock(player, "OCULUS", null);
            changed |= unlock(player, "CRIMSON_KEY", null);
        }

        if (changed) {
            ThaumcraftNetwork.syncResearch(player);
        }
    }

    public static boolean readCrimsonRites(Player player) {
        boolean changed = PlayerThaumData.unlockResearch(player, "CRIMSON");
        PlayerThaumData.addWarpSticky(player, 1);
        PlayerThaumData.addEldritchAttunement(player, 5);

        if (!player.level.isClientSide) {
            player.displayClientMessage(Component.literal("The Crimson Rites are now known to you.").withStyle(ChatFormatting.DARK_RED), false);
            player.level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.75F, 0.65F);

            if (player instanceof ServerPlayer serverPlayer) {
                syncFromWarp(serverPlayer);
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        return changed;
    }

    public static boolean attuneWithEldritchEye(Player player, boolean consumeAttunement) {
        int actualWarp = PlayerThaumData.getActualWarp(player);

        if (actualWarp <= ELDRITCH_MINOR_WARP && PlayerThaumData.getEldritchAttunement(player) < 15) {
            if (!player.level.isClientSide) {
                player.displayClientMessage(Component.literal("The eye remains shut. More true Warp is required.").withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            return false;
        }

        PlayerThaumData.addWarpSticky(player, consumeAttunement ? 2 : 1);
        PlayerThaumData.addEldritchAttunement(player, consumeAttunement ? 10 : 5);
        PlayerThaumData.unlockResearch(player, "ELDRITCH_WHISPERS");
        PlayerThaumData.unlockResearch(player, "ELDRITCHMINOR");

        if (actualWarp > ELDRITCH_MAJOR_WARP || PlayerThaumData.getEldritchAttunement(player) >= 30) {
            PlayerThaumData.unlockResearch(player, "ELDRITCH_START");
            PlayerThaumData.unlockResearch(player, "ELDRITCH_ALTAR");
        }

        if (!player.level.isClientSide) {
            player.displayClientMessage(Component.literal("The eye opens. Something notices you.").withStyle(ChatFormatting.DARK_PURPLE), false);
            player.level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 0.9F, 0.55F);

            if (player instanceof ServerPlayer serverPlayer) {
                syncFromWarp(serverPlayer);
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        return true;
    }

    public static boolean canOpenOuterLands(Player player) {
        return PlayerThaumData.hasResearch(player, "ELDRITCHMINOR")
                || PlayerThaumData.hasResearch(player, "ELDRITCHMAJOR")
                || PlayerThaumData.hasResearch(player, "ELDRITCH_START");
    }

    public static boolean canStartGuardianTrial(Player player) {
        return PlayerThaumData.hasResearch(player, "ELDRITCHMAJOR")
                || PlayerThaumData.hasResearch(player, "ELDRITCH_START");
    }

    private static boolean unlock(ServerPlayer player, String key, String message) {
        boolean changed = PlayerThaumData.unlockResearch(player, key);

        if (changed && message != null && !message.isEmpty()) {
            player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.DARK_PURPLE), false);
        }

        return changed;
    }
}
