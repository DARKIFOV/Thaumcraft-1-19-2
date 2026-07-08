package com.darkifov.thaumcraft.eldritch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;

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
    private static final Set<String> POPULATED = new HashSet<>();
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
        String key = level.dimension().location() + ":" + level.getSeed() + ":" + chunkX + ":" + chunkZ;
        if (!POPULATED.add(key)) {
            return false;
        }
        TC4OuterLandsChunkProviderBridge.populateLikeTC4(level, chunkX, chunkZ);
        return true;
    }
}
