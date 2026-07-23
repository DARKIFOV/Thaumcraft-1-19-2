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
        BlockPos destinationPortal = prepareDestination(destination, sourcePortal);

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

    private static BlockPos prepareDestination(ServerLevel level, BlockPos sourcePortal) {
        // TC4 GenPortal always receives chunk-aligned cell coordinates and places
        // its actual portal at local (8, 3, 8).  The previous bridge used the
        // arbitrary Overworld portal coordinates as a room corner, then placed a
        // second portal at that corner.  That produced overlapping rooms and a
        // return portal that was not part of the generated maze.
        int originX = Math.floorDiv(sourcePortal.getX(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE)
                * TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE;
        int originZ = Math.floorDiv(sourcePortal.getZ(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE)
                * TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE;
        BlockPos mazeOrigin = new BlockPos(originX, TC4OuterLandsDimensionAdapter.ORIGINAL_ROOM_Y, originZ);

        level.getChunkAt(mazeOrigin);
        TC4OuterLandsMazeHandler.ensurePortalMaze(level, mazeOrigin);

        // Only the entry cell is allowed to generate synchronously. The old
        // bridge generated a five-by-five room square before teleportTo(),
        // which can monopolize the integrated-server tick while the client is
        // already waiting on the "Loading terrain" screen. Neighbouring maze
        // cells are populated progressively by TC4OuterLandsLivePopulateAdapter.
        int originCellX = Math.floorDiv(mazeOrigin.getX(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE);
        int originCellZ = Math.floorDiv(mazeOrigin.getZ(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE);
        TC4OuterLandsMazeHandler.generateEldritch(level, mazeOrigin, originCellX, originCellZ);

        BlockPos generatedPortal = mazeOrigin.offset(8, 3, 8);
        if (!level.getBlockState(generatedPortal).is(ThaumcraftMod.ELDRITCH_PORTAL.get())) {
            TC4OuterLandsGenCommonAdapter.generatePortalRoom(
                    level,
                    mazeOrigin,
                    TC4OuterLandsGenCommonAdapter.portalCell()
            );
        }

        // Keep the same safe one-block offset used by TeleporterThaumcraft while
        // preserving the portal and obelisk at the exact TC4 room coordinates.
        // Loading only this final landing chunk is bounded and does not trigger
        // the old five-by-five synchronous room generation.
        level.getChunkAt(generatedPortal);
        level.removeBlock(generatedPortal.east(), false);
        level.removeBlock(generatedPortal.east().above(), false);
        return generatedPortal;
    }
}
