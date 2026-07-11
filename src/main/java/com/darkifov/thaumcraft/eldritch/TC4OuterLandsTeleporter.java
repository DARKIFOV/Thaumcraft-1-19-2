package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/** Real 1.19.2 dimension transfer path for TC4's TeleporterThaumcraft. */
public final class TC4OuterLandsTeleporter {
    private TC4OuterLandsTeleporter() {
    }

    public static boolean tryTeleport(ServerPlayer player, BlockPos sourcePortal) {
        if (player == null || sourcePortal == null || player.isOnPortalCooldown()) {
            return false;
        }
        if (!TC4EldritchProgression.canOpenOuterLands(player)) {
            player.displayClientMessage(Component.literal("The portal remains opaque. Eldritch research is required.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
            player.setPortalCooldown();
            return false;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }

        if (TC4OuterLandsDimensionAdapter.isOuterLands(player.level.dimension())) {
            return returnToOverworld(player, server);
        }

        ServerLevel destination = server.getLevel(TC4OuterLandsDimensionAdapter.OUTER_LANDS);
        if (destination == null) {
            player.displayClientMessage(Component.literal("Outer Lands dimension is unavailable in this world.")
                    .withStyle(ChatFormatting.RED), false);
            player.setPortalCooldown();
            return false;
        }

        PlayerThaumData.setOuterLandsReturnPos(player, sourcePortal.asLong());
        BlockPos destinationPortal = new BlockPos(sourcePortal.getX(), TC4OuterLandsDimensionAdapter.ORIGINAL_ROOM_Y, sourcePortal.getZ());
        prepareDestination(destination, destinationPortal);

        player.setPortalCooldown();
        player.teleportTo(destination, destinationPortal.getX() + 1.5D, destinationPortal.getY(),
                destinationPortal.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.setPortalCooldown();
        PlayerThaumData.unlockResearch(player, "ENTEROUTER");
        ThaumcraftNetwork.syncResearch(player);
        destination.playSound(null, destinationPortal, com.darkifov.thaumcraft.porting.TC4Sounds.event("evilportal"),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean returnToOverworld(ServerPlayer player, MinecraftServer server) {
        ServerLevel destination = server.getLevel(Level.OVERWORLD);
        if (destination == null) {
            return false;
        }
        BlockPos target = PlayerThaumData.hasOuterLandsReturnPos(player)
                ? BlockPos.of(PlayerThaumData.getOuterLandsReturnPos(player))
                : destination.getSharedSpawnPos();
        destination.getChunkAt(target);
        player.setPortalCooldown();
        player.teleportTo(destination, target.getX() + 1.5D, target.getY() + 0.1D,
                target.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.setPortalCooldown();
        destination.playSound(null, target, com.darkifov.thaumcraft.porting.TC4Sounds.event("evilportal"),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static void prepareDestination(ServerLevel level, BlockPos portalPos) {
        level.getChunkAt(portalPos);
        TC4OuterLandsMazeHandler.ensurePortalMaze(level, portalPos);
        TC4OuterLandsMazeHandler.generateAround(level, portalPos, 2);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(portalPos.offset(x, -1, z), ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState(), 3);
            }
        }
        level.setBlock(portalPos, ThaumcraftMod.ELDRITCH_PORTAL.get().defaultBlockState(), 3);
        level.removeBlock(portalPos.east(), false);
        level.removeBlock(portalPos.east().above(), false);
    }
}
