package com.darkifov.thaumcraft.eldritch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;


/**
 * Stage323-342 live tick bridge for TC4 ChunkProviderOuter.populate.
 *
 * <p>The original TC4 provider populated the Outer Lands when chunks were
 * provided.  The current 1.19.2 port still uses a soft dimension adapter, so we
 * invoke the populate bridge from server ticks only for recognized Outer Lands
 * dimensions and only once per chunk.  This is intentionally marked as a Forge
 * 1.19.2 adapter: it preserves the TC4 seed constants and room coordinates, but
 * avoids forcing generation hooks while the real dimension bootstrap is still
 * being ported.</p>
 */
public final class TC4OuterLandsLivePopulateAdapter {
    private static final int PLAYER_CHUNK_RADIUS = 1;

    private TC4OuterLandsLivePopulateAdapter() {
    }

    public static void tickPlayerArea(ServerLevel level, ServerPlayer player) {
        if (!TC4OuterLandsDimensionAdapter.isOuterLands(level.dimension())) {
            return;
        }
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        ChunkPos center = player.chunkPosition();
        for (int dx = -PLAYER_CHUNK_RADIUS; dx <= PLAYER_CHUNK_RADIUS; dx++) {
            for (int dz = -PLAYER_CHUNK_RADIUS; dz <= PLAYER_CHUNK_RADIUS; dz++) {
                populateChunkOnce(level, center.x + dx, center.z + dz);
            }
        }
    }

    public static boolean populateChunkOnce(ServerLevel level, int chunkX, int chunkZ) {
        if (!TC4OuterLandsDimensionAdapter.isOuterLands(level.dimension())) {
            return false;
        }
        BlockPos chunkProbe = new BlockPos((chunkX << 4) + 8, level.getMinBuildHeight(), (chunkZ << 4) + 8);
        if (!level.hasChunkAt(chunkProbe)) {
            return false;
        }
        TC4OuterLandsMazeSavedData data = TC4OuterLandsMazeSavedData.get(level);
        if (data.isChunkPopulated(chunkX, chunkZ)) {
            return false;
        }
        if (!TC4OuterLandsChunkProviderBridge.populateLikeTC4(level, chunkX, chunkZ)) {
            return false;
        }
        if (!data.markChunkPopulated(chunkX, chunkZ)) {
            return false;
        }
        data.setDirty();
        return true;
    }
}
